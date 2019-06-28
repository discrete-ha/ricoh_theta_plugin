//package theta;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.net.wifi.WifiInfo;
//import android.net.wifi.WifiManager;
//
//import org.apache.cordova.CallbackContext;
//import org.apache.cordova.CordovaPlugin;
//import org.json.JSONArray;
//import org.json.JSONException;
//
//import static android.content.Context.WIFI_SERVICE;
//
///**
// * This class echoes a string called from JavaScript.
// */
//public class ThetaNative extends CordovaPlugin {
//
//    private static final int IMAGE_REQUEST_CODE = 1;
//    CallbackContext callbackContext;
//
//    @Override
//    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
//        if (action.equals("startCamera")) {
//            this.callbackContext = callbackContext;
//            this.startCamera();
//            return true;
//        }
//        if (action.equals("isConnectedToThetaWifi")) {
//            this.callbackContext = callbackContext;
//            boolean isConnected = isConnectedToThetaWifi();
//            callbackContext.success(isConnected ? 1 : 0);
//            return true;
//        }
//        return false;
//    }
//
//    private boolean isConnectedToThetaWifi() {
//        WifiManager wifiManager = (WifiManager)cordova.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        String ssid = wifiInfo.getSSID();
//
//        return ssid.toLowerCase().contains("theta");
//    }
//
//    private void startCamera( ) {
//        cordova.setActivityResultCallback(this);
//        Intent intent = new Intent(cordova.getActivity(), LivePreviewActivity.class);
//        cordova.startActivityForResult(this, intent, IMAGE_REQUEST_CODE);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        if (requestCode == IMAGE_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                callbackContext.success(intent.getStringExtra("fileUri"));
//            }
//        }
//    }
//}
