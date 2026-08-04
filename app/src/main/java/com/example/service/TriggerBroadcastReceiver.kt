package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class TriggerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("TriggerReceiver", "Received context action: $action")

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                // Charger connected trigger
                val triggerIntent = Intent("com.example.morphlauncher.TRIGGER_EVENT").apply {
                    putExtra("event_type", "POWER_CONNECTED")
                    setPackage(context.packageName)
                }
                context.sendBroadcast(triggerIntent)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                val triggerIntent = Intent("com.example.morphlauncher.TRIGGER_EVENT").apply {
                    putExtra("event_type", "POWER_DISCONNECTED")
                    setPackage(context.packageName)
                }
                context.sendBroadcast(triggerIntent)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Boot completed
            }
        }
    }
}
