#import "RicohThetaPlugin.h"
#import <ricoh_theta_plugin/ricoh_theta_plugin-Swift.h>

@implementation RicohThetaPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftRicohThetaPlugin registerWithRegistrar:registrar];
}
@end
