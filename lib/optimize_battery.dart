import 'dart:async';
import 'package:flutter/services.dart';




typedef OnClickable = void Function(bool);

class OptimizeBattery {
  static const MethodChannel _channel = MethodChannel('optimize_battery');

  static Future<bool> isIgnoringBatteryOptimizations() async {
    final reading =
        await _channel.invokeMethod('isIgnoringBatteryOptimizations');
    return reading!;
  }

  static Future<bool> stopOptimizingBatteryUsage() async {
    final reading = await _channel.invokeMethod('stopOptimizingBatteryUsage');
    return reading!;
  }

  static void listenOptimizingBatteryEvent(OnClickable callback) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'BatteryOptimizationDenied') {
        callback(call.arguments as bool);
      }
    });
  }
}
