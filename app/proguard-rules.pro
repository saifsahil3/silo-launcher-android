# ==============================================================================
# Silo Launcher ProGuard & R8 Optimization Rules
# ==============================================================================

# 1. Stack Traces & Google Play Crash De-obfuscation
# Preserves file names and line numbers in stack traces for de-obfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. General Annotations & Enums
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 3. Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# 4. Moshi JSON Serialization
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class * extends com.squareup.moshi.JsonAdapter
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-dontwarn com.squareup.moshi.**

# 5. Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**

# 6. Coil Image Loading
-dontwarn coil.**
-dontwarn coil3.**

# 7. Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# 8. Reorderable Grid
-keep class sh.calvin.reorderable.** { *; }

# 9. App Domain Models, Database Entities & UI States
-keep class com.example.model.** { *; }
-keep class com.example.db.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.ui.FocusTimerState { *; }
-keep class com.example.ui.SleepState { *; }
-keep class com.example.ui.CreatorSessionState { *; }
-keep class com.example.ui.DriveStats { *; }
-keep class com.example.ui.DriveAppPair { *; }
-keep class com.example.ui.DriveQuickShortcut { *; }
-keep class com.example.ui.theme.EPaperColorProfile { *; }

# 10. Android Jetpack ViewModels & Lifecycle
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# 11. AppWidget & System Integration
-keep public class * extends android.appwidget.AppWidgetProvider {
    public *;
}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
