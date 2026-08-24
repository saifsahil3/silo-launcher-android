package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppInfo
import com.example.data.ReadingAppDetector
import com.example.db.OfflineBookEntity
import com.example.ui.LauncherViewModel
import com.example.ui.components.CoverFlowBookshelf
import com.example.ui.components.EPaperBottomDrawer
import com.example.ui.components.rememberEPaperFlingBehavior
import com.example.ui.theme.EPaperColorProfile
import com.example.ui.theme.EPaperFontFamily
import com.example.ui.theme.EPaperMonoFamily
import com.example.ui.theme.EPaperTheme
import com.example.util.toImageBitmapSafe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Throttles frame rate on supported displays to ~15 FPS to emulate physical e-paper refresh.
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

private val CURATED_EDITORIAL_QUOTES = listOf(
    "A reader lives a thousand lives before he dies. The man who never reads lives only one." to "George R.R. Martin",
    "Reading is to the mind what exercise is to the body." to "Joseph Addison",
    "There is no friend as loyal as a book." to "Ernest Hemingway",
    "Think before you speak. Read before you think." to "Fran Lebowitz",
    "Books are a uniquely portable magic." to "Stephen King",
    "The more that you read, the more things you will know." to "Dr. Seuss",
    "We read to know we're not alone." to "William Nicholson",
    "No two persons ever read the same book." to "Edmund Wilson",
    "Quiet minds cannot be perplexed or frightened, but go on in fortune or misfortune at their own private pace." to "Robert Louis Stevenson",
    "The world was hers for the reading." to "Betty Smith"
)

