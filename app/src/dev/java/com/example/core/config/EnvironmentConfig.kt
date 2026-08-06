package com.example.core.config

object DevConfig : AppConfig {
    override val environmentName: String = "Development"
    override val preferencesName: String = "launcher_dev"
    override val isDebugLoggingEnabled: Boolean = true
    override val isVerboseLoggingEnabled: Boolean = true
    override val isPerformanceLoggingEnabled: Boolean = true
    override val isLabsAvailable: Boolean = true
    override val isAnalyticsEnabled: Boolean = false
    override val isCrashReportingEnabled: Boolean = false
}

object EnvironmentConfig {
    val current: AppConfig = DevConfig
}
