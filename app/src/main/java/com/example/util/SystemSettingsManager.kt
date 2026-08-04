package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

object SystemSettingsManager {

    /**
     * Toggles system-wide monochrome/grayscale mode via Settings.Secure daltonizer keys.
     * Requires WRITE_SECURE_SETTINGS permission granted via ADB.
     * Returns true if successfully updated, false if SecurityException or permission missing.
     */
    fun toggleMonochromeGrayscale(context: Context, enabled: Boolean): Boolean {
        return try {
            val contentResolver = context.contentResolver
            val enabledSuccess = Settings.Secure.putInt(
                contentResolver,
                "accessibility_display_daltonizer_enabled",
                if (enabled) 1 else 0
            )
            val modeSuccess = Settings.Secure.putInt(
                contentResolver,
                "accessibility_display_daltonizer",
                if (enabled) -1 else 0
            )
            enabledSuccess && modeSuccess
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Checks if daltonizer grayscale mode is currently enabled in system settings.
     */
    fun isMonochromeEnabled(context: Context): Boolean {
        return try {
            val contentResolver = context.contentResolver
            val daltonizerEnabled = Settings.Secure.getInt(
                contentResolver,
                "accessibility_display_daltonizer_enabled",
                0
            ) == 1
            val daltonizerMode = Settings.Secure.getInt(
                contentResolver,
                "accessibility_display_daltonizer",
                0
            ) == -1
            daltonizerEnabled && daltonizerMode
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Checks whether the app holds WRITE_SECURE_SETTINGS permission.
     */
    fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        val permissionResult = context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)
        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns the ADB grant command string for user copy/paste reference.
     */
    fun getAdbCommand(context: Context): String {
        return "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
    }
}
