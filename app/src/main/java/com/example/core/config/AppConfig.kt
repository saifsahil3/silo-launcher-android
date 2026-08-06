package com.example.core.config

/**
 * Interface defining environment-specific application configuration.
 */
interface AppConfig {
    val environmentName: String
    val preferencesName: String
    val isDebugLoggingEnabled: Boolean
    val isVerboseLoggingEnabled: Boolean
    val isPerformanceLoggingEnabled: Boolean
    val isLabsAvailable: Boolean
    val isAnalyticsEnabled: Boolean
    val isCrashReportingEnabled: Boolean
}
