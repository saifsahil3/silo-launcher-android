package com.example.data

import java.util.Locale
import java.util.regex.Pattern

object ReadingAppDetector {

    const val GOOGLE_NEWS_PACKAGE = "com.google.android.apps.magazines"
    const val PLAY_STORE_MARKET_URI = "market://details?id=$GOOGLE_NEWS_PACKAGE"
    const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id=$GOOGLE_NEWS_PACKAGE"

    // High confidence Tier 1: Books, e-Readers, Document Readers & Audiobooks
    private val KNOWN_BOOK_PACKAGES = setOf(
        // eBoox & KOReader
        "com.reader.books",
        "com.eboox",
        "com.eboox.app",
        "com.eboox.reader",
        "org.koreader.launcher",
        "org.koreader.launcher.fdroid",

        // Major E-Book Platforms
        "com.amazon.kindle",
        "com.google.android.apps.books",
        "com.kobo.epub",
        "com.barnesandnoble.bnorm",
        "com.obreey.reader", // PocketBook
        "de.telekom.epub",   // Tolino

        // Independent & Open Source Readers
        "org.readera",
        "org.readera.premium",
        "com.flyersoft.moonreader",
        "com.flyersoft.moonreaderp",
        "com.foobnix.pdf.reader",
        "com.foobnix.pro.pdf.reader",
        "com.foobnix.librera",
        "com.faultexception.reader", // Lithium
        "org.geometerplus.zlibrary.ui.android", // FBReader
        "com.fbreader",
        "org.alreader",
        "org.alreaderx",
        "com.bookfusion.android.reader",
        "com.turnipsoft.freda",
        "com.gmail.jxlab.app.reasily",
        "org.coolreader",
        "com.prestigio.ereader",
        "com.plethora.ereader",
        "org.ebookdroid",
        "com.graphilos.acv",
        "com.onyx.android.sdk",
        "com.onyx.kreader",

        // Library & Audiobooks
        "com.overdrive.mobile.android.libby",
        "com.overdrive.mobile.android.mediaconsole",
        "com.hoopladigital.android",
        "com.audible.application",
        "com.scribd.app.reader0",
        "com.scribd.everand",
        "com.storytel.app",
        "com.bookbeat.android",
        "com.frescano.nextory",
        "com.legimi.ereader",
        "com.blinkist.android",
        "com.headway.books",
        "com.goodreads.kindle",
        "com.thestorygraph.storygraph",
        "com.calibrecompanion",
        "cz.tomasdvorak.calibresync",

        // Manga & Comics
        "xyz.mihon",
        "eu.kanade.tachiyomi",
        "eu.kanade.tachiyomi.sy",
        "eu.kanade.tachiyomi.j2k",
        "org.koitharu.kotatsu",
        "org.kavitareader.kavita",
        "org.gotson.komga",
        "com.progdigy.cdisplay",
        "com.rookiestudio.perfectviewer",
        "com.brunodles.kuroreader",
        "com.viewer.comicscreen",
        "org.aerofrog.comicreader",
        "com.naver.linewebtoon",
        "com.tapastic"
    )

    // High confidence Tier 2: Articles, Read-Later & RSS
    private val KNOWN_ARTICLE_PACKAGES = setOf(
        "com.ideashower.readitlater.pro",
        "com.instapaper.android",
        "com.medium.reader",
        "com.substack.app",
        "com.readwise",
        "com.readwise.reader",
        "com.omnivore.app",
        "com.matter.reader",
        "fr.gaelj.wallabag",
        "com.devhd.feedly",
        "com.innologica.inoreader",
        "com.newsblur",
        "com.ranchero.NetNewsWire",
        "com.nononsenseapps.feeder",
        "com.readrops.app",
        "org.fox.ttrss",
        "q.buzz.pluma"
    )

    // High confidence Tier 3: Text-based News
    private val KNOWN_NEWS_PACKAGES = setOf(
        GOOGLE_NEWS_PACKAGE,
        "com.nytimes.android",
        "com.theguardian",
        "bbc.mobile.news.ww",
        "com.reuters.android",
        "com.wsj.android",
        "com.economist.lamarr",
        "com.washingtonpost.android",
        "com.bloomberg.android.plus",
        "com.theatlantic.android",
        "com.huffpost.android",
        "com.ft.news",
        "com.pressreader"
    )

    // Strict Blacklist for non-reading apps (banking, shopping, smart home, photo, utilities)
    private val EXCLUDED_PACKAGES_OR_PREFIXES = listOf(
        "au.com.shiftyjelly.pocketcasts",
        "com.limelight",
        "com.whatsapp",
        "org.telegram.messenger",
        "com.spotify.music",
        "com.google.android.youtube",
        "com.bigbasket.mobileapp",
        "com.axis.mobile",
        "com.upi.axispay",
        "com.discoverfinancial.mobile",
        "com.ecobee.athena",
        "com.adobe.lrmobile",
        "com.walgreens.android",
        "com.google.android.calculator",
        "com.google.android.deskclock",
        "com.google.android.GoogleCamera",
        "com.google.android.apps.photos",
        "com.android.settings",
        "com.android.vending"
    )

