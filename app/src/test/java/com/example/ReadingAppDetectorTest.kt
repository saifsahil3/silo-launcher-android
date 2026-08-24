package com.example

import android.content.Intent
import com.example.data.AppInfo
import com.example.data.ReadingAppDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingAppDetectorTest {

    private fun createApp(label: String, packageName: String, category: String = "General"): AppInfo {
        return AppInfo(
            label = label,
            packageName = packageName,
            launchIntent = Intent(),
            category = category
        )
    }

    @Test
    fun testRejectsReportedBugAppsStrictly() {
        // Verify apps specifically mentioned in bug report are 100% rejected
        val bigbasket = createApp("BigBasket", "com.bigbasket.mobileapp")
        val axis = createApp("Axis Mobile", "com.axis.mobile")
        val axisPay = createApp("Axis Pay", "com.upi.axispay")
        val discover = createApp("Discover Mobile", "com.discoverfinancial.mobile")
        val ecobee = createApp("ecobee", "com.ecobee.athena")
        val lightroom = createApp("Adobe Lightroom", "com.adobe.lrmobile")
        val walgreens = createApp("Walgreens", "com.walgreens.android")
        val pocketCasts = createApp("Pocket Casts", "au.com.shiftyjelly.pocketcasts")
        val moonlight = createApp("Moonlight", "com.limelight")
        val whatsapp = createApp("WhatsApp", "com.whatsapp")
        val spotify = createApp("Spotify", "com.spotify.music")
        val camera = createApp("Camera", "com.google.android.GoogleCamera")
        val settings = createApp("Settings", "com.android.settings")

        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(bigbasket))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(axis))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(axisPay))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(discover))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(ecobee))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(lightroom))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(walgreens))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(pocketCasts))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(moonlight))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(whatsapp))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(spotify))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(camera))
        assertFalse(ReadingAppDetector.isStrictReadingOrNewsApp(settings))
    }

    @Test
    fun testTier1BooksHighPriority() {
        val kindle = createApp("Amazon Kindle", "com.amazon.kindle")
        val playBooks = createApp("Google Play Books", "com.google.android.apps.books")
        val readEra = createApp("ReadEra", "org.readera")
        val moonReader = createApp("Moon+ Reader", "com.flyersoft.moonreaderp")
        val libby = createApp("Libby", "com.overdrive.mobile.android.libby")
        val kobo = createApp("Kobo Books", "com.kobo.epub")
        val mihon = createApp("Mihon", "xyz.mihon")
        val eboox = createApp("eBoox", "com.reader.books")
        val koreader = createApp("KOReader", "org.koreader.launcher")
        val pocketbook = createApp("PocketBook", "com.obreey.reader")
        val librera = createApp("Librera", "com.foobnix.pdf.reader")

        assertTrue(ReadingAppDetector.isBookOrReaderApp(kindle))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(playBooks))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(readEra))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(moonReader))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(libby))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(kobo))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(mihon))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(eboox))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(koreader))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(pocketbook))
        assertTrue(ReadingAppDetector.isBookOrReaderApp(librera))

        assertEquals(1, ReadingAppDetector.getReadingPriorityRank(kindle))
        assertEquals(1, ReadingAppDetector.getReadingPriorityRank(eboox))
        assertEquals(1, ReadingAppDetector.getReadingPriorityRank(readEra))
        assertEquals("E-Book & Docs", ReadingAppDetector.getReadingAppCategoryTag(eboox))
    }

    @Test
    fun testTier2ArticlesAndRss() {
        val pocket = createApp("Pocket", "com.ideashower.readitlater.pro")
        val medium = createApp("Medium", "com.medium.reader")
        val feedly = createApp("Feedly", "com.devhd.feedly")
        val inoreader = createApp("Inoreader", "com.innologica.inoreader")

        assertTrue(ReadingAppDetector.isArticleOrRssApp(pocket))
        assertTrue(ReadingAppDetector.isArticleOrRssApp(medium))
        assertTrue(ReadingAppDetector.isArticleOrRssApp(feedly))
        assertTrue(ReadingAppDetector.isArticleOrRssApp(inoreader))

        assertEquals(2, ReadingAppDetector.getReadingPriorityRank(pocket))
        assertEquals("Articles & Read Later", ReadingAppDetector.getReadingAppCategoryTag(pocket))
    }

    @Test
    fun testTier3News() {
        val googleNews = createApp("Google News", "com.google.android.apps.magazines")
        val nytimes = createApp("The New York Times", "com.nytimes.android")
        val bbcNews = createApp("BBC News", "bbc.mobile.news.ww")

        assertTrue(ReadingAppDetector.isTextNewsApp(googleNews))
        assertTrue(ReadingAppDetector.isTextNewsApp(nytimes))
        assertTrue(ReadingAppDetector.isTextNewsApp(bbcNews))

        assertEquals(3, ReadingAppDetector.getReadingPriorityRank(nytimes))
        assertEquals("Pre-installed News", ReadingAppDetector.getReadingAppCategoryTag(googleNews))
    }

    @Test
    fun testPriorityRankingOrder() {
        val kindle = createApp("Kindle", "com.amazon.kindle")
        val pocket = createApp("Pocket", "com.ideashower.readitlater.pro")
        val nytimes = createApp("The New York Times", "com.nytimes.android")
        val randomApp = createApp("Random App", "com.random.app")

        val apps = listOf(nytimes, randomApp, kindle, pocket)
        val sorted = apps.filter { ReadingAppDetector.isStrictReadingOrNewsApp(it) }
            .sortedBy { ReadingAppDetector.getReadingPriorityRank(it) }

        assertEquals(listOf(kindle, pocket, nytimes), sorted)
    }
}
