package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.AppInfo

object PassThroughManager {

    private const val PREFS_NAME = "passthrough_prefs"
    private const val KEY_PASSTHROUGH_PACKAGE = "passthrough_package_name"
    private const val KEY_PASSTHROUGH_ACTIVE = "passthrough_active"

    fun getInstalledLaunchers(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val ownPackageName = context.packageName

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val resolveInfoList = try {
            pm.queryIntentActivities(homeIntent, 0)
        } catch (e: Throwable) {
            emptyList()
        }

        val launcherList = mutableListOf<AppInfo>()

        for (resolveInfo in resolveInfoList) {
            try {
                val activityInfo = resolveInfo.activityInfo ?: continue
                val packageName = activityInfo.packageName ?: continue
                if (packageName == ownPackageName) continue

                val label = try {
                    resolveInfo.loadLabel(pm)?.toString() ?: packageName
                } catch (e: Throwable) {
                    packageName
                }

                val launchIntent = try {
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        component = ComponentName(activityInfo.packageName, activityInfo.name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    }
                } catch (e: Throwable) {
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        setPackage(packageName)
                    }
                }

                val iconDrawable = try {
                    resolveInfo.loadIcon(pm)
                } catch (e: Throwable) {
                    null
                }

                launcherList.add(
                    AppInfo(
                        label = label,
                        packageName = packageName,
                        launchIntent = launchIntent,
                        iconDrawable = iconDrawable,
                        isSystemApp = true,
                        category = "Stock Launcher"
                    )
                )
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        return launcherList.distinctBy { it.packageName }
    }

    fun detectStockLauncherPackage(context: Context): String? {
        val installed = getInstalledLaunchers(context)
        if (installed.isEmpty()) return null

        val knownStockPackages = listOf(
            "com.google.android.apps.nexuslauncher", // Pixel Launcher
            "com.sec.android.app.launcher",          // Samsung OneUI Launcher
            "com.miui.home",                         // Xiaomi MIUI / HyperOS
            "com.oppo.launcher",                     // ColorOS / OnePlus
            "com.oneplus.launcher",
            "com.huawei.android.launcher",           // EMUI
            "com.motorola.launcher3"                 // Moto Launcher
        )

        for (pkg in knownStockPackages) {
            if (installed.any { it.packageName.equals(pkg, ignoreCase = true) }) {
                return pkg
            }
        }

        return installed.firstOrNull()?.packageName
    }

    fun getSavedPassThroughLauncher(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_PASSTHROUGH_PACKAGE, null)
        if (!saved.isNullOrEmpty()) {
            return saved
        }

        val autoDetected = detectStockLauncherPackage(context)
        if (autoDetected != null) {
            savePassThroughLauncher(context, autoDetected)
        }
        return autoDetected
    }

    fun savePassThroughLauncher(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PASSTHROUGH_PACKAGE, packageName).apply()
    }

    fun isPassThroughActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PASSTHROUGH_ACTIVE, false)
    }

    fun setPassThroughActive(context: Context, active: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PASSTHROUGH_ACTIVE, active).apply()
    }

    fun launchPassThroughLauncher(context: Context, packageName: String? = null): Boolean {
        val targetPkg = packageName ?: getSavedPassThroughLauncher(context) ?: return false
        val pm = context.packageManager

        var launchIntent: Intent? = null

        // 1. Resolve exact ComponentName handling CATEGORY_HOME inside targetPkg
        try {
            val homeQuery = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                setPackage(targetPkg)
            }
            val resolveList = pm.queryIntentActivities(homeQuery, 0)
            if (!resolveList.isNullOrEmpty()) {
                val actInfo = resolveList.first().activityInfo
                launchIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    component = ComponentName(actInfo.packageName, actInfo.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // 2. Fallback to package launcher intent
        if (launchIntent == null) {
            try {
                launchIntent = pm.getLaunchIntentForPackage(targetPkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // 3. Fallback to generic home intent with setPackage
        if (launchIntent == null) {
            launchIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                setPackage(targetPkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        return try {
            context.startActivity(launchIntent)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }
}
