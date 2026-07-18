package com.screenlog.app.presentation.profile

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.screenlog.app.domain.model.LogEntry
import com.screenlog.app.domain.model.Title
import com.screenlog.app.presentation.components.RatingBar
import com.screenlog.app.presentation.components.TitleCard

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onNavigateToDetail: (String, String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var editingLog by remember { mutableStateOf<LogEntry?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }

    if (editingLog != null) {
        EditLogDialog(
            log = editingLog!!,
            onDismiss = { editingLog = null },
            onConfirm = { rating: Int, review: String ->
                viewModel.updateLog(editingLog!!, rating, review)
                editingLog = null
            }
        )
    }

    if (isChangingPassword) {
        ChangePasswordDialog(
            onDismiss = { isChangingPassword = false },
            onConfirm = { newPassword: String ->
                viewModel.updatePassword(newPassword)
                isChangingPassword = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "My Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // User profile Card
        state.user?.let { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Home Region: " + when (user.homeCountry) {
                            "KE" -> "Kenya"
                            "TZ" -> "Tanzania"
                            "UG" -> "Uganda"
                            "RW" -> "Rwanda"
                            else -> user.homeCountry
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section: Watchlist
        if (state.watchlist.isNotEmpty()) {
            Text(
                text = "My Watchlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                state.watchlist.forEach { item ->
                    val titleObj = Title(
                        id = item.titleId,
                        tmdbId = item.tmdbId,
                        titleType = item.titleType,
                        name = item.titleName,
                        overview = "",
                        posterPath = item.posterPath,
                        backdropPath = null,
                        releaseDate = item.releaseDate,
                        firstAirDate = item.releaseDate,
                        originCountry = emptyList(),
                        genres = emptyList(),
                        runtimeMinutes = null,
                        directorNames = emptyList(),
                        castNames = emptyList(),
                        isLocalContent = false,
                        localSource = null,
                        averageRating = 0.0,
                        ratingCount = 0,
                        createdAt = 0,
                        updatedAt = 0
                    )
                    TitleCard(
                        title = titleObj,
                        onClick = { onNavigateToDetail(item.titleType, item.tmdbId) },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
        }

        // Section: Recent Logs
        Text(
            text = "My Recent Logs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (state.recentLogs.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No logs yet. Your cinematic journey starts here!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            state.recentLogs.forEach { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = log.titleName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            RatingBar(
                                rating = log.rating,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (!log.reviewText.isNullOrBlank()) {
                            Text(
                                text = log.reviewText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { editingLog = log }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit", style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = { viewModel.deleteLog(log.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Theme Toggle Section
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = state.isDarkMode,
                    onCheckedChange = { viewModel.toggleTheme(it) }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Account Security",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = { isChangingPassword = true }) {
                    Text("Change Password")
                }
            }
        }

        if (state.passwordUpdateSuccess == true) {
            Text(
                text = "Password updated successfully!",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000L)
                viewModel.resetPasswordUpdateState()
            }
        } else if (state.passwordUpdateSuccess == false) {
            Text(
                text = state.error ?: "Failed to update password",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Logout action gate
        Button(
            onClick = { viewModel.signOut(onSignOut) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EditLogDialog(
    log: LogEntry,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(log.rating) }
    var reviewText by remember { mutableStateOf(log.reviewText ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Log: ${log.titleName}") },
        text = {
            Column {
                Text("Rating", style = MaterialTheme.typography.labelMedium)
                RatingBar(
                    rating = rating,
                    onRatingChanged = { rating = it },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Review Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(rating, reviewText) }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword.length < 6) {
                        error = "Password must be at least 6 characters"
                    } else if (newPassword != confirmPassword) {
                        error = "Passwords do not match"
                    } else {
                        onConfirm(newPassword)
                    }
                }
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
