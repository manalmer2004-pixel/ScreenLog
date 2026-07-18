package com.screenlog.app.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Filmmaker
import com.screenlog.app.domain.model.Region
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.RegionalDiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegionalDiscoveryUiState(
    val isLoading: Boolean = false,
    val locallyProduced: List<Title> = emptyList(),
    val regionallyProduced: List<Title> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RegionalDiscoveryViewModel @Inject constructor(
    private val discoveryRepository: RegionalDiscoveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegionalDiscoveryUiState())
    val uiState: StateFlow<RegionalDiscoveryUiState> = _uiState.asStateFlow()

    init {
        loadDiscoveryData()
    }

    private fun loadDiscoveryData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val localResult = discoveryRepository.getLocallyProduced()
            val regionalResult = discoveryRepository.getRegionallyProduced()

            if (localResult is Resource.Success && regionalResult is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        locallyProduced = localResult.data,
                        regionallyProduced = regionalResult.data
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load discovery data."
                    )
                }
            }
        }
    }
}
