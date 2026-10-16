package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.BookDataSource
import com.example.data.repository.BookRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var repository: BookRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        repository = BookRepository(context)
    }

    @Test
    fun `read string from context matches book name`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("نُص سوا", appName)
    }

    @Test
    fun `verify authors list contains the six companions`() {
        val authors = repository.getAllAuthors()
        assertEquals(6, authors.size)
        val names = authors.map { it.name }
        assertTrue(names.contains("سالي"))
        assertTrue(names.contains("حليم"))
        assertTrue(names.contains("محمد"))
        assertTrue(names.contains("شيرين"))
        assertTrue(names.contains("إنجي"))
        assertTrue(names.contains("مؤنس"))
    }

    @Test
    fun `search by story title returns matching stories`() {
        val results = repository.searchStories(query = "صرخة طفل")
        assertTrue(results.isNotEmpty())
        assertEquals("صرخة طفل", results.first().title)
    }

    @Test
    fun `search by author name returns their stories`() {
        val authorFiltered = repository.searchStories(query = "", authorId = "sally")
        assertTrue(authorFiltered.isNotEmpty())
        assertTrue(authorFiltered.all { it.authorId == "sally" })

        val results = repository.searchStories(query = "سالي")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `author statistics calculate story counts correctly`() {
        val sallyStats = repository.getAuthorStats("sally")
        assertNotNull(sallyStats)
        assertTrue(sallyStats!!.totalStories > 0)
        assertTrue(sallyStats.totalWords > 0)

        val overview = repository.getBookOverviewStats()
        assertEquals(6, overview.totalAuthors)
        assertEquals(3, overview.totalChapters)
        assertTrue(overview.totalStories >= 30)
    }
}
