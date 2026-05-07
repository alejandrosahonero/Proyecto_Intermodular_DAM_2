package com.alejandrosahonero.courthub.ui.screens.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.model.CourtFilter
import com.alejandrosahonero.courthub.domain.model.CourtType
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.alejandrosahonero.courthub.domain.model.User
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.domain.repository.ISportCenterRepository
import com.alejandrosahonero.courthub.domain.usecase.auth.LogoutUseCase
import com.alejandrosahonero.courthub.domain.usecase.court.GetCourtsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientHomeUiState(
    val courts: List<Court> = emptyList(),
    val filteredCourts: List<Court> = emptyList(),
    val centers: List<SportCenter> = emptyList(),
    val selectedCenterId: String? = null,
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val unreadCount: Int = 0,
    val favorites: Set<String> = emptySet(),
    val activeFilter: CourtFilter = CourtFilter.ALL
)

class ClientHomeViewModel(
    private val getCourtsUseCase: GetCourtsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository,
    private val sportCenterRepository: ISportCenterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientHomeUiState())
    val uiState: StateFlow<ClientHomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadCourts()
        loadUnreadCount()
        loadFavorites()
        loadCenters()
    }

    private fun loadCenters() {
        viewModelScope.launch {
            sportCenterRepository.getSportCenters().collect { centers ->
                _uiState.update { it.copy(centers = centers) }
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            authRepository.getFavorites(user.uid)
                .onSuccess { list ->
                    val newFavs = list.toSet()
                    _uiState.update {
                        it.copy(
                            favorites = newFavs,
                            filteredCourts = applyFilter(
                                it.courts,
                                it.searchQuery,
                                it.activeFilter,
                                newFavs,
                                it.selectedCenterId
                            )
                        )
                    }
                }
        }
    }

    fun toggleFavorite(courtId: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            authRepository.toggleFavorite(user.uid, courtId)
                .onSuccess { isNowFavorite ->
                    _uiState.update { state ->
                        val updated = state.favorites.toMutableSet()
                        if (isNowFavorite) updated.add(courtId) else updated.remove(courtId)
                        val newFavs = updated.toSet()
                        state.copy(
                            favorites = newFavs,
                            filteredCourts = applyFilter(
                                state.courts,
                                state.searchQuery,
                                state.activeFilter,
                                newFavs,
                                state.selectedCenterId
                            )
                        )
                    }
                }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.getUnreadCount(user.uid).collect { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    private fun loadCourts() {
        viewModelScope.launch {
            getCourtsUseCase()
                .catch { e -> _uiState.update { it.copy(isLoading = false) } }
                .collect { courts ->
                    _uiState.update {
                        it.copy(
                            courts = courts,
                            filteredCourts = applyFilter(
                                courts,
                                it.searchQuery,
                                it.activeFilter,
                                it.favorites,
                                it.selectedCenterId
                            ),
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    private fun applyFilter(
        courts: List<Court>,
        query: String,
        filter: CourtFilter,
        favorites: Set<String>,
        centerId: String? = null
    ): List<Court> {
        var result = courts.filter { it.isEnabled }

        if (!centerId.isNullOrBlank()) {
            result = result.filter { it.centerId == centerId }
        }

        result = when (filter) {
            CourtFilter.ALL -> result
            CourtFilter.FAVORITES -> result.filter { favorites.contains(it.id) }
            CourtFilter.PRICE_ASC -> result.sortedBy { it.pricePerHour }
            CourtFilter.PRICE_DESC -> result.sortedByDescending { it.pricePerHour }
            CourtFilter.AVAILABLE -> result.filter { it.isEnabled && it.disabledUntil == null }
            CourtFilter.PADEL -> result.filter { it.type == CourtType.PADEL }
            CourtFilter.FUTBOL -> result.filter { it.type == CourtType.FUTBOL }
            CourtFilter.TENIS -> result.filter { it.type == CourtType.TENIS }
            CourtFilter.CRISTAL -> result.filter { it.type == CourtType.CRISTAL }
        }

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.type.value.contains(query, ignoreCase = true)
            }
        }
        return result
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCourts = applyFilter(
                    state.courts, query, state.activeFilter, state.favorites, state.selectedCenterId
                )
            )
        }
    }

    fun onFilterSelected(filter: CourtFilter) {
        _uiState.update {
            it.copy(
                activeFilter = filter,
                filteredCourts = applyFilter(
                    it.courts,
                    it.searchQuery,
                    filter,
                    it.favorites,
                    it.selectedCenterId
                )
            )
        }
    }

    fun onCenterFilterSelected(centerId: String?) {
        _uiState.update {
            it.copy(
                selectedCenterId = centerId,
                filteredCourts = applyFilter(
                    it.courts, it.searchQuery, it.activeFilter,
                    it.favorites, centerId
                )
            )
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onSuccess()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            getCourtsUseCase()
                .take(1)
                .collect { courts ->
                    _uiState.update {
                        it.copy(
                            courts = courts,
                            filteredCourts = applyFilter(
                                courts,
                                it.searchQuery,
                                it.activeFilter,
                                it.favorites,
                                it.selectedCenterId
                            ),
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(
            getCourtsUseCase: GetCourtsUseCase,
            logoutUseCase: LogoutUseCase,
            authRepository: IAuthRepository,
            notificationRepository: INotificationRepository,
            sportCenterRepository: ISportCenterRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ClientHomeViewModel(
                    getCourtsUseCase,
                    logoutUseCase,
                    authRepository,
                    notificationRepository,
                    sportCenterRepository
                ) as T
        }
    }
}