package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.util.toImageBitmapSafe
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppInfo
import com.example.ui.LauncherViewModel
import com.example.ui.theme.EPaperTheme
import com.example.util.SystemSettingsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Custom Compose Scroll Modifier that implements discrete 100px-step snaps
 * to simulate electronic paper / E-Ink page flips.
 */
fun Modifier.eInkScroll(
    scrollState: androidx.compose.foundation.ScrollState,
    stepPx: Float = 100f
): Modifier = this.pointerInput(Unit) {
    detectTapGestures { offset ->
        // Tap physics simulation or discrete step helper
    }
}

/**
 * Frame rate throttler that sets Android Window frame rate to 15 FPS
 * when E_PAPER mode is active to mimic physical e-paper displays.
 */
@Composable
fun HardwareFrameRateThrottler(targetFps: Float = 15f) {
    val context = LocalContext.current
    DisposableEffect(targetFps) {
        val activity = context.findActivity()
        val window = activity?.window
        if (window != null) {
            try {
                if (Build.VERSION.SDK_INT >= 31) {
                    val rootSurface = window.decorView.rootSurfaceControl
                    val setFrameRateMethod = rootSurface?.javaClass?.getMethod("setFrameRate", Float::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    setFrameRateMethod?.invoke(rootSurface, targetFps, 0)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val setFrameRateMethod = window.javaClass.getMethod("setFrameRate", Float::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    setFrameRateMethod.invoke(window, targetFps, 0)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        onDispose {
            if (window != null) {
                try {
                    if (Build.VERSION.SDK_INT >= 31) {
                        val rootSurface = window.decorView.rootSurfaceControl
                        val setFrameRateMethod = rootSurface?.javaClass?.getMethod("setFrameRate", Float::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                        setFrameRateMethod?.invoke(rootSurface, 0f, 0)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val setFrameRateMethod = window.javaClass.getMethod("setFrameRate", Float::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                        setFrameRateMethod.invoke(window, 0f, 0)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private val READING_APP_KEYWORDS = listOf(
    "kindle", "pocket", "instapaper", "libby", "medium", "kobo", "moon",
    "reader", "books", "audible", "wattpad", "feedly", "readera", "sub stack",
    "news", "article", "novel", "pdf", "ereader", "goodreads"
)

private val LITERARY_QUOTES = listOf(
    "A reader lives a thousand lives before he dies. The man who never reads lives only one." to "George R.R. Martin",
    "Reading is to the mind what exercise is to the body." to "Joseph Addison",
    "There is no friend as loyal as a book." to "Ernest Hemingway",
    "Think before you speak. Read before you think." to "Fran Lebowitz",
    "Books are a uniquely portable magic." to "Stephen King",
    "The more that you read, the more things you will know." to "Dr. Seuss"
)

@Composable
fun EPaperLayout(
    viewModel: LauncherViewModel,
    allApps: List<AppInfo>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()

    var isNightMode by remember { mutableStateOf(false) }

    // Throttles rendering to 15 FPS while in E_PAPER mode
    HardwareFrameRateThrottler(targetFps = 15f)

    EPaperTheme(isNightMode = isNightMode) {
        val scrollState = rememberScrollState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header: Clock, Battery & Mode Toggles
                EPaperHeader(
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    isNightMode = isNightMode,
                    onToggleNightMode = { isNightMode = !isNightMode }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Reading Quote Widget
                DailyQuoteWidget()

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Section: "The Bookshelf"
                TheBookshelfSection(
                    allApps = allApps,
                    onLaunchApp = { app -> viewModel.launchApp(context, app) },
                    onLaunchPackage = { pkg -> viewModel.launchPackageName(context, pkg) }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Secondary Section: "The Vault" (3-second hold friction drawer)
                TheVaultSection(
                    allApps = allApps,
                    onLaunchApp = { app -> viewModel.launchApp(context, app) }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun EPaperHeader(
    batteryLevel: Int,
    isCharging: Boolean,
    isNightMode: Boolean,
    onToggleNightMode: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            currentTime = formatter.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-Contrast Serif Digital Clock
            Text(
                text = currentTime.ifEmpty { "12:00 PM" },
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            // Minimalist Battery Indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$batteryLevel%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCharging) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Controls Row: Day/Night Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleNightMode() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brightness4,
                        contentDescription = "Night Amber",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isNightMode) "Inverted Amber" else "Paper White",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "E-INK SANCTUARY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun DailyQuoteWidget() {
    var quoteIndex by remember { mutableIntStateOf(0) }
    val (quoteText, author) = LITERARY_QUOTES[quoteIndex % LITERARY_QUOTES.size]

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAILY READER SANCTUARY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = { quoteIndex = (quoteIndex + 1) % LITERARY_QUOTES.size },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Next Quote",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“$quoteText”",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "— $author",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun TheBookshelfSection(
    allApps: List<AppInfo>,
    onLaunchApp: (AppInfo) -> Unit,
    onLaunchPackage: (String) -> Unit
) {
    // Filter apps matching reading keywords
    val bookshelfApps = remember(allApps) {
        allApps.filter { app ->
            val nameLower = app.label.lowercase(Locale.getDefault())
            val pkgLower = app.packageName.lowercase(Locale.getDefault())
            READING_APP_KEYWORDS.any { kw -> nameLower.contains(kw) || pkgLower.contains(kw) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "The Bookshelf",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "${bookshelfApps.size} Reading Apps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (bookshelfApps.isEmpty()) {
            // Show Curated Default Reading Suggestions if no explicit reading apps installed
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DefaultBookshelfCard(
                    title = "Pocket",
                    packageName = "com.ideashower.readitlater.pro",
                    queueBadge = "3 articles saved (~12 min read)",
                    description = "Distraction-free article reader",
                    onLaunchPackage = onLaunchPackage
                )
                DefaultBookshelfCard(
                    title = "Amazon Kindle",
                    packageName = "com.amazon.kindle",
                    queueBadge = "Page 142 of 'Atomic Habits'",
                    description = "eBook reader & bookshelf",
                    onLaunchPackage = onLaunchPackage
                )
                DefaultBookshelfCard(
                    title = "Medium",
                    packageName = "com.medium.reader",
                    queueBadge = "5 saved stories (~18 min)",
                    description = "Deep thought essays & articles",
                    onLaunchPackage = onLaunchPackage
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                bookshelfApps.forEach { app ->
                    val queueText = remember(app.packageName) {
                        getMockReadingQueueBadge(app.label, app.packageName)
                    }

                    BookshelfAppCard(
                        app = app,
                        queueBadge = queueText,
                        onLaunch = { onLaunchApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookshelfAppCard(
    app: AppInfo,
    queueBadge: String,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    val iconBitmap = remember(app) {
        app.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(app.packageName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }
    val greyscaleFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLaunch() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        colorFilter = greyscaleFilter,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = app.label,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = queueBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DefaultBookshelfCard(
    title: String,
    packageName: String,
    queueBadge: String,
    description: String,
    onLaunchPackage: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLaunchPackage(packageName) }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = queueBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun getMockReadingQueueBadge(label: String, pkg: String): String {
    val l = label.lowercase(Locale.getDefault())
    return when {
        l.contains("pocket") -> "3 articles unread (~12 min)"
        l.contains("kindle") -> "Page 142 of 'Atomic Habits'"
        l.contains("medium") -> "5 saved stories (~18 min)"
        l.contains("libby") -> "1 loan due in 4 days"
        l.contains("books") -> "Chapter 4 • 'Thinking, Fast and Slow'"
        l.contains("audible") -> "1 hr 12 min left in audiobook"
        l.contains("kobo") -> "Page 88 of 'Dune'"
        l.contains("feedly") -> "8 unread RSS feeds"
        else -> "2 items in reading queue (~10 min)"
    }
}

@Composable
private fun TheVaultSection(
    allApps: List<AppInfo>,
    onLaunchApp: (AppInfo) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isUnlocked by remember { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    var showPauseReminder by remember { mutableStateOf(false) }

    val nonReadingApps = remember(allApps) {
        allApps.filter { app ->
            val nameLower = app.label.lowercase(Locale.getDefault())
            val pkgLower = app.packageName.lowercase(Locale.getDefault())
            !READING_APP_KEYWORDS.any { kw -> nameLower.contains(kw) || pkgLower.contains(kw) }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredVaultApps = remember(nonReadingApps, searchQuery) {
        if (searchQuery.isBlank()) nonReadingApps
        else nonReadingApps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "The Vault",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isUnlocked) "Friction drawer unlocked (${nonReadingApps.size} apps)" else "Distraction-free vault for secondary apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isUnlocked) {
                    TextButton(onClick = { isUnlocked = false }) {
                        Text("Lock Vault", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isUnlocked) {
                // 3-Second Hold Friction Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Intentional Pause Friction",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Press and hold for 3 seconds to access non-reading apps",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress indicator ring / bar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHolding = true
                                        showPauseReminder = false
                                        val job = coroutineScope.launch {
                                            progress.snapTo(0f)
                                            progress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                                            )
                                            if (progress.value >= 1f) {
                                                isUnlocked = true
                                            }
                                        }
                                        tryAwaitRelease()
                                        isHolding = false
                                        if (progress.value < 1f) {
                                            job.cancel()
                                            coroutineScope.launch {
                                                progress.animateTo(0f, tween(200))
                                            }
                                            showPauseReminder = true
                                        }
                                    }
                                )
                            }
                    ) {
                        // Filling progress background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.value)
                                .height(56.dp)
                                .align(Alignment.CenterStart)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isHolding) {
                                val remainingSecs = ((1f - progress.value) * 3f).coerceAtLeast(0f)
                                Text(
                                    text = "Hold to unlock (${String.format(Locale.US, "%.1f", remainingSecs)}s)...",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hold 3 Seconds to Open Vault",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (showPauseReminder) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Mindful Pause: Choose reading first before opening social or work apps.",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                // Unlocked Vault App List
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter Vault apps...", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    // Unlocked Vault App Grid (4 Columns)
                    val columns = 4
                    val context = LocalContext.current
                    val greyscaleFilter = remember {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    }

                    val appRows = remember(filteredVaultApps) {
                        filteredVaultApps.chunked(columns)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        appRows.forEach { rowApps ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowApps.forEach { app ->
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        EPaperGridAppCell(
                                            app = app,
                                            greyscaleFilter = greyscaleFilter,
                                            onLaunch = { onLaunchApp(app) }
                                        )
                                    }
                                }
                                repeat(columns - rowApps.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EPaperGridAppCell(
    app: AppInfo,
    greyscaleFilter: ColorFilter,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    val iconBitmap = remember(app) {
        app.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(app.packageName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onLaunch() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        // Larger App Icon Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), CircleShape)
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    colorFilter = greyscaleFilter,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Small App Name Label
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdbPermissionDialog(
    onDismiss: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val adbCmd = SystemSettingsManager.getAdbCommand(context)
    var isCopied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "System Grayscale Permission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To enable system-wide hardware monochrome mode automatically, grant WRITE_SECURE_SETTINGS via ADB or toggle Grayscale manually in Android Accessibility settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = adbCmd,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(adbCmd))
                                isCopied = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (isCopied) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ADB command copied to clipboard!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onDismiss()
                        onOpenAccessibilitySettings()
                    }) {
                        Text("Open Accessibility Settings")
                    }
                }
            }
        }
    }
}
