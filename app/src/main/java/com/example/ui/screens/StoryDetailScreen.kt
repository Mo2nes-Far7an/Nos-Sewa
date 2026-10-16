package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Story
import com.example.ui.components.AuthorBadge
import com.example.ui.components.getAuthorColor
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    storyId: String,
    uiState: UiState,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val story = uiState.stories.find { it.id == storyId } ?: uiState.selectedStory

    if (story == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم العثور على القصة")
        }
        return
    }

    val isFav = uiState.favoriteStoryIds.contains(story.id)
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Reading Mode Colors
    val paperColor = when (uiState.readerPaperMode) {
        "light" -> ReadingPaperLight
        "dark" -> ReadingPaperDark
        else -> ReadingPaperSepia
    }
    val textColor = when (uiState.readerPaperMode) {
        "light" -> ReadingTextDark
        "dark" -> ReadingTextLight
        else -> ReadingTextSepia
    }

    val authorColor = getAuthorColor(story.authorId)

    // Current story index for prev/next
    val currentIndex = uiState.stories.indexOfFirst { it.id == story.id }
    val prevStory = if (currentIndex > 0) uiState.stories[currentIndex - 1] else null
    val nextStory = if (currentIndex >= 0 && currentIndex < uiState.stories.size - 1) uiState.stories[currentIndex + 1] else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = story.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("reader_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FormatSize,
                            contentDescription = "إعدادات القراءة"
                        )
                    }
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "«${story.title}» - بقلم ${story.authorFullName}\nمن كتاب نُص سوا (صـ ${story.pageNumber})\n\n${story.content}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة النص"))
                        },
                        modifier = Modifier.testTag("reader_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "مشاركة"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleFavorite(story.id) },
                        modifier = Modifier.testTag("reader_fav_button")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = paperColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor,
                    actionIconContentColor = textColor
                )
            )
        },
        containerColor = paperColor
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("story_detail_content")
        ) {
            // Story Metadata Header
            Surface(
                color = authorColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuthorBadge(
                            authorName = story.authorFullName,
                            color = authorColor,
                            modifier = Modifier.clickable {
                                viewModel.navigateTo(Screen.AuthorDetail(story.authorId))
                            }
                        )

                        Text(
                            text = "الصفحة ${story.pageNumber} من الكتاب",
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${story.chapterTitle} • موضوع: ${story.sectionTopic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = authorColor,
                        fontWeight = FontWeight.Medium
                    )

                    if (story.promptWords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "كلمات وتمرين التحدي:",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            story.promptWords.forEach { word ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = authorColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = authorColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Authentic Story Body Text
            Text(
                text = story.content,
                fontSize = uiState.readerFontSize.sp,
                lineHeight = (uiState.readerFontSize * 1.8f).sp,
                color = textColor,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Copy Story Button
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("نص القصة", "${story.title}\n\n${story.content}")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "تم نسخ النص إلى الحافظة", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("copy_story_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("نسخ النص كاملاً")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Prev / Next Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prevStory != null) {
                    FilledTonalButton(
                        onClick = { viewModel.navigateTo(Screen.StoryDetail(prevStory.id)) },
                        modifier = Modifier.testTag("prev_story_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("السابق: ${prevStory.title.take(12)}", fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                if (nextStory != null) {
                    FilledTonalButton(
                        onClick = { viewModel.navigateTo(Screen.StoryDetail(nextStory.id)) },
                        modifier = Modifier.testTag("next_story_button")
                    ) {
                        Text("التالي: ${nextStory.title.take(12)}", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reader Settings Dialog/BottomSheet
    if (showSettingsSheet) {
        AlertDialog(
            onDismissRequest = { showSettingsSheet = false },
            title = {
                Text(
                    text = "إعدادات القراءة",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "حجم الخط (${uiState.readerFontSize.toInt()} sp)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (uiState.readerFontSize > 14f) {
                                    viewModel.updateReaderFontSize(uiState.readerFontSize - 2f)
                                }
                            }
                        ) {
                            Text("A-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Slider(
                            value = uiState.readerFontSize,
                            onValueChange = { viewModel.updateReaderFontSize(it) },
                            valueRange = 14f..28f,
                            steps = 6,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (uiState.readerFontSize < 28f) {
                                    viewModel.updateReaderFontSize(uiState.readerFontSize + 2f)
                                }
                            }
                        ) {
                            Text("A+", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "لون خلفية القراءة",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PaperColorOption(
                            title = "عتيق (Sepia)",
                            color = ReadingPaperSepia,
                            isSelected = uiState.readerPaperMode == "sepia",
                            onClick = { viewModel.updateReaderPaperMode("sepia") },
                            modifier = Modifier.weight(1f)
                        )
                        PaperColorOption(
                            title = "أبيض (Light)",
                            color = ReadingPaperLight,
                            isSelected = uiState.readerPaperMode == "light",
                            onClick = { viewModel.updateReaderPaperMode("light") },
                            modifier = Modifier.weight(1f)
                        )
                        PaperColorOption(
                            title = "ليلي (Dark)",
                            color = ReadingPaperDark,
                            isSelected = uiState.readerPaperMode == "dark",
                            onClick = { viewModel.updateReaderPaperMode("dark") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsSheet = false }) {
                    Text("تم")
                }
            }
        )
    }
}

@Composable
private fun PaperColorOption(
    title: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = color,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
        ),
        modifier = modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (color == ReadingPaperDark) Color.White else Color.Black
            )
        }
    }
}
