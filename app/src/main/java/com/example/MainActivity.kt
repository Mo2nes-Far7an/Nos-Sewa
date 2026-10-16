package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NossSawaBottomNavigation
import com.example.ui.components.NossSawaTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BookViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Ensure Arabic Right-to-Left Layout Direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // Handle system back button for nested screens
                    BackHandler(enabled = uiState.screenStack.size > 1) {
                        viewModel.navigateBack()
                    }

                    val isTopLevelScreen = uiState.currentScreen is Screen.Home ||
                        uiState.currentScreen is Screen.Search ||
                        uiState.currentScreen is Screen.Statistics ||
                        uiState.currentScreen is Screen.Authors

                    // StoryDetailScreen has its own custom Scaffold for reader mode
                    if (uiState.currentScreen is Screen.StoryDetail) {
                        StoryDetailScreen(
                            storyId = (uiState.currentScreen as Screen.StoryDetail).storyId,
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    } else if (uiState.currentScreen is Screen.WritingStudio) {
                        WritingStudioScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("main_scaffold"),
                            topBar = {
                                val title = when (uiState.currentScreen) {
                                    Screen.Home -> "كتاب نُص سِوا"
                                    Screen.Search -> "البحث في النصوص والكُتّاب"
                                    Screen.Statistics -> "إحصائيات الكتاب"
                                    Screen.Authors -> "الرفاق الستة"
                                    is Screen.AuthorDetail -> uiState.selectedAuthorStats?.author?.fullName ?: "ملف الكاتب"
                                    else -> "نُص سوا"
                                }

                                NossSawaTopBar(
                                    title = title,
                                    canNavigateBack = !isTopLevelScreen,
                                    onNavigateBack = { viewModel.navigateBack() },
                                    actions = {
                                        if (uiState.currentScreen is Screen.Home) {
                                            IconButton(
                                                onClick = { viewModel.navigateTo(Screen.Search) },
                                                modifier = Modifier.testTag("action_search")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Search,
                                                    contentDescription = "بحث"
                                                )
                                            }
                                        }
                                    }
                                )
                            },
                            bottomBar = {
                                if (isTopLevelScreen) {
                                    NossSawaBottomNavigation(
                                        currentScreen = uiState.currentScreen,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            val screenModifier = Modifier.padding(innerPadding)

                            when (val screen = uiState.currentScreen) {
                                Screen.Home -> HomeScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.Search -> SearchScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.Statistics -> StatsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.Authors -> AuthorsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                is Screen.AuthorDetail -> AuthorDetailScreen(
                                    authorId = screen.authorId,
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
