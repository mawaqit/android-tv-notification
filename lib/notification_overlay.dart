export 'notification_overlay.dart';
import 'package:flutter/services.dart';

class NotificationOverlay {
  static const MethodChannel _channel = MethodChannel('notification_overlay');

  static Future<bool> checkOverlayPermission() async {
    final bool hasPermission =
        await _channel.invokeMethod('checkOverlayPermission');
    return hasPermission;
  }

  static Future<bool> requestOverlayPermission() async {
    final bool granted =
        await _channel.invokeMethod('requestOverlayPermission');
    return granted;
  }

  static Future<bool> showNotification(String message) async {
    final bool result = await _channel.invokeMethod('showNotification', {
      'message': message,
    });
    return result;
  }

  static Future<void> hideNotification() async {
    await _channel.invokeMethod('hideNotification');
  }
}
