package com.example.core.flag

/**
 * Enumeration of active application feature flags.
 * Add new feature flags here to protect unfinished or experimental functionality during development.
 */
enum class FeatureFlag(
    val key: String,
    val title: String,
    val description: String,
    val defaultValue: Boolean = false
) {
    SAMPLE_EXPERIMENTAL_FEATURE(
        key = "sample_experimental_feature",
        title = "Sample Feature Flag",
        description = "Template flag for guarding upcoming features during feature development",
        defaultValue = false
    );

    companion object {
        fun fromKey(key: String): FeatureFlag? = entries.find { it.key == key }
    }
}
