import 'package:flutter/services.dart';

class AccessibilityServiceHelper {
  static const platform = MethodChannel('com.accessapp/accessibility');

  static Future<bool> isAccessibilityEnabled() async {
    try {
      final result = await platform.invokeMethod<bool>('isAccessibilityEnabled');
      return result ?? false;
    } catch (e) {
      return false;
    }
  }

  static Future<void> openAccessibilitySettings() async {
    try {
      await platform.invokeMethod('openAccessibilitySettings');
    } catch (e) {
      print('Error: $e');
    }
  }
}
