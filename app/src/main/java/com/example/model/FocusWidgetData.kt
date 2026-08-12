package com.example.model

import org.json.JSONArray
import org.json.JSONObject

sealed class FocusWidgetData {
    data class SystemWidget(
        val widgetId: Int,
        val label: String,
        val packageName: String = "",
        var heightDp: Int = 180,
        var widthFraction: Float = 1.0f
    ) : FocusWidgetData()

    data class BuiltInNotes(
        var content: String,
        var heightDp: Int = 180,
        var widthFraction: Float = 1.0f
    ) : FocusWidgetData()

    data class BuiltInMantra(
        var quoteIndex: Int,
        var heightDp: Int = 140,
        var widthFraction: Float = 1.0f
    ) : FocusWidgetData()

    data class BuiltInTimer(
        var durationMinutes: Int = 25,
        var heightDp: Int = 160,
        var widthFraction: Float = 1.0f
    ) : FocusWidgetData()

    data class BuiltInAudio(
        val title: String = "Rain & Lo-Fi Focus Sound",
        var heightDp: Int = 180,
        var widthFraction: Float = 1.0f
    ) : FocusWidgetData()

    data class AppShortcut(
        val packageName: String,
        val appName: String,
        var heightDp: Int = 120,
        var widthFraction: Float = 1.0f
    ) : FocusWidgetData()
}

fun focusWidgetsToJson(widgets: List<FocusWidgetData>): String {
    val array = JSONArray()
    for (widget in widgets) {
        val obj = JSONObject()
        when (widget) {
            is FocusWidgetData.SystemWidget -> {
                obj.put("type", "SYSTEM_WIDGET")
                obj.put("widgetId", widget.widgetId)
                obj.put("label", widget.label)
                obj.put("packageName", widget.packageName)
                obj.put("heightDp", widget.heightDp)
                obj.put("widthFraction", widget.widthFraction.toDouble())
            }
            is FocusWidgetData.BuiltInNotes -> {
                obj.put("type", "BUILTIN_NOTES")
                obj.put("content", widget.content)
                obj.put("heightDp", widget.heightDp)
                obj.put("widthFraction", widget.widthFraction.toDouble())
            }
            is FocusWidgetData.BuiltInMantra -> {
                obj.put("type", "BUILTIN_MANTRA")
                obj.put("quoteIndex", widget.quoteIndex)
                obj.put("heightDp", widget.heightDp)
                obj.put("widthFraction", widget.widthFraction.toDouble())
            }
            is FocusWidgetData.BuiltInTimer -> {
                obj.put("type", "BUILTIN_TIMER")
                obj.put("durationMinutes", widget.durationMinutes)
                obj.put("heightDp", widget.heightDp)
                obj.put("widthFraction", widget.widthFraction.toDouble())
            }
            is FocusWidgetData.BuiltInAudio -> {
                obj.put("type", "BUILTIN_AUDIO")
                obj.put("title", widget.title)
                obj.put("heightDp", widget.heightDp)
                obj.put("widthFraction", widget.widthFraction.toDouble())
            }
            is FocusWidgetData.AppShortcut -> {
                obj.put("type", "APP_SHORTCUT")
                obj.put("packageName", widget.packageName)
                obj.put("appName", widget.appName)
                obj.put("heightDp", widget.heightDp)
                obj.put("widthFraction", widget.widthFraction.toDouble())
            }
        }
        array.put(obj)
    }
    return array.toString()
}

fun focusWidgetsFromJson(jsonString: String?): List<FocusWidgetData> {
    if (jsonString.isNullOrBlank()) return getDefaultFocusWidgets()
    return try {
        val array = JSONArray(jsonString)
        val list = mutableListOf<FocusWidgetData>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val type = obj.optString("type")
            val heightDp = obj.optInt("heightDp", 180)
            val widthFraction = obj.optDouble("widthFraction", 1.0).toFloat()
            when (type) {
                "SYSTEM_WIDGET" -> list.add(
                    FocusWidgetData.SystemWidget(
                        widgetId = obj.optInt("widgetId"),
                        label = obj.optString("label"),
                        packageName = obj.optString("packageName"),
                        heightDp = heightDp,
                        widthFraction = widthFraction
                    )
                )
                "BUILTIN_NOTES" -> list.add(
                    FocusWidgetData.BuiltInNotes(
                        content = obj.optString("content"),
                        heightDp = heightDp,
                        widthFraction = widthFraction
                    )
                )
                "BUILTIN_MANTRA" -> list.add(
                    FocusWidgetData.BuiltInMantra(
                        quoteIndex = obj.optInt("quoteIndex"),
                        heightDp = heightDp,
                        widthFraction = widthFraction
                    )
                )
                "BUILTIN_TIMER" -> list.add(
                    FocusWidgetData.BuiltInTimer(
                        durationMinutes = obj.optInt("durationMinutes", 25),
                        heightDp = heightDp,
                        widthFraction = widthFraction
                    )
                )
                "BUILTIN_AUDIO" -> list.add(
                    FocusWidgetData.BuiltInAudio(
                        title = obj.optString("title", "Rain & Lo-Fi Focus Sound"),
                        heightDp = heightDp,
                        widthFraction = widthFraction
                    )
                )
                "APP_SHORTCUT" -> list.add(
                    FocusWidgetData.AppShortcut(
                        packageName = obj.optString("packageName"),
                        appName = obj.optString("appName"),
                        heightDp = heightDp,
                        widthFraction = widthFraction
                    )
                )
            }
        }
        if (list.isEmpty()) getDefaultFocusWidgets() else list
    } catch (e: Exception) {
        getDefaultFocusWidgets()
    }
}

fun getDefaultFocusWidgets(): List<FocusWidgetData> {
    return listOf(
        FocusWidgetData.BuiltInTimer(25),
        FocusWidgetData.BuiltInNotes("Task 1: Finish Deep Work session\nTask 2: Review project pull requests"),
        FocusWidgetData.BuiltInMantra(0)
    )
}
