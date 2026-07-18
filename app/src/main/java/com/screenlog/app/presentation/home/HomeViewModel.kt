package com.screenlog.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.AnalyticsSummary
import com.screenlog.app.domain.model.LogEntry
import com.screenlog.app.domain.model.Recommendation
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val recommendations: List<Recommendation> = emptyList(),
    val trendingKE: List<Title> = emptyList(),
    val lastLogged: LogEntry? = null,
    val analytics: AnalyticsSummary? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val regionalDiscoveryRepository: RegionalDiscoveryRepository,
    private val titleRepository: TitleRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val logRepository: LogRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Trigger recommendations and analytics refresh
            recommendationRepository.refreshRecommendations(uid)
            analyticsRepository.computeAnalyticsSummary(uid)

            // Flow for personalized "For You" content using TMDB Genre IDs
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            val forYouFlow = analyticsRepository.getAnalyticsSummaryFlow(uid)
                .map { it?.topGenres?.firstOrNull()?.first }
                .distinctUntilChanged()
                .flatMapLatest { topGenreName ->
                    flow {
                        val genreId = when (topGenreName?.lowercase()?.trim()) {
                            "action" -> "28"
                            "adventure" -> "12"
                            "animation" -> "16"
                            "comedy" -> "35"
                            "crime" -> "80"
                            "documentary" -> "99"
                            "drama" -> "18"
                            "family" -> "10751"
                            "fantasy" -> "14"
                            "history" -> "36"
                            "horror" -> "27"
                            "music" -> "10402"
                            "mystery" -> "9648"
                            "romance" -> "10749"
                            "science fiction", "sci-fi", "science-fiction" -> "878"
                            "tv movie", "tv-movie" -> "10770"
                            "thriller" -> "53"
                            "war" -> "10752"
                            "western" -> "37"
                            "action & adventure", "action-adventure" -> "10759" // TV Action & Adventure
                            "sci-fi & fantasy", "sci-fi-fantasy" -> "10765" // TV Sci-Fi & Fantasy
                            "war & politics" -> "10768" // TV War & Politics
                            "soap" -> "10766"
                            "reality" -> "10764"
                            "talk" -> "10767"
                            "news" -> "10763"
                            else -> null
                        }

                        val titles = if (genreId != null) {
                            val moviesResult = titleRepository.getMoviesByGenre(genreId)
                            val tvResult = titleRepository.getTvShowsByGenre(genreId)
                            
                            val movies = if (moviesResult is Resource.Success) moviesResult.data else emptyList()
                            val tvShows = if (tvResult is Resource.Success) tvResult.data else emptyList()
                            
                            // Interleave movies and TV shows
                            val combined = mutableListOf<Title>()
                            val maxSize = maxOf(movies.size, tvShows.size)
                            for (i in 0 until maxSize) {
                                if (i < movies.size) combined.add(movies[i])
                                if (i < tvShows.size) combined.add(tvShows[i])
                            }
                            
                            if (combined.isEmpty()) {
                                // Fallback to trending Kenyan movies if genre search fails
                                val result = regionalDiscoveryRepository.getTopRatedInRegion("KE")
                                if (result is Resource.Success) result.data.filter { it.titleType == "movie" } else emptyList()
                            } else {
                                combined
                            }
                        } else {
                            // No logs yet, don't show recommendations
                            emptyList()
                        }
                        emit(titles)
                    }
                }

            // Combine all flows
            combine(
                recommendationRepository.getRecommendationsFlow(uid),
                analyticsRepository.getAnalyticsSummaryFlow(uid),
                logRepository.getUserLogsFlow(uid),
                forYouFlow
            ) { recommendations, analytics, logs, forYouTitles ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recommendations = recommendations,
                        analytics = analytics,
                        lastLogged = logs.maxByOrNull { log -> log.watchedDate },
                        trendingKE = forYouTitles,
                        error = null
                    )
                }
            }.collect()
        }
    }

    fun syncPendingLogs() {
        viewModelScope.launch {
            logRepository.syncPendingLogs()
        }
    }
}
