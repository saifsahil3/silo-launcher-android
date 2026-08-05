package com.example.service

import android.content.Context

/**
 * Legacy stub - Floating overlay service has been removed completely per Google Play Store compliance
 * and zero-overlay design guidelines.
 */
class PassThroughOverlayService {
    companion object {
        fun startService(context: Context) {
            // No-op: Overlay functionality removed for 100% Google Play compliance
        }

        fun stopService(context: Context) {
            // No-op: Overlay functionality removed
        }
    }
}
