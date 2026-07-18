package com.screenlog.app.presentation.log

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.screenlog.app.presentation.components.RatingBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTitleScreen(
    titleType: String,
    tmdbId: String,
    onLogSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: LogTitleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(tmdbId) {
        viewModel.loadTitle(titleType, tmdbId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title?.let { "Log ${it.name}" } ?: "Log Title") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading && state.title == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "How would you rate this?",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Clickable Rating Bar
                    RatingBar(
                        rating = state.rating,
                        onRatingChanged = { viewModel.onRatingChanged(it) },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Review Notes
                    OutlinedTextField(
                        value = state.reviewText,
                        onValueChange = { viewModel.onReviewChanged(it) },
                        label = { Text("Write your review (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 16.dp)
                    )

                    // Language Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Language", style = MaterialTheme.typography.bodyMedium)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.languageCode == "en",
                                onClick = { viewModel.onLanguageChanged("en") },
                                label = { Text("English") }
                            )
                            FilterChip(
                                selected = state.languageCode == "sw",
                                onClick = { viewModel.onLanguageChanged("sw") },
                                label = { Text("Kiswahili") }
                            )
                        }
                    }

                    // Spoilers Switch
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Contains Spoilers", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = state.containsSpoilers,
                            onCheckedChange = { viewModel.onSpoilersChanged(it) }
                        )
                    }

                    if (state.error != null) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Log watch button
                    Button(
                        onClick = { viewModel.submitLog(onLogSuccess) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Save Watch Log")
                        }
                    }
                }
            }
        }
    }
}
