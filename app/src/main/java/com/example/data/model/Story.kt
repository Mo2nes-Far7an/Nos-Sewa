package com.example.data.model

data class Story(
    val id: String,
    val title: String,
    val authorId: String,
    val authorName: String,
    val authorFullName: String,
    val chapterIndex: Int,
    val chapterTitle: String,
    val sectionTopic: String,
    val promptWords: List<String> = emptyList(),
    val pageNumber: Int,
    val content: String,
    val wordCount: Int = content.trim().split("\\s+".toRegex()).size,
    val estimatedReadingMinutes: Int = (content.trim().split("\\s+".toRegex()).size / 150).coerceAtLeast(1),
    val excerpt: String = content.take(130).replace("\n", " ").trim() + "..."
)

data class Author(
    val id: String,
    val name: String,
    val fullName: String,
    val bio: String,
    val roleInGroup: String,
    val signatureColor: Long,
    val favoriteQuote: String
)

data class AuthorStats(
    val author: Author,
    val totalStories: Int,
    val chapter1Stories: Int,
    val chapter2Stories: Int,
    val chapter3Stories: Int,
    val totalWords: Int,
    val percentageOfBook: Float,
    val stories: List<Story>
)

data class BookOverviewStats(
    val totalStories: Int,
    val totalAuthors: Int,
    val totalChapters: Int,
    val totalWords: Int,
    val authorStatsList: List<AuthorStats>,
    val chapterCounts: Map<Int, Int>
)

data class UserCreativeDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val promptWords: List<String>,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
