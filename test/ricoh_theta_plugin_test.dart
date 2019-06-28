import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ricoh_theta_plugin/ricoh_theta_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('ricoh_theta_plugin');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await RicohThetaPlugin.platformVersion, '42');
  });
}
