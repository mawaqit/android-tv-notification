package com.example.notification_overlay

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import android.util.Log

class NotificationOverlayPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    private val TAG = "NotificationOverlayPlugin"
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
        try {
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
                    val message = call.argument<String>("message") ?: ""
                    notificationOverlay.show(message)
                    result.success(true)
                }
                "hideNotification" -> {
                    notificationOverlay.hide()
                    result.success(true)
                }
                else -> {
                    result.notImplemented()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in method call: ${call.method}", e)
            result.error("ERROR", e.message, e.stackTraceToString())
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        try {
            notificationOverlay.dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing notification overlay", e)
        }
        channel.setMethodCallHandler(null)
    }
    
    private fun isAndroidTV(): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    private fun hasOverlayPermissionInPackageInfo(): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            }
            
            packageInfo.requestedPermissions?.contains(android.Manifest.permission.SYSTEM_ALERT_WINDOW) == true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking package info", e)
            false
        }
    }
    
    private fun checkOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android TV, check if the permission is declared in manifest
            if (isAndroidTV()) {
                // First check if permission is in manifest
                if (hasOverlayPermissionInPackageInfo()) {
                    // Try to read the stored permission state
                    val prefs = context.getSharedPreferences("overlay_prefs", Context.MODE_PRIVATE)
                    return prefs.getBoolean("overlay_granted", false)
                }
                return false
            }
            // For regular Android devices, use the standard check
            return Settings.canDrawOverlays(context)
        }
        return true
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkOverlayPermission()) {
                try {
                    activity?.let { activity ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
                    } ?: run {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        
                        if (isAndroidTV()) {
                            android.os.Handler().postDelayed({
                                context.getSharedPreferences("overlay_prefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("overlay_granted", true)
                                    .apply()
                                pendingResult?.success(true)
                                pendingResult = null
                            }, 1000)
                        } else {
                            android.os.Handler().postDelayed({
                                pendingResult?.success(Settings.canDrawOverlays(context))
                                pendingResult = null
                            }, 1000)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to request overlay permission", e)
                    pendingResult?.error("PERMISSION_REQUEST_FAILED", e.message, null)
                    pendingResult = null
                }
            } else {
                pendingResult?.success(true)
                pendingResult = null
            }
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
            if (isAndroidTV()) {
                // For Android TV, store the permission state
                context.getSharedPreferences("overlay_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("overlay_granted", true)
                    .apply()
                pendingResult?.success(true)
            } else {
                // For regular Android devices, check the actual permission
                pendingResult?.success(Settings.canDrawOverlays(context))
            }
            pendingResult = null
            return true
        }
        return false
    }
}