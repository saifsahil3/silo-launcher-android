package com.example.core.flag

/**
 * Enumeration of all application feature flags.
 */
enum class FeatureFlag(
    val key: String,
    val title: String,
    val description: String,
    val defaultValue: Boolean = false
) {
    CREATOR_MODE(
        key = "creator_mode",
        title = "Creator Mode",
        description = "Enable Creator Mode features, analytics, and custom layout tools",
        defaultValue = false
    ),
    NEW_WIDGET_PAGE(
        key = "new_widget_page",
        title = "New Widget Page",
        description = "Enable the redesigned Android widget host screen",
        defaultValue = false
    ),
    EXPERIMENTAL_SEARCH(
        key = "experimental_search",
        title = "Experimental Search",
        description = "Enable experimental indexed search algorithm and web integration",
        defaultValue = false
    ),
    NEW_SETTINGS_UI(
        key = "new_settings_ui",
        title = "New Settings UI",
        description = "Enable modern card-based settings interface",
        defaultValue = false
    );

    companion object {
        fun fromKey(key: String): FeatureFlag? = entries.find { it.key == key }
    }
}
