package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.BookViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingStudioScreen(
    uiState: UiState,
    viewModel: BookViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    var storyTitle by remember { mutableStateOf("") }
    var storyBody by remember { mutableStateOf("") }

    // Writing Timer
    var isTimerRunning by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableStateOf(600) } // 10 minutes default

    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (remainingSeconds == 0 && isTimerRunning) {
            isTimerRunning = false
            Toast.makeText(context, "انتهى وقت تمرين الكتابة!", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ورشة كتابة نص سوا", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("writing_studio_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Banner
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "لعبة الكلمات مع الرفاق الستة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "مثلما كان يجتمع الرفاق أسبوعياً لاقتراح كلمات عشوائية وكتابة نص في دقائق معدودة، وظّف هذه الكلمات في نصك الخاص وكن سابع الرفاق!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Word challenge prompt cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "كلمات تمرينك الحالي:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.generateNewStudioPrompt() }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "تغيير الكلمات")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.studioRandomWords.forEach { word ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(word, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Timer bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val mins = remainingSeconds / 60
                            val secs = remainingSeconds % 60
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (remainingSeconds < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            Row {
                                TextButton(onClick = {
                                    isTimerRunning = !isTimerRunning
                                }) {
                                    Text(if (isTimerRunning) "إيقاف مؤقت" else "بدء التحدي")
                                }
                                TextButton(onClick = {
                                    isTimerRunning = false
                                    remainingSeconds = 600
                                }) {
                                    Text("إعادة ضبط")
                                }
                            }
                        }
                    }
                }
            }

            // Writing Area
            item {
                OutlinedTextField(
                    value = storyTitle,
                    onValueChange = { storyTitle = it },
                    label = { Text("عنوان نصك") },
                    placeholder = { Text("مثال: في شرفة المقهى...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("draft_title_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = storyBody,
                    onValueChange = { storyBody = it },
                    label = { Text("اكتب نصك هنا...") },
                    placeholder = { Text("ابدأ بكتابة أفكارك ومحاولة توظيف الكلمات المقترحة...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("draft_body_field"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Save draft button
            item {
                Button(
                    onClick = {
                        if (storyBody.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة نص أولاً قبل الحفظ", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveDraft(storyTitle, storyBody)
                            storyTitle = ""
                            storyBody = ""
                            Toast.makeText(context, "تم حفظ نصك بنجاح في مجموعتك!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_draft_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ النص في كتابي", fontWeight = FontWeight.Bold)
                }
            }

            // Saved drafts
            if (uiState.userDrafts.isNotEmpty()) {
                item {
                    Text(
                        text = "نصوصك المحفوظة (${uiState.userDrafts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.userDrafts) { draft ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = draft.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.deleteDraft(draft.id) }) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "حذف",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Text(
                                text = draft.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}
