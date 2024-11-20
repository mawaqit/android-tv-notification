# Custom AndroidTV Notification 

A Flutter package that provides a simple way to display overlay notifications on top of your app, even when it's in the background.

## Features

- Show overlay notifications on top of any app
- Hide notifications programmatically
- Permission handling for overlay display
- Works in both foreground and background

## Getting Started

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  notification_overlay: ^1.0.0

  ```

 ## Usage

```
import 'package:notification_overlay/notification_overlay.dart'
```

## Check and Request Permission

Before showing notifications, check and request overlay permission:

```
bool hasPermission = await NotificationOverlay.checkOverlayPermission();

// Request permission if needed
if (!hasPermission) {
  bool granted = await NotificationOverlay.requestOverlayPermission();
}
```
## Show and Hide Notifications

```
// Show a notification
await NotificationOverlay.showNotification('Hello World!');

// Hide the notification
await NotificationOverlay.hideNotification();

```

## Complete Example

```
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
                  NotificationOverlay.showNotification('This is a test notification');
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
```
## Additional Information

### Android Setup
Add the following permission to your AndroidManifest.xml:

```
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
```


