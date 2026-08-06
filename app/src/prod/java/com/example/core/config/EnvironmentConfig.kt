package com.example.core.config

object ProdConfig : AppConfig {
    override val environmentName: String = "Production"
    override val preferencesName: String = "launcher_prod"
    override val isDebugLoggingEnabled: Boolean = false
    override val isVerboseLoggingEnabled: Boolean = false
    override val isPerformanceLoggingEnabled: Boolean = false
    override val isLabsAvailable: Boolean = false
    override val isAnalyticsEnabled: Boolean = true
    override val isCrashReportingEnabled: Boolean = true
}

object EnvironmentConfig {
    val current: AppConfig = ProdConfig
}
