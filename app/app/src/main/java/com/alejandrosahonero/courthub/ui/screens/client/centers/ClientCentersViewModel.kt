package com.alejandrosahonero.courthub.ui.screens.client.centers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.alejandrosahonero.courthub.domain.repository.ISportCenterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

data class ClientCentersUiState(
    val centers: List<SportCenter> = emptyList(),
    val filteredCenters: List<SportCenter> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val sortByDistance: Boolean = false,
    val error: String? = null
)

class ClientCentersViewModel(
    private val sportCenterRepository: ISportCenterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientCentersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCenters()
    }

    private fun loadCenters() {
        viewModelScope.launch {
            sportCenterRepository.getSportCenters().collect { centers ->
                _uiState.update {
                    it.copy(
                        centers = centers,
                        filteredCenters = applyFilter(
                            centers, it.searchQuery,
                            it.sortByDistance, it.userLatitude, it.userLongitude
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCenters = applyFilter(
                    it.centers, query,
                    it.sortByDistance, it.userLatitude, it.userLongitude
                )
            )
        }
    }

    fun onLocationReceived(lat: Double, lng: Double) {
        _uiState.update {
            it.copy(
                userLatitude = lat,
                userLongitude = lng,
                filteredCenters = applyFilter(
                    it.centers, it.searchQuery, it.sortByDistance, lat, lng
                )
            )
        }
    }

    fun toggleSortByDistance() {
        _uiState.update {
            val newSort = !it.sortByDistance
            it.copy(
                sortByDistance = newSort,
                filteredCenters = applyFilter(
                    it.centers, it.searchQuery,
                    newSort, it.userLatitude, it.userLongitude
                )
            )
        }
    }

    private fun applyFilter(
        centers: List<SportCenter>,
        query: String,
        sortByDistance: Boolean,
        lat: Double?,
        lng: Double?
    ): List<SportCenter> {
        var result = centers.filter { it.isEnabled }

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.city.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
            }
        }

        if (sortByDistance && lat != null && lng != null) {
            result = result.sortedBy { center ->
                distanceKm(lat, lng, center.latitude, center.longitude)
            }
        }

        return result
    }

    fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2).pow(2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2).pow(2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    companion object {
        fun factory(sportCenterRepository: ISportCenterRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ClientCentersViewModel(sportCenterRepository) as T
            }
    }
}
