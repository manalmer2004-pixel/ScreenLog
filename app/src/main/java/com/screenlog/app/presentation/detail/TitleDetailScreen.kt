package com.screenlog.app.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.screenlog.app.core.common.Constants
import com.screenlog.app.presentation.components.LocalContentBadge
import com.screenlog.app.presentation.components.RatingBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleDetailScreen(
    titleType: String,
    tmdbId: String,
    onNavigateToLog: (String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TitleDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(tmdbId) {
        viewModel.loadTitleDetails(titleType, tmdbId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "Failed to load details.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                state.title?.let { title ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Backdrop Image banner
                        if (!title.backdropPath.isNullOrBlank()) {
                            AsyncImage(
                                model = "${Constants.TMDB_IMAGE_BASE_URL}${title.backdropPath}",
                                contentDescription = "Backdrop",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (title.isLocalContent) {
                                    LocalContentBadge()
                                }
                            }

                            val typeString = if (title.titleType == "movie") "Movie" else "TV Series"
                            val releaseYear = title.releaseDate?.take(4) ?: title.firstAirDate?.take(4) ?: "N/A"
                            Text(
                                text = "$typeString • $releaseYear",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToLog(title.titleType, title.tmdbId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Log / Review")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.toggleWatchlist() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (state.isInWatchlist) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (state.isInWatchlist) "Watchlist" else "Add Watchlist")
                                }
                            }

                            // Source
                            if (title.isLocalContent && !title.localSource.isNullOrBlank()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Regional Info",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "This title is in the KFCB local database. Source: ${title.localSource}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            // Genres
                            if (title.genres.isNotEmpty()) {
                                Text(
                                    text = "Genres: " + title.genres.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // Crew
                            if (title.directorNames.isNotEmpty()) {
                                val label = if (title.titleType == "movie") "Directed by: " else "Created by: "
                                Text(
                                    text = label + title.directorNames.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            if (title.castNames.isNotEmpty()) {
                                Text(
                                    text = "Cast: " + title.castNames.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            // Overview
                            Text(
                                text = "Synopsis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = title.overview,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Reviews section
                            Text(
                                text = "Community Reviews",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (state.reviews.isEmpty()) {
                                Text(
                                    text = "No reviews yet. Be the first to add yours!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                state.reviews.forEach { r ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = r.userName,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                RatingBar(rating = r.rating)
                                            }
                                            Text(
                                                text = r.reviewText,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
