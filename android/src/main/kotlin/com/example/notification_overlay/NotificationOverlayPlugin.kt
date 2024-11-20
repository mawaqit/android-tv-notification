package com.example.notification_overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

class NotificationOverlayPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    private lateinit var channel : MethodChannel
    private lateinit var context: Context
    private lateinit var notificationOverlay: NotificationOverlay
    private var activity: Activity? = null
    private var pendingResult: Result? = null
    private val OVERLAY_PERMISSION_REQ_CODE = 1234

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "notification_overlay")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        notificationOverlay = NotificationOverlay(context)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "checkOverlayPermission" -> {
                result.success(checkOverlayPermission())
            }
            "requestOverlayPermission" -> {
                if (checkOverlayPermission()) {
                    result.success(true)
                } else {
                    pendingResult = result
                    requestOverlayPermission()
                }
            }
   "showNotification" -> {
                if (checkOverlayPermission()) {
                    val message = call.argument<String>("message") ?: ""
                    notificationOverlay.show(message)
                    result.success(true)
                } else {
                    result.error("PERMISSION_DENIED", "Overlay permission not granted", null)
                }
            }
            "hideNotification" -> {
                notificationOverlay.hide()
                result.success(null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            activity?.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
                pendingResult?.success(true)
            } else {
                pendingResult?.success(false)
            }
            pendingResult = null
            return true
        }
        return false
    }
}