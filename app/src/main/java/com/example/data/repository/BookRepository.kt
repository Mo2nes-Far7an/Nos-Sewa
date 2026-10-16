package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Author
import com.example.data.model.AuthorStats
import com.example.data.model.BookOverviewStats
import com.example.data.model.Story
import com.example.data.model.UserCreativeDraft
import org.json.JSONArray
import org.json.JSONObject

class BookRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("noss_sawa_prefs", Context.MODE_PRIVATE)

    fun getAllStories(): List<Story> = BookDataSource.stories

    fun getAllAuthors(): List<Author> = BookDataSource.authors

    fun getAuthorById(authorId: String): Author? =
        BookDataSource.authors.find { it.id == authorId }

    fun getStoryById(storyId: String): Story? =
        BookDataSource.stories.find { it.id == storyId }

    fun getRandomStory(): Story =
        BookDataSource.stories.random()

    fun getStoriesByAuthor(authorId: String): List<Story> =
        BookDataSource.stories.filter { it.authorId == authorId }

    fun getStoriesByChapter(chapterIndex: Int): List<Story> =
        BookDataSource.stories.filter { it.chapterIndex == chapterIndex }

    fun searchStories(query: String, authorId: String? = null, chapter: Int? = null): List<Story> {
        val trimmed = query.trim()
        return BookDataSource.stories.filter { story ->
            val matchesAuthor = authorId.isNullOrBlank() || story.authorId == authorId
            val matchesChapter = chapter == null || story.chapterIndex == chapter
            val matchesQuery = if (trimmed.isEmpty()) {
                true
            } else {
                story.title.contains(trimmed, ignoreCase = true) ||
                    story.authorName.contains(trimmed, ignoreCase = true) ||
                    story.authorFullName.contains(trimmed, ignoreCase = true) ||
                    story.sectionTopic.contains(trimmed, ignoreCase = true) ||
                    story.promptWords.any { it.contains(trimmed, ignoreCase = true) } ||
                    story.content.contains(trimmed, ignoreCase = true)
            }
            matchesAuthor && matchesChapter && matchesQuery
        }
    }

    // Statistics Calculations
    fun getAuthorStats(authorId: String): AuthorStats? {
        val author = getAuthorById(authorId) ?: return null
        val authorStories = getStoriesByAuthor(authorId)
        val allStoriesCount = BookDataSource.stories.size.coerceAtLeast(1)

        val c1Count = authorStories.count { it.chapterIndex == 1 }
        val c2Count = authorStories.count { it.chapterIndex == 2 }
        val c3Count = authorStories.count { it.chapterIndex == 3 }
        val totalWords = authorStories.sumOf { it.wordCount }
        val percentage = (authorStories.size.toFloat() / allStoriesCount.toFloat()) * 100f

        return AuthorStats(
            author = author,
            totalStories = authorStories.size,
            chapter1Stories = c1Count,
            chapter2Stories = c2Count,
            chapter3Stories = c3Count,
            totalWords = totalWords,
            percentageOfBook = percentage,
            stories = authorStories
        )
    }

    fun getAllAuthorStats(): List<AuthorStats> {
        return BookDataSource.authors.mapNotNull { author ->
            getAuthorStats(author.id)
        }.sortedByDescending { it.totalStories }
    }

    fun getBookOverviewStats(): BookOverviewStats {
        val allStories = BookDataSource.stories
        val authorStats = getAllAuthorStats()
        val chapterCounts = mapOf(
            1 to allStories.count { it.chapterIndex == 1 },
            2 to allStories.count { it.chapterIndex == 2 },
            3 to allStories.count { it.chapterIndex == 3 }
        )
        val totalWords = allStories.sumOf { it.wordCount }

        return BookOverviewStats(
            totalStories = allStories.size,
            totalAuthors = BookDataSource.authors.size,
            totalChapters = 3,
            totalWords = totalWords,
            authorStatsList = authorStats,
            chapterCounts = chapterCounts
        )
    }

    // Favorites persistence
    fun isFavorite(storyId: String): Boolean {
        val favs = prefs.getStringSet("favorite_story_ids", emptySet()) ?: emptySet()
        return favs.contains(storyId)
    }

    fun toggleFavorite(storyId: String): Boolean {
        val favs = prefs.getStringSet("favorite_story_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        val newState = if (favs.contains(storyId)) {
            favs.remove(storyId)
            false
        } else {
            favs.add(storyId)
            true
        }
        prefs.edit().putStringSet("favorite_story_ids", favs).apply()
        return newState
    }

    fun getFavoriteStories(): List<Story> {
        val favs = prefs.getStringSet("favorite_story_ids", emptySet()) ?: emptySet()
        return BookDataSource.stories.filter { favs.contains(it.id) }
    }

    // Reader Preferences (Font size, Paper color mode)
    fun getReaderFontSize(): Float = prefs.getFloat("reader_font_size", 18f)

    fun saveReaderFontSize(size: Float) {
        prefs.edit().putFloat("reader_font_size", size).apply()
    }

    fun getReaderPaperMode(): String = prefs.getString("reader_paper_mode", "sepia") ?: "sepia"

    fun saveReaderPaperMode(mode: String) {
        prefs.edit().putString("reader_paper_mode", mode).apply()
    }

    // User Creative Writing Drafts
    fun getUserDrafts(): List<UserCreativeDraft> {
        val jsonString = prefs.getString("user_drafts_json", "[]") ?: "[]"
        val list = mutableListOf<UserCreativeDraft>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val wordsArray = obj.getJSONArray("promptWords")
                val words = mutableListOf<String>()
                for (j in 0 until wordsArray.length()) {
                    words.add(wordsArray.getString(j))
                }
                list.add(
                    UserCreativeDraft(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        promptWords = words,
                        content = obj.getString("content"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    fun saveUserDraft(draft: UserCreativeDraft) {
        val currentDrafts = getUserDrafts().filter { it.id != draft.id }.toMutableList()
        currentDrafts.add(0, draft)
        val jsonArray = JSONArray()
        for (d in currentDrafts) {
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("title", d.title)
            obj.put("content", d.content)
            obj.put("createdAt", d.createdAt)
            val wArr = JSONArray()
            d.promptWords.forEach { wArr.put(it) }
            obj.put("promptWords", wArr)
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_drafts_json", jsonArray.toString()).apply()
    }

    fun deleteUserDraft(draftId: String) {
        val filtered = getUserDrafts().filter { it.id != draftId }
        val jsonArray = JSONArray()
        for (d in filtered) {
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("title", d.title)
            obj.put("content", d.content)
            obj.put("createdAt", d.createdAt)
            val wArr = JSONArray()
            d.promptWords.forEach { wArr.put(it) }
            obj.put("promptWords", wArr)
            jsonArray.put(obj)
        }
        prefs.edit().putString("user_drafts_json", jsonArray.toString()).apply()
    }
}
