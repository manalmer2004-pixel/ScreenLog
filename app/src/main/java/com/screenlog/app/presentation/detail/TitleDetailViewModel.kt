package com.screenlog.app.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Review
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.TitleRepository
import com.screenlog.app.domain.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TitleDetailUiState(
    val isLoading: Boolean = false,
    val title: Title? = null,
    val reviews: List<Review> = emptyList(),
    val isInWatchlist: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TitleDetailViewModel @Inject constructor(
    private val titleRepository: TitleRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TitleDetailUiState())
    val uiState: StateFlow<TitleDetailUiState> = _uiState.asStateFlow()

    fun loadTitleDetails(titleType: String, tmdbId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 1. Fetch main title details
            val detailResult = titleRepository.getTitleDetails(titleType, tmdbId)
            
            // 2. Check watchlist status
            val inWatchlist = watchlistRepository.isTitleInWatchlist(titleType, tmdbId)

            when (detailResult) {
                is Resource.Success -> {
                    val titleObj = detailResult.data
                    // 3. Load associated reviews
                    val reviewsResult = titleRepository.getTitleReviews(titleObj.id)
                    val reviewsList = if (reviewsResult is Resource.Success) reviewsResult.data else emptyList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = titleObj,
                            reviews = reviewsList,
                            isInWatchlist = inWatchlist
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = detailResult.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleWatchlist() {
        val titleObj = _uiState.value.title ?: return
        viewModelScope.launch {
            if (_uiState.value.isInWatchlist) {
                watchlistRepository.removeFromWatchlist(titleObj.titleType, titleObj.tmdbId)
                _uiState.update { it.copy(isInWatchlist = false) }
            } else {
                watchlistRepository.addToWatchlist(
                    titleType = titleObj.titleType,
                    tmdbId = titleObj.tmdbId,
                    titleName = titleObj.name,
                    posterPath = titleObj.posterPath,
                    releaseDate = titleObj.releaseDate ?: titleObj.firstAirDate
                )
                _uiState.update { it.copy(isInWatchlist = true) }
            }
        }
    }
}
