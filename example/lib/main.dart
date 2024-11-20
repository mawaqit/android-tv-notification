import 'package:flutter/material.dart';
import 'package:notification_overlay/notification_overlay.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Notification Overlay Example'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () {
                  NotificationOverlay.showNotification(
                      'This is a test notification'
                  );
                },
                child: Text('Show Notification'),
              ),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  NotificationOverlay.hideNotification();
                },
                child: Text('Hide Notification'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