@Composable
fun EPaperLayout(
    viewModel: LauncherViewModel,
    allApps: List<AppInfo>,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val offlineBooks by viewModel.offlineBooks.collectAsState()
    val colorProfile by viewModel.epaperColorProfile.collectAsState()
    val pinnedAppPackages by viewModel.pinnedReadingAppPackages.collectAsState()

    // 12 FPS Hardware Throttle for authentic E-Ink display experience
    HardwareFrameRateThrottler(targetFps = 12f)

    // Detected & Pinned Reading Apps
    val detectedReadingApps = remember(allApps) {
        allApps.filter { app -> ReadingAppDetector.isStrictReadingOrNewsApp(app) }
            .sortedBy { ReadingAppDetector.getReadingPriorityRank(it) }
    }

    // Rule: Up to 3 manually pinned custom apps on top + ALL auto-identified reading apps (unlimited count)
    val finalReadingApps = remember(allApps, detectedReadingApps, pinnedAppPackages) {
        val pinned = pinnedAppPackages.take(3).mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
        val pinnedPkgSet = pinned.map { it.packageName }.toSet()
        val remainingDetected = detectedReadingApps.filter { app -> !pinnedPkgSet.contains(app.packageName) }
        pinned + remainingDetected
    }

    val primaryReaderApp = remember(finalReadingApps) {
        finalReadingApps.firstOrNull { ReadingAppDetector.isBookOrReaderApp(it) } ?: finalReadingApps.firstOrNull()
    }

    val ePaperFlingBehavior = rememberEPaperFlingBehavior()

    EPaperTheme(profile = colorProfile) {
        val scrollState = rememberScrollState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState, flingBehavior = ePaperFlingBehavior)
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 64.dp)
            ) {
                // 1. Header & Status Bar (System Status Line + Secondary Utility Row)
                EPaperHeader(
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    currentColorProfile = colorProfile,
                    onCycleProfile = { viewModel.cycleEPaperColorProfile() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Editorial Quote Module (Frameless Typography with hairline divider)
                EditorialQuoteModule()

                Spacer(modifier = Modifier.height(20.dp))

                // 3. "Now Reading" Hero Banner
                NowReadingHeroBanner(
                    viewModel = viewModel,
                    offlineBooks = offlineBooks,
                    primaryReaderApp = primaryReaderApp
                )

                Spacer(modifier = Modifier.height(26.dp))

                // 4. Horizontal Cover-Flow Offline Bookshelf (3-Book Viewport)
                CoverFlowBookshelf(
                    viewModel = viewModel,
                    offlineBooks = offlineBooks,
                    readingApps = finalReadingApps
                )

                Spacer(modifier = Modifier.height(26.dp))

                // 5. Reading Apps Shelf
                ReadingAppsShelf(
                    allApps = allApps,
                    readingApps = finalReadingApps,
                    pinnedPackages = pinnedAppPackages,
                    onPinApp = { pkg -> viewModel.pinReadingApp(pkg) },
                    onUnpinApp = { pkg -> viewModel.unpinReadingApp(pkg) },
                    onLaunchApp = { app -> viewModel.launchApp(context, app) },
                    onLaunchPackage = { pkg -> viewModel.launchPackageName(context, pkg) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 6. Bottom Navigation Drawer Dock Handle
            EPaperBottomDrawer(
                allApps = allApps,
                currentColorProfile = colorProfile,
                onSelectColorProfile = { viewModel.setEPaperColorProfile(it) },
                onLaunchApp = { app -> viewModel.launchApp(context, app) },
                onOpenSettings = onOpenSettings,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * 1. Header & Status Bar
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EPaperHeader(
    batteryLevel: Int,
    isCharging: Boolean,
    currentColorProfile: EPaperColorProfile,
    onCycleProfile: () -> Unit
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
        // System Status Line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Large Display Serif Typography Clock
            Text(
                text = currentTime.ifEmpty { "11:34 AM" },
                style = MaterialTheme.typography.displayMedium,
                fontFamily = EPaperFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Right: Minimalist Battery Badge with exact % and charging glyph
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$batteryLevel%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = EPaperMonoFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCharging) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Utility Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Uppercase Workspace Label
            Text(
                text = "E-INK SANCTUARY",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = EPaperMonoFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )

            // Right: Interactive Color Profile / Theme Switcher Pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(onClick = onCycleProfile)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.InvertColors,
                        contentDescription = "Cycle Contrast Preset",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentColorProfile.badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 2. Editorial Quote Module (Frameless Layout + Hairline Divider)
 */
@Composable
private fun EditorialQuoteModule() {
    var quoteIndex by remember { mutableIntStateOf(0) }
    val (quoteText, author) = CURATED_EDITORIAL_QUOTES[quoteIndex % CURATED_EDITORIAL_QUOTES.size]

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "“",
                    fontSize = 32.sp,
                    fontFamily = EPaperFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = quoteText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = EPaperFontFamily,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— $author",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = EPaperFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            IconButton(
                onClick = { quoteIndex = (quoteIndex + 1) % CURATED_EDITORIAL_QUOTES.size },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Quote",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subtle hairline divider separating quote from lower content
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * 3. "Now Reading" Hero Banner
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NowReadingHeroBanner(
    viewModel: LauncherViewModel,
    offlineBooks: List<OfflineBookEntity>,
    primaryReaderApp: AppInfo?
) {
    val context = LocalContext.current
    val bookTitle by viewModel.currentlyReadingBook.collectAsState()
    val currentPage by viewModel.currentlyReadingPage.collectAsState()
    val totalPages by viewModel.currentlyReadingTotalPages.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    // Find if current reading title matches any offline book
    val activeOfflineBook = remember(offlineBooks, bookTitle) {
        offlineBooks.find { it.title.equals(bookTitle, ignoreCase = true) }
    }

    val authorText = activeOfflineBook?.author?.ifBlank { "Author" } ?: "Frank Herbert"
    val formatBadge = activeOfflineBook?.formatBadge ?: "EPUB"
    val percentage = if (totalPages > 0) ((currentPage.toFloat() / totalPages.toFloat()) * 100).roundToInt().coerceIn(0, 100) else 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Now Reading",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { showEditDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Progress",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hero Card Surface
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = {
                        if (activeOfflineBook != null) {
                            viewModel.openOfflineBook(context, activeOfflineBook)
                        } else if (primaryReaderApp != null) {
                            viewModel.launchApp(context, primaryReaderApp)
                        }
                    }
                )
                .shadow(3.dp, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High-Contrast Book Thumbnail on Left
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .width(54.dp)
                        .height(72.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = formatBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = "${percentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = EPaperMonoFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Stacked Metadata Hierarchy on Right
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bookTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = EPaperFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "by $authorText",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val isProgressLogged = currentPage > 0
                    val progressLabel = if (isProgressLogged) "Page $currentPage of $totalPages ($percentage%)" else "Ready to Read • Tap to Start"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = progressLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = if (isProgressLogged) EPaperMonoFamily else EPaperFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick Log -1 / +1 Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .combinedClickable(
                                        onClick = {
                                            if (currentPage > 0) {
                                                viewModel.updateReadingProgress(bookTitle, currentPage - 1, totalPages)
                                                if (activeOfflineBook != null) {
                                                    viewModel.updateOfflineBookProgress(activeOfflineBook.id, currentPage - 1, totalPages)
                                                }
                                            }
                                        }
                                    )
                            ) {
                                Text(
                                    text = " -1 ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .combinedClickable(
                                        onClick = {
                                            if (currentPage < totalPages) {
                                                viewModel.updateReadingProgress(bookTitle, currentPage + 1, totalPages)
                                                if (activeOfflineBook != null) {
                                                    viewModel.updateOfflineBookProgress(activeOfflineBook.id, currentPage + 1, totalPages)
                                                }
                                            }
                                        }
                                    )
                            ) {
                                Text(
                                    text = " +1 ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dual-Tone Interactive Progress Track
                    LinearProgressIndicator(
                        progress = { (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }

    // Edit Reading Progress Dialog
    if (showEditDialog) {
        var tempTitle by remember { mutableStateOf(bookTitle) }
        var tempPage by remember { mutableStateOf(currentPage.toString()) }
        var tempTotal by remember { mutableStateOf(totalPages.toString()) }

        Dialog(onDismissRequest = { showEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Edit Reading Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Book Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempPage,
                            onValueChange = { tempPage = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Current Page") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempTotal,
                            onValueChange = { tempTotal = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Total Pages") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val pageNum = tempPage.toIntOrNull() ?: currentPage
                                val totalNum = tempTotal.toIntOrNull() ?: totalPages
                                viewModel.updateReadingProgress(tempTitle.ifBlank { bookTitle }, pageNum, totalNum)
                                if (activeOfflineBook != null) {
                                    viewModel.updateOfflineBookProgress(activeOfflineBook.id, pageNum, totalNum)
                                }
                                showEditDialog = false
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. Reading Apps Shelf (Frameless list items + Header '+' App Pinning Sheet)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ReadingAppsShelf(
    allApps: List<AppInfo>,
    readingApps: List<AppInfo>,
    pinnedPackages: List<String>,
    onPinApp: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onLaunchPackage: (String) -> Unit
) {
    var showAppPickerSheet by remember { mutableStateOf(false) }
    var selectedAppForAction by remember { mutableStateOf<AppInfo?>(null) }
    val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var pickerSearchQuery by remember { mutableStateOf("") }

    val customPinnedCount = pinnedPackages.take(3).size
    val isMaxPinsReached = customPinnedCount >= 3

    val unpinnedInstalledApps = remember(allApps, readingApps, pickerSearchQuery) {
        val existingPkgSet = readingApps.map { it.packageName }.toSet()
        allApps.filter { !existingPkgSet.contains(it.packageName) &&
            (pickerSearchQuery.isBlank() || it.label.contains(pickerSearchQuery, ignoreCase = true))
        }.sortedBy { it.label.lowercase() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header Row
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reading Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (readingApps.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${readingApps.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dedicated '+' Button to launch App Picker Sheet with Custom Pin Counter
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(onClick = { showAppPickerSheet = true })
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Pin Reading App",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMaxPinsReached) "Pinned ($customPinnedCount/3)" else "Pin ($customPinnedCount/3)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (readingApps.isEmpty()) {
            // Fallback Sanctuary
            EmptyReadingAppsSanctuary(
                onLaunchGoogleNews = { onLaunchPackage(ReadingAppDetector.GOOGLE_NEWS_PACKAGE) },
                onOpenPicker = { showAppPickerSheet = true }
            )
        } else {
            // Frameless List Items separated by clean whitespace
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                readingApps.forEach { app ->
                    val isPinned = pinnedPackages.take(3).contains(app.packageName)
                    val categoryTag = remember(app.packageName, app.label) {
                        ReadingAppDetector.getReadingAppCategoryTag(app)
                    }

                    FramelessReadingAppRow(
                        app = app,
                        categoryTag = categoryTag,
                        isPinned = isPinned,
                        onLaunch = { onLaunchApp(app) },
                        onLongClick = { selectedAppForAction = app }
                    )
                }
            }
        }
    }

    // App Action Bottom Sheet (Unpin / Launch)
    if (selectedAppForAction != null) {
        val app = selectedAppForAction!!
        val isPinned = pinnedPackages.take(3).contains(app.packageName)

        ModalBottomSheet(
            onDismissRequest = { selectedAppForAction = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .combinedClickable(
                            onClick = {
                                if (isPinned) {
                                    onUnpinApp(app.packageName)
                                } else {
                                    if (!isMaxPinsReached) {
                                        onPinApp(app.packageName)
                                    }
                                }
                                selectedAppForAction = null
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.Delete else Icons.Default.PushPin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isPinned) "Unpin from Shelf" else "Pin to Shelf (Max 3)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // App Picker Bottom Sheet
    if (showAppPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppPickerSheet = false },
            sheetState = pickerSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Pin Custom Reading Apps",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Auto-detected reading apps are added automatically. You can pin up to 3 custom apps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isMaxPinsReached) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pin limit reached (3/3). Long-press any pinned app on the shelf to unpin it first.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = pickerSearchQuery,
                    onValueChange = { pickerSearchQuery = it },
                    placeholder = { Text("Search installed apps to pin...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(unpinnedInstalledApps, key = { it.packageName }) { app ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .combinedClickable(
                                    enabled = !isMaxPinsReached,
                                    onClick = {
                                        onPinApp(app.packageName)
                                        showAppPickerSheet = false
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMaxPinsReached) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = app.category.ifBlank { "General" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Pin",
                                    tint = if (isMaxPinsReached) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Frameless Reading App Row with high-contrast monochrome icon
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FramelessReadingAppRow(
    app: AppInfo,
    categoryTag: String,
    isPinned: Boolean,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit
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

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-Contrast Monochrome Circular Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        colorFilter = greyscaleFilter,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = app.label,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = categoryTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmptyReadingAppsSanctuary(
    onLaunchGoogleNews: () -> Unit,
    onOpenPicker: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Reading Sanctuary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pin reading, read-later, or news apps to your shelf for distraction-free reading.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(onClick = onOpenPicker)
                ) {
                    Text(
                        text = "Pin Apps (+)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(onClick = onLaunchGoogleNews)
                ) {
                    Text(
                        text = "Open Google News",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
