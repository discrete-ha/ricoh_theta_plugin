import 'dart:async';

import 'package:flutter/services.dart';

class RicohThetaPlugin {
  static const MethodChannel _channel =
      const MethodChannel('ricoh_theta_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> startCamera() async => await _channel.invokeMethod('startCamera',{"style":"spacely_red"});

}