    // Word boundary patterns to prevent substring leakage
    private val BOOK_PATTERN = Pattern.compile(
        "\\b(kindle|kobo|nook|tolino|pocketbook|readera|moon\\+? ?reader|alreader|fbreader|aldiko|lithium|librera|prestigio|ereader|ebook|ebooks|e-book|epub|mobi|audible|libby|overdrive|hoopla|scribd|storytel|audiobook|audiobooks|goodreads|storygraph|blinkist|headway|tachiyomi|mihon|kotatsu|kavita|komga|calibre|play books|book reader|pdf reader|eboox|e-boox|boox|koreader|bookfusion|freda|reasily|cdisplay|comicscreen|kuro reader|perfect viewer)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val ARTICLE_PATTERN = Pattern.compile(
        "\\b(pocket|instapaper|medium|substack|omnivore|wallabag|readwise|feedly|inoreader|newsblur|netnewswire|feeder|rss|readrops|matter reader|pluma)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val NEWS_PATTERN = Pattern.compile(
        "\\b(google news|newspaper|the guardian|reuters|ap news|bbc news|the economist|the atlantic|new yorker|bloomberg news|washington post|wall street journal|wsj|pressreader|daily news|huffpost)\\b",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Checks if the app is strictly blacklisted (finance, shopping, utilities, media).
     */
    fun isBlacklisted(packageName: String, label: String): Boolean {
        val pkgLower = packageName.lowercase(Locale.ROOT)
        val labelLower = label.lowercase(Locale.ROOT)

        if (EXCLUDED_PACKAGES_OR_PREFIXES.any { pkgLower.contains(it) }) return true

        // Disambiguate common collisions (Pocket Casts, Moonlight, Lightroom, Ecobee, BigBasket, Axis, Discover, Walgreens)
        if (pkgLower.contains("pocketcast") || labelLower.contains("pocket casts")) return true
        if (pkgLower.contains("moonlight") || labelLower.contains("moonlight")) return true
        if (pkgLower.contains("lightroom") || labelLower.contains("lightroom")) return true
        if (pkgLower.contains("bigbasket") || labelLower.contains("bigbasket")) return true
        if (pkgLower.contains("ecobee") || labelLower.contains("ecobee")) return true
        if (pkgLower.contains("axis") || labelLower.contains("axis")) return true
        if (pkgLower.contains("discover") || labelLower.contains("discover")) return true
        if (pkgLower.contains("walgreens") || labelLower.contains("walgreens")) return true

        return false
    }

    /**
     * Priority 1: E-Books, Document Readers, Digital Libraries & Audiobooks.
     */
    fun isBookOrReaderApp(app: AppInfo): Boolean {
        if (isBlacklisted(app.packageName, app.label)) return false
        if (KNOWN_BOOK_PACKAGES.contains(app.packageName.lowercase(Locale.ROOT))) return true

        val label = app.label
        val pkg = app.packageName
        return BOOK_PATTERN.matcher(label).find() || BOOK_PATTERN.matcher(pkg).find()
    }

    /**
     * Priority 2: Articles, Longform Read-Later & RSS Feeds.
     */
    fun isArticleOrRssApp(app: AppInfo): Boolean {
        if (isBlacklisted(app.packageName, app.label)) return false
        if (KNOWN_ARTICLE_PACKAGES.contains(app.packageName.lowercase(Locale.ROOT))) return true

        val label = app.label
        val pkg = app.packageName
        return ARTICLE_PATTERN.matcher(label).find() || ARTICLE_PATTERN.matcher(pkg).find()
    }

    /**
     * Priority 3: Text-Based News Apps.
     */
    fun isTextNewsApp(app: AppInfo): Boolean {
        if (isBlacklisted(app.packageName, app.label)) return false
        if (KNOWN_NEWS_PACKAGES.contains(app.packageName.lowercase(Locale.ROOT))) return true

        val label = app.label
        val pkg = app.packageName
        return NEWS_PATTERN.matcher(label).find() || NEWS_PATTERN.matcher(pkg).find()
    }

    /**
     * Strictly verifies if an app belongs anywhere in E-Paper mode.
     * Returns true ONLY if it qualifies for Books, Articles, or News.
     */
    fun isStrictReadingOrNewsApp(app: AppInfo): Boolean {
        return isBookOrReaderApp(app) || isArticleOrRssApp(app) || isTextNewsApp(app)
    }

    fun isReadingOrNewsApp(app: AppInfo): Boolean = isStrictReadingOrNewsApp(app)

    /**
     * Returns the strict priority rank for ordering (1 = Books, 2 = Articles, 3 = News, Int.MAX_VALUE = Not allowed).
     */
    fun getReadingPriorityRank(app: AppInfo): Int {
        return when {
            isBookOrReaderApp(app) -> 1
            isArticleOrRssApp(app) -> 2
            isTextNewsApp(app) -> 3
            else -> Int.MAX_VALUE
        }
    }

    /**
     * Returns clean category tag for display in UI.
     */
    fun getReadingAppCategoryTag(app: AppInfo): String {
        return when {
            app.packageName.equals(GOOGLE_NEWS_PACKAGE, ignoreCase = true) -> "Pre-installed News"
            isBookOrReaderApp(app) -> "E-Book & Docs"
            isArticleOrRssApp(app) -> "Articles & Read Later"
            isTextNewsApp(app) -> "News & Journals"
            else -> "Reading"
        }
    }

    fun isGoogleNews(packageName: String): Boolean {
        return packageName.equals(GOOGLE_NEWS_PACKAGE, ignoreCase = true)
    }
}
