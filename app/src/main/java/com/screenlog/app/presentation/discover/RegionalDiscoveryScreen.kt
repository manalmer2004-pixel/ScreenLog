package com.screenlog.app.presentation.discover

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.screenlog.app.presentation.components.TitleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionalDiscoveryScreen(
    onNavigateToDetail: (String, String) -> Unit,
    viewModel: RegionalDiscoveryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Regional Spotlight",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Failed to load data.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            // Locally Produced (Kenya)
            Text(
                text = "Locally Produced",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (state.locallyProduced.isEmpty()) {
                Text(
                    text = "No locally produced titles found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    state.locallyProduced.forEach { title ->
                        TitleCard(
                            title = title,
                            onClick = { onNavigateToDetail(title.titleType, title.tmdbId) },
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            }

            // Regionally Produced (East Africa)
            Text(
                text = "Regionally Produced",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (state.regionallyProduced.isEmpty()) {
                Text(
                    text = "No regionally produced titles found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    state.regionallyProduced.forEach { title ->
                        TitleCard(
                            title = title,
                            onClick = { onNavigateToDetail(title.titleType, title.tmdbId) },
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
