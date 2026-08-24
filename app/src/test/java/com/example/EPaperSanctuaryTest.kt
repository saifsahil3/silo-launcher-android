package com.example

import com.example.db.OfflineBookEntity
import com.example.ui.theme.EPaperColorProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EPaperSanctuaryTest {

    @Test
    fun testEPaperColorProfileCycling() {
        val p1 = EPaperColorProfile.PAPER_WHITE
        val p2 = p1.next()
        val p3 = p2.next()
        val p4 = p3.next()
        val p5 = p4.next()

        assertEquals(EPaperColorProfile.WARM_AMBER, p2)
        assertEquals(EPaperColorProfile.SLATE_CHARCOAL, p3)
        assertEquals(EPaperColorProfile.HIGH_CONTRAST, p4)
        assertEquals(EPaperColorProfile.PAPER_WHITE, p5)
    }

    @Test
    fun testOfflineBookEntityProgressCalculation() {
        val book = OfflineBookEntity(
            id = 1L,
            title = "Dune",
            author = "Frank Herbert",
            uriString = "content://books/dune.epub",
            mimeType = "application/epub+zip",
            formatBadge = "EPUB",
            currentPage = 142,
            totalPages = 320
        )

        assertEquals(44, book.progressPercentage)
        assertEquals("Dune", book.title)
        assertEquals("Frank Herbert", book.author)
        assertEquals("EPUB", book.formatBadge)
        assertFalse(book.isCompleted)
    }

    @Test
    fun testOfflineBookCompletionAndZeroPages() {
        val bookZero = OfflineBookEntity(
            id = 2L,
            title = "Notes",
            uriString = "content://docs/notes.txt",
            mimeType = "text/plain",
            currentPage = 0,
            totalPages = 0
        )
        assertEquals(0, bookZero.progressPercentage)

        val completedBook = OfflineBookEntity(
            id = 3L,
            title = "Meditations",
            author = "Marcus Aurelius",
            uriString = "content://books/meditations.pdf",
            mimeType = "application/pdf",
            currentPage = 200,
            totalPages = 200,
            isCompleted = true
        )
        assertEquals(100, completedBook.progressPercentage)
        assertTrue(completedBook.isCompleted)
    }
}
