package com.screenlog.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.AnalyticsSummary
import com.screenlog.app.domain.model.LogEntry
import com.screenlog.app.domain.model.User
import com.screenlog.app.domain.model.WatchlistItem
import com.screenlog.app.domain.repository.AnalyticsRepository
import com.screenlog.app.domain.repository.AuthRepository
import com.screenlog.app.domain.repository.LogRepository
import com.screenlog.app.domain.repository.WatchlistRepository
import com.screenlog.app.core.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val analytics: AnalyticsSummary? = null,
    val recentLogs: List<LogEntry> = emptyList(),
    val watchlist: List<WatchlistItem> = emptyList(),
    val isDarkMode: Boolean = false,
    val error: String? = null,
    val passwordUpdateSuccess: Boolean? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val logRepository: LogRepository,
    private val watchlistRepository: WatchlistRepository,
    private val firebaseAuth: FirebaseAuth,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileAndStats()
        observeTheme()
    }

    private fun observeTheme() {
        themePreferences.isDarkMode.onEach { isDark ->
            _uiState.update { it.copy(isDarkMode = isDark) }
        }.launchIn(viewModelScope)
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themePreferences.toggleTheme(isDark)
        }
    }

    private fun loadProfileAndStats() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Recompute latest log stats
            analyticsRepository.computeAnalyticsSummary(uid)

            combine(
                authRepository.currentUser,
                analyticsRepository.getAnalyticsSummaryFlow(uid),
                logRepository.getUserLogsFlow(uid),
                watchlistRepository.getWatchlistFlow(uid)
            ) { user, stats, logs, watchlist ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        analytics = stats,
                        recentLogs = logs,
                        watchlist = watchlist,
                        error = null
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }

    fun deleteLog(logId: String, titleId: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            logRepository.deleteLog(uid, logId, titleId)
            // Analytics will update automatically via the flow
        }
    }

    fun updateLog(log: LogEntry, newRating: Int, newReview: String) {
        viewModelScope.launch {
            val updatedLog = log.copy(
                rating = newRating,
                reviewText = newReview
            )
            logRepository.updateLog(updatedLog)
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, passwordUpdateSuccess = null) }
            val result = authRepository.updatePassword(newPassword)
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, passwordUpdateSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, passwordUpdateSuccess = false, error = result.message) }
            }
        }
    }

    fun resetPasswordUpdateState() {
        _uiState.update { it.copy(passwordUpdateSuccess = null) }
    }
}
