import Flutter
import UIKit

public class SwiftRicohThetaPlugin: NSObject, FlutterPlugin, CameraCaptureProtocol {
    var result: FlutterResult?
    
    func cameraCaptureDidFinish(fileURI: String?) {
        if (fileURI != nil && self.result != nil) {
            self.result!(fileURI)
        }
    }
    
    public static let BUNDLE_ID = "org.cocoapods.ricoh-theta-plugin"
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "ricoh_theta_plugin", binaryMessenger: registrar.messenger())
        let instance = SwiftRicohThetaPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "getPlatformVersion":
            result("iOS " + UIDevice.current.systemVersion)
        case "startCamera":
            let storyboard = UIStoryboard(name: "CameraStoryboard", bundle: Bundle(identifier: SwiftRicohThetaPlugin.BUNDLE_ID) )
            let viewNaviController:UINavigationController = storyboard.instantiateViewController(withIdentifier: "LiveViewController") as! UINavigationController
            let viewController:LiveViewController = viewNaviController.topViewController as! LiveViewController
            viewController.delegate = self
            let hostViewController = UIApplication.shared.keyWindow?.rootViewController
            DispatchQueue.main.async {
                hostViewController?.present(viewController, animated: false, completion: nil)
            }
            self.result = result
        default:
            result(FlutterMethodNotImplemented)
        }
    }
}
