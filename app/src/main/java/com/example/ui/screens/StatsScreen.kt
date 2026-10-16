package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthorStats
import com.example.ui.components.AuthorBarGraph
import com.example.ui.components.StoryCard
import com.example.ui.components.getAuthorColor
import com.example.ui.viewmodel.BookViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiState

@Composable
fun StatsScreen(
    uiState: UiState,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    var selectedAuthorId by remember { mutableStateOf<String?>(null) }

    val authorStatsList = uiState.bookOverviewStats?.authorStatsList ?: emptyList()
    val currentAuthorStat = authorStatsList.find { it.author.id == selectedAuthorId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "الإحصائيات والتحليلات الأدبية",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تحليلات دقيقة لنتاج كُتّاب كتاب «نُص سوا» وتوزيع النصوص والفصول",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Author Filter Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedAuthorId == null,
                    onClick = { selectedAuthorId = null },
                    label = { Text("إحصائيات شاملة") },
                    modifier = Modifier.testTag("stats_tab_all")
                )

                uiState.authors.forEach { author ->
                    val isSelected = selectedAuthorId == author.id
                    val authorColor = getAuthorColor(author.id)

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAuthorId = author.id },
                        label = { Text(author.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = authorColor.copy(alpha = 0.2f),
                            selectedLabelColor = authorColor
                        ),
                        modifier = Modifier.testTag("stats_tab_${author.id}")
                    )
                }
            }
        }

        if (selectedAuthorId == null) {
            // General Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "إجمالي النصوص",
                        value = "${uiState.stories.size}",
                        subtitle = "موزعة على 3 فصول",
                        icon = Icons.Outlined.AutoStories,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "الرفاق الكُتّاب",
                        value = "${uiState.authors.size}",
                        subtitle = "كتابة إبداعية جماعية",
                        icon = Icons.Outlined.Group,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Word Count & Reading Time Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalWords = uiState.stories.sumOf { it.wordCount }
                    val avgWords = if (uiState.stories.isNotEmpty()) totalWords / uiState.stories.size else 0

                    MetricCard(
                        title = "إجمالي الكلمات",
                        value = "$totalWords",
                        subtitle = "بمعدل $avgWords كلمة لكل نص",
                        icon = Icons.Outlined.FormatQuote,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "زمن القراءة التقديري",
                        value = "${(totalWords / 150)} دقيقة",
                        subtitle = "للكتاب كاملاً",
                        icon = Icons.Outlined.Timer,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Chapters Distribution
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "توزيع النصوص على فصول الكتاب الثلاثة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val c1 = uiState.stories.count { it.chapterIndex == 1 }
                        val c2 = uiState.stories.count { it.chapterIndex == 2 }
                        val c3 = uiState.stories.count { it.chapterIndex == 3 }
                        val total = uiState.stories.size.coerceAtLeast(1)

                        ChapterProgressRow("الفصل الأول: لعبة الكلمات", c1, total, MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChapterProgressRow("الفصل الثاني: لعبة الأشياء", c2, total, MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChapterProgressRow("الفصل الثالث: ألعاب أخرى", c3, total, MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            // Comparative Author Bar Graph
            item {
                AuthorBarGraph(
                    statsList = authorStatsList,
                    onAuthorClick = { authorId -> selectedAuthorId = authorId }
                )
            }
        } else if (currentAuthorStat != null) {
            // Detailed Single Author Statistics (Specific Request from User Prompt)
            val author = currentAuthorStat.author
            val authorColor = Color(author.signatureColor)

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = authorColor.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(authorColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = author.name.take(1),
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = author.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = author.roleInGroup,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = authorColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = authorColor.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Numbers row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AuthorMiniStat(
                                label = "إجمالي النصوص",
                                value = "${currentAuthorStat.totalStories}",
                                color = authorColor
                            )
                            AuthorMiniStat(
                                label = "النسبة من الكتاب",
                                value = "${String.format("%.1f", currentAuthorStat.percentageOfBook)}%",
                                color = authorColor
                            )
                            AuthorMiniStat(
                                label = "إجمالي الكلمات",
                                value = "${currentAuthorStat.totalWords}",
                                color = authorColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "مشاركة ${author.name} في فصول الكتاب:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ChapterBadgeStat("الفصل الأول (لعبة الكلمات)", currentAuthorStat.chapter1Stories, authorColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        ChapterBadgeStat("الفصل الثاني (لعبة الأشياء)", currentAuthorStat.chapter2Stories, authorColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        ChapterBadgeStat("الفصل الثالث (ألعاب أخرى)", currentAuthorStat.chapter3Stories, authorColor)

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.AuthorDetail(author.id)) },
                            colors = ButtonDefaults.buttonColors(containerColor = authorColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("عرض الملف التعريفي الكامل للكاتب")
                        }
                    }
                }
            }

            // Stories written by this author
            item {
                Text(
                    text = "جميع نصوص الكاتب ${author.name} (${currentAuthorStat.stories.size} نصوص)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(currentAuthorStat.stories) { story ->
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

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ChapterProgressRow(
    title: String,
    count: Int,
    total: Int,
    color: Color
) {
    val ratio = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(
                text = "$count نصوص (${String.format("%.1f", ratio * 100)}%)",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun AuthorMiniStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChapterBadgeStat(
    chapterName: String,
    count: Int,
    color: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = chapterName, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "$count نصوص",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
