package com.example.model

enum class LauncherMode {
    NORMAL,       // Standard App List / Grid Layout
    FOCUS,        // Minimalist Olauncher-style text layout (3-5 allowed apps only)
    DRIVE,        // Large high-contrast cards (Maps, Music, Phone)
    SLEEP,        // Ultra-dim grayscale layout with Alarm/Meditation focus
    E_PAPER,      // E-Ink / Reader Sanctuary Mode with Living Bookshelf and friction vault
    PASS_THROUGH  // Hands UI control back to stock launcher
}
