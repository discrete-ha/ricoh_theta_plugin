/*
 * Copyright Ricoh Company, Ltd. All rights reserved.
 */

#import <UIKit/UIKit.h>
#import <GLKit/GLKit.h>
#import "HttpSession.h"

@class HttpImageInfo;

#define KINT_HIGHT_INTERVAL_BUTTON  54

typedef enum : int {
    NoneInertia = 0,
    ShortInertia,
    LongInertia
} enumInertia;

@interface ImageViewController : UIViewController

@property NSString *fileURI;

- (void)getObject:(HttpImageInfo *)imageInfo withSession:(HttpSession *)session;

@end
