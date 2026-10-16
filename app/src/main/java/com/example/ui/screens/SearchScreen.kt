package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StoryCard
import com.example.ui.components.getAuthorColor
import com.example.ui.viewmodel.BookViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: UiState,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Input Box
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_text_field"),
            placeholder = {
                Text("ابحث باسم القصة أو اسم الكاتب أو كلمة مفتاحية...")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.onSearchQueryChanged("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح البحث"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Author Filter Chips (Horizontal Scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = uiState.selectedAuthorFilter == null,
                onClick = { viewModel.onAuthorFilterSelected(null) },
                label = { Text("جميع الكُتّاب") },
                modifier = Modifier.testTag("filter_author_all")
            )

            uiState.authors.forEach { author ->
                val isSelected = uiState.selectedAuthorFilter == author.id
                val authorColor = getAuthorColor(author.id)

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onAuthorFilterSelected(author.id) },
                    label = { Text(author.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = authorColor.copy(alpha = 0.2f),
                        selectedLabelColor = authorColor
                    ),
                    modifier = Modifier.testTag("filter_author_${author.id}")
                )
            }
        }

        // Chapter Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = uiState.selectedChapterFilter == null,
                onClick = { viewModel.onChapterFilterSelected(null) },
                label = { Text("جميع الفصول") },
                modifier = Modifier.testTag("filter_chapter_all")
            )
            FilterChip(
                selected = uiState.selectedChapterFilter == 1,
                onClick = { viewModel.onChapterFilterSelected(1) },
                label = { Text("ف١: لعبة الكلمات") },
                modifier = Modifier.testTag("filter_chapter_1")
            )
            FilterChip(
                selected = uiState.selectedChapterFilter == 2,
                onClick = { viewModel.onChapterFilterSelected(2) },
                label = { Text("ف٢: لعبة الأشياء") },
                modifier = Modifier.testTag("filter_chapter_2")
            )
            FilterChip(
                selected = uiState.selectedChapterFilter == 3,
                onClick = { viewModel.onChapterFilterSelected(3) },
                label = { Text("ف٣: ألعاب أخرى") },
                modifier = Modifier.testTag("filter_chapter_3")
            )
        }

        // Active filters status row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "النتائج (${uiState.searchResults.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.selectedAuthorFilter != null || uiState.selectedChapterFilter != null || uiState.searchQuery.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearFilters() },
                    modifier = Modifier.testTag("clear_all_filters")
                ) {
                    Text("إعادة ضبط الفلاتر", fontSize = 12.sp)
                }
            }
        }

        // Search Results List or Empty State
        if (uiState.searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لم يتم العثور على أي نصوص مطابقة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "جرب البحث باسم كاتب آخر مثل «حليم» أو «سالي»، أو كلمة مثل «قهوة»، «شاي»، «سقراط».",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.clearFilters() }) {
                        Text("عرض جميع القصص")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.searchResults, key = { it.id }) { story ->
                    StoryCard(
                        story = story,
                        isFavorite = uiState.favoriteStoryIds.contains(story.id),
                        onFavoriteToggle = { viewModel.toggleFavorite(story.id) },
                        onClick = { viewModel.navigateTo(Screen.StoryDetail(story.id)) }
                    )
                }
            }
        }
    }
}
