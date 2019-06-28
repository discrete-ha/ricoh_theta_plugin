package com.airflare.ricoh_theta_plugin

import android.app.Activity
import android.content.Intent
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

class RicohThetaPlugin(val activity: Activity): MethodCallHandler, PluginRegistry.ActivityResultListener {
  var result : Result? = null
  var IMAGE_REQUEST_CODE = 100

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "ricoh_theta_plugin")
      var plugin = RicohThetaPlugin(registrar.activity())
      channel.setMethodCallHandler(plugin)
      registrar.addActivityResultListener(plugin)
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "startCamera" -> {
        this.result = result
        val styleClass = call.argument<String>("style")
        val intent = Intent(activity, LivePreviewActivity::class.java)
        intent.putExtra("style", styleClass)
        this.activity.startActivityForResult(intent, IMAGE_REQUEST_CODE)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onActivityResult(code: Int, resultCode: Int, data: Intent?): Boolean {
    if (code == IMAGE_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        val imageUri = data?.getStringExtra("fileUri")
        imageUri?.let { this.result?.success(imageUri) }
      } else {
        val errorCode = data?.getStringExtra("ERROR_CODE")
        this.result?.error(errorCode, null, null)
      }
      return true
    }
    return false
  }
}
