package com.screenlog.app.presentation.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.LogRepository
import com.screenlog.app.domain.repository.TitleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LogTitleUiState(
    val isLoading: Boolean = false,
    val title: Title? = null,
    val rating: Int = 3,
    val reviewText: String = "",
    val languageCode: String = "en",
    val containsSpoilers: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LogTitleViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val titleRepository: TitleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogTitleUiState())
    val uiState: StateFlow<LogTitleUiState> = _uiState.asStateFlow()

    fun loadTitle(titleType: String, tmdbId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = titleRepository.getTitleDetails(titleType, tmdbId)
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, title = result.data) }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onRatingChanged(rating: Int) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun onReviewChanged(text: String) {
        _uiState.update { it.copy(reviewText = text) }
    }

    fun onLanguageChanged(code: String) {
        _uiState.update { it.copy(languageCode = code) }
    }

    fun onSpoilersChanged(contains: Boolean) {
        _uiState.update { it.copy(containsSpoilers = contains) }
    }

    fun submitLog(onSuccess: () -> Unit) {
        val titleObj = _uiState.value.title ?: return
        val sharedId = UUID.randomUUID().toString()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Log title to local Room first, triggering background firestore sync
            val result = logRepository.logTitle(
                titleType = titleObj.titleType,
                tmdbId = titleObj.tmdbId,
                titleName = titleObj.name,
                posterPath = titleObj.posterPath,
                releaseDate = titleObj.releaseDate ?: titleObj.firstAirDate,
                rating = _uiState.value.rating,
                reviewText = _uiState.value.reviewText,
                languageCode = _uiState.value.languageCode,
                containsSpoilers = _uiState.value.containsSpoilers,
                watchedDate = System.currentTimeMillis(),
                logId = sharedId
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSubmitted = true) }
                    
                    // Also submit the review to Firestore as a public post if they wrote text
                    if (_uiState.value.reviewText.isNotBlank()) {
                        titleRepository.submitReview(
                            titleId = titleObj.id,
                            rating = _uiState.value.rating,
                            text = _uiState.value.reviewText,
                            language = _uiState.value.languageCode,
                            containsSpoilers = _uiState.value.containsSpoilers,
                            reviewId = sharedId
                        )
                    }
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
