package com.example.core.log

import android.util.Log
import com.example.core.config.EnvironmentConfig

/**
 * Global application logger respecting build environment log levels.
 */
object AppLogger {
    private const val DEFAULT_TAG = "SiloLauncher"

    fun v(tag: String = DEFAULT_TAG, message: String) {
        if (EnvironmentConfig.current.isVerboseLoggingEnabled) {
            Log.v(tag, message)
        }
    }

    fun d(tag: String = DEFAULT_TAG, message: String) {
        if (EnvironmentConfig.current.isDebugLoggingEnabled) {
            Log.d(tag, message)
        }
    }

    fun p(tag: String = DEFAULT_TAG, metric: String, durationMs: Long) {
        if (EnvironmentConfig.current.isPerformanceLoggingEnabled) {
            Log.d(tag, "[PERF] $metric completed in ${durationMs}ms")
        }
    }

    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}
