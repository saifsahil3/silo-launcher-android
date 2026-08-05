package com.example.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object DndManager {
    fun isNotificationPolicyAccessGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            return nm?.isNotificationPolicyAccessGranted == true
        }
        return true
    }

    fun openNotificationPolicySettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDndInterruptionFilter(context: Context): Int {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return try {
            nm?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        } catch (e: Exception) {
            NotificationManager.INTERRUPTION_FILTER_ALL
        }
    }

    fun setDndInterruptionFilter(context: Context, filter: Int): Boolean {
        if (!isNotificationPolicyAccessGranted(context)) {
            openNotificationPolicySettings(context)
            return false
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return try {
            nm?.setInterruptionFilter(filter)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
