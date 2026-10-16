package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Author
import com.example.data.model.AuthorStats
import com.example.data.model.BookOverviewStats
import com.example.data.model.Story
import com.example.data.model.UserCreativeDraft
import com.example.data.repository.BookDataSource
import com.example.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Search : Screen()
    object Statistics : Screen()
    object Authors : Screen()
    data class AuthorDetail(val authorId: String) : Screen()
    data class StoryDetail(val storyId: String) : Screen()
    object Favorites : Screen()
    object WritingStudio : Screen()
    object AboutBook : Screen()
}

data class UiState(
    val currentScreen: Screen = Screen.Home,
    val screenStack: List<Screen> = listOf(Screen.Home),
    val stories: List<Story> = emptyList(),
    val authors: List<Author> = emptyList(),
    val bookOverviewStats: BookOverviewStats? = null,
    val searchQuery: String = "",
    val selectedAuthorFilter: String? = null,
    val selectedChapterFilter: Int? = null,
    val searchResults: List<Story> = emptyList(),
    val selectedAuthorStats: AuthorStats? = null,
    val selectedStory: Story? = null,
    val favoriteStoryIds: Set<String> = emptySet(),
    val favoriteStories: List<Story> = emptyList(),
    val readerFontSize: Float = 18f,
    val readerPaperMode: String = "sepia", // "light", "sepia", "dark"
    val userDrafts: List<UserCreativeDraft> = emptyList(),
    val studioRandomWords: List<String> = emptyList()
)

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val allStories = repository.getAllStories()
        val allAuthors = repository.getAllAuthors()
        val overview = repository.getBookOverviewStats()
        val favStories = repository.getFavoriteStories()
        val fontSize = repository.getReaderFontSize()
        val paperMode = repository.getReaderPaperMode()
        val drafts = repository.getUserDrafts()

        _uiState.value = _uiState.value.copy(
            stories = allStories,
            authors = allAuthors,
            bookOverviewStats = overview,
            searchResults = allStories,
            favoriteStoryIds = favStories.map { it.id }.toSet(),
            favoriteStories = favStories,
            readerFontSize = fontSize,
            readerPaperMode = paperMode,
            userDrafts = drafts,
            studioRandomWords = generateStudioWords()
        )
    }

    fun navigateTo(screen: Screen) {
        val currentStack = _uiState.value.screenStack.toMutableList()
        currentStack.add(screen)
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            screenStack = currentStack
        )

        // When navigating to details, set the relevant selected models
        when (screen) {
            is Screen.StoryDetail -> {
                _uiState.value = _uiState.value.copy(
                    selectedStory = repository.getStoryById(screen.storyId)
                )
            }
            is Screen.AuthorDetail -> {
                _uiState.value = _uiState.value.copy(
                    selectedAuthorStats = repository.getAuthorStats(screen.authorId)
                )
            }
            else -> {}
        }
    }

    fun navigateBack(): Boolean {
        val currentStack = _uiState.value.screenStack.toMutableList()
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.size - 1)
            val previousScreen = currentStack.last()
            _uiState.value = _uiState.value.copy(
                currentScreen = previousScreen,
                screenStack = currentStack
            )
            return true
        }
        return false
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(searchQuery = newQuery)
        executeSearch()
    }

    fun onAuthorFilterSelected(authorId: String?) {
        val updated = if (_uiState.value.selectedAuthorFilter == authorId) null else authorId
        _uiState.value = _uiState.value.copy(selectedAuthorFilter = updated)
        executeSearch()
    }

    fun onChapterFilterSelected(chapter: Int?) {
        val updated = if (_uiState.value.selectedChapterFilter == chapter) null else chapter
        _uiState.value = _uiState.value.copy(selectedChapterFilter = updated)
        executeSearch()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedAuthorFilter = null,
            selectedChapterFilter = null,
            searchResults = _uiState.value.stories
        )
    }

    private fun executeSearch() {
        val query = _uiState.value.searchQuery
        val authorId = _uiState.value.selectedAuthorFilter
        val chapter = _uiState.value.selectedChapterFilter
        val results = repository.searchStories(query, authorId, chapter)
        _uiState.value = _uiState.value.copy(searchResults = results)
    }

    fun selectAuthorForStats(authorId: String) {
        val stats = repository.getAuthorStats(authorId)
        _uiState.value = _uiState.value.copy(selectedAuthorStats = stats)
    }

    fun openRandomStory() {
        val randomStory = repository.getRandomStory()
        navigateTo(Screen.StoryDetail(randomStory.id))
    }

    fun toggleFavorite(storyId: String) {
        repository.toggleFavorite(storyId)
        val favStories = repository.getFavoriteStories()
        _uiState.value = _uiState.value.copy(
            favoriteStoryIds = favStories.map { it.id }.toSet(),
            favoriteStories = favStories
        )
    }

    fun updateReaderFontSize(newSize: Float) {
        repository.saveReaderFontSize(newSize)
        _uiState.value = _uiState.value.copy(readerFontSize = newSize)
    }

    fun updateReaderPaperMode(mode: String) {
        repository.saveReaderPaperMode(mode)
        _uiState.value = _uiState.value.copy(readerPaperMode = mode)
    }

    fun generateNewStudioPrompt() {
        _uiState.value = _uiState.value.copy(
            studioRandomWords = generateStudioWords()
        )
    }

    private fun generateStudioWords(): List<String> {
        val pool = listOf(
            "قهوة", "شرفة", "قطة", "مطر", "حقيبة", "نظارة", "بلكونة",
            "عطر", "شمس", "قوس قزح", "سفينة", "مكتبة", "حنين", "صدمة",
            "حلم", "شاي بالنعناع", "صوت", "بحر", "طريق", "وردة", "ساعة"
        )
        return pool.shuffled().take(5)
    }

    fun saveDraft(title: String, content: String) {
        viewModelScope.launch {
            val draft = UserCreativeDraft(
                title = title.ifBlank { "نص بدون عنوان" },
                promptWords = _uiState.value.studioRandomWords,
                content = content
            )
            repository.saveUserDraft(draft)
            _uiState.value = _uiState.value.copy(
                userDrafts = repository.getUserDrafts()
            )
        }
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            repository.deleteUserDraft(draftId)
            _uiState.value = _uiState.value.copy(
                userDrafts = repository.getUserDrafts()
            )
        }
    }
}
