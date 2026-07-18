package com.screenlog.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.TitleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val localOnly: Boolean = false,
    val results: List<Title> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val titleRepository: TitleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        if (newQuery.isNotBlank()) {
            search()
        }
    }

    fun onLocalOnlyToggle(enabled: Boolean) {
        _uiState.update { it.copy(localOnly = enabled) }
        search()
    }

    private fun search() {
        val query = _uiState.value.query
        val localOnly = _uiState.value.localOnly
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = titleRepository.searchTitles(query, null, localOnly)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, results = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
