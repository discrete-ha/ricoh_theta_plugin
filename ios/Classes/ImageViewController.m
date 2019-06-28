/*
 * Copyright Ricoh Company, Ltd. All rights reserved.
 */

#import "ImageViewController.h"
#import "glkViewController.h"
#import "GLRenderView.h"
#import "SphereXmp.h"
#import "HttpImageInfo.h"
#import <ricoh_theta_plugin/ricoh_theta_plugin-Swift.h>

@interface ImageViewController ()
{
    HttpImageInfo *_httpImageInfo;
    HttpSession *_session;
    NSMutableData *_imageData;
    int imageWidth;
    int imageHeight;
    GlkViewController *_glkViewController;
    float _yaw;
    float _roll;
    float _pitch;
}

@property (weak, nonatomic) IBOutlet UIImageView* imageView;
@property (weak, nonatomic) IBOutlet UIProgressView* progressView;
@property (weak, nonatomic) IBOutlet UIButton *doneButton;
@property (weak, nonatomic) IBOutlet UIButton *cancelButton;

@end

@implementation ImageViewController

#pragma mark - UI events

#pragma mark - HTTP Operation

- (void)getObject:(HttpImageInfo *)imageInfo withSession:(HttpSession *)session
{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_progressView.progress = 0.0;
        self->_progressView.hidden = NO;
    });
    
    _httpImageInfo = imageInfo;
    _session = session;
    NSString *fileUrl = imageInfo.file_id;
    // Semaphore for synchronization (cannot be entered until signal is called)
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    
    [session getResizedImageObject:fileUrl
                           onStart:^(int64_t totalLength) {
                               // Callback before object-data reception.
                               NSLog(@"getObject(%@) will received %lld bytes.", fileUrl, totalLength);
                           }
                           onWrite:^(int64_t totalBytesWritten, int64_t totalBytesExpectedToWrite) {
                               // Callback for each chunks.
                               dispatch_async(dispatch_get_main_queue(), ^{
                                   // Update progress.
                                   self->_progressView.progress = (float)totalBytesWritten / totalBytesExpectedToWrite;
                               });
                           }
                          onFinish:^(NSURL *location){
                              self->_imageData = [NSMutableData dataWithContentsOfURL:[NSURL URLWithString:fileUrl]];
                              dispatch_semaphore_signal(semaphore);
                          }];
    
    // Wait until signal is called
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    
    // Parse XMP data, it contains the data to correct the tilt.
    SphereXmp *xmp = [[SphereXmp alloc] init];
    [xmp parse:_imageData];
    
    // If there is no information, yaw, pitch and roll method returns NaN.
    NSString* tiltInfo = [NSString stringWithFormat:@"yaw:%@ pitch:%@ roll:%@",
                          xmp.yaw, xmp.pitch, xmp.roll];
    
    _yaw = [xmp.yaw floatValue];     // 0.0 if conversion fails
    _pitch = [xmp.pitch floatValue]; // 0.0 if conversion fails
    _roll = [xmp.roll floatValue];   // 0.0 if conversion fails
    
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_progressView.hidden = YES;
        self->_doneButton.enabled = YES;
        self->_cancelButton.enabled = YES;
        
        NSLog(@"Tilt info: %@", tiltInfo);
        [self startGLK];
    });
}

#pragma mark - Life cycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    _doneButton.enabled = NO;
    _cancelButton.enabled = NO;
    
    [_doneButton setTitle:[Localizer getStringWithKey:@"glphot_ok_button"] forState:UIControlStateNormal];
    [_cancelButton setTitle:[Localizer getStringWithKey:@"glphot_cancel_button"] forState:UIControlStateNormal];
}


- (void)viewWillAppear:(BOOL)animated {
    if (nil != _httpImageInfo && CODE_JPEG == _httpImageInfo.file_format) {
        _progressView.hidden = NO;
    }
    else {
        _progressView.hidden = YES;
    }
    
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    _imageView.image = nil;
}

#pragma make - operation

- (void)startGLK
{
    _glkViewController = [[GlkViewController alloc] init:_imageView.frame image:_imageData width:imageWidth height:imageHeight yaw:_yaw roll:_roll pitch:_pitch];
    _glkViewController.glRenderView.kindInertia = ShortInertia;
    
    [self addChildViewController:_glkViewController];
    [self.view addSubview:_glkViewController.view];
    _glkViewController.view.frame = _imageView.frame;
    [_glkViewController didMoveToParentViewController:self];
}

- (IBAction)didPressOk:(id)sender {
    [self writeImageToFile];
    [self performSegueWithIdentifier:@"FinishCapture" sender:self];
}

- (void) writeImageToFile {
    NSError *error = nil;
    NSURL *temporaryDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSCachesDirectory
                                                                          inDomain:NSUserDomainMask
                                                                 appropriateForURL:nil
                                                                            create:YES
                                                                             error:&error];
    NSURL *remoteURL = [NSURL URLWithString:_httpImageInfo.file_id];
    NSURL *localURL = [temporaryDirectoryURL URLByAppendingPathComponent:[remoteURL lastPathComponent]];
    self.fileURI = [localURL absoluteString];
    BOOL success = [_imageData writeToFile:[localURL path] atomically:NO];
    NSLog(@"Success writing to file: %d", success);
}

@end

