package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val label: String,
    val packageName: String,
    val launchIntent: Intent,
    val iconDrawable: Drawable? = null,
    val isSystemApp: Boolean = false,
    val category: String = "General"
)

class AppRepository {

    suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val appList = mutableListOf<AppInfo>()
        try {
            val pm = context.packageManager
            val ownPackageName = context.packageName

            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfoList: List<ResolveInfo> = try {
                pm.queryIntentActivities(mainIntent, 0)
            } catch (e: Throwable) {
                emptyList()
            }

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
                        pm.getLaunchIntentForPackage(packageName)
                    } catch (e: Throwable) {
                        null
                    } ?: continue

                    val iconDrawable = try {
                        resolveInfo.loadIcon(pm)
                    } catch (e: Throwable) {
                        null
                    }

                    val isSystemApp = try {
                        activityInfo.applicationInfo != null && (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    } catch (e: Throwable) {
                        false
                    }

                    val category = categorizeApp(packageName, label, isSystemApp)

                    appList.add(
                        AppInfo(
                            label = label,
                            packageName = packageName,
                            launchIntent = launchIntent,
                            iconDrawable = iconDrawable,
                            isSystemApp = isSystemApp,
                            category = category
                        )
                    )
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        appList.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    suspend fun getInstalledStockLaunchers(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val launcherList = mutableListOf<AppInfo>()
        try {
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
                        pm.getLaunchIntentForPackage(packageName) ?: Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            setPackage(packageName)
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
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        launcherList.distinctBy { it.packageName }
    }

    private fun categorizeApp(packageName: String, label: String, isSystemApp: Boolean): String {
        val pkg = packageName.lowercase()

        return when {
            pkg.contains("maps") || pkg.contains("navigation") || pkg.contains("uber") || pkg.contains("waze") -> "Navigation"
            pkg.contains("music") || pkg.contains("spotify") || pkg.contains("youtube") || pkg.contains("audio") || pkg.contains("podcast") || pkg.contains("player") -> "Media"
            pkg.contains("dialer") || pkg.contains("phone") || pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("message") || pkg.contains("contacts") -> "Communication"
            pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("firefox") || pkg.contains("search") -> "Tools"
            pkg.contains("camera") || pkg.contains("gallery") || pkg.contains("photos") -> "Media"
            pkg.contains("clock") || pkg.contains("alarm") || pkg.contains("calendar") || pkg.contains("notes") || pkg.contains("calculator") -> "Productivity"
            pkg.contains("game") || pkg.contains("play") -> "Games"
            isSystemApp -> "System"
            else -> "Apps"
        }
    }
}

