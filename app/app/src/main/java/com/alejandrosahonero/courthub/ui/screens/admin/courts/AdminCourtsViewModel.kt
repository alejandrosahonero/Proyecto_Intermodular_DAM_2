package com.alejandrosahonero.courthub.ui.screens.admin.courts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alejandrosahonero.courthub.domain.model.AdminCourtFilter
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.model.CourtType
import com.alejandrosahonero.courthub.domain.model.SportCenter
import com.alejandrosahonero.courthub.domain.repository.IAuthRepository
import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.domain.repository.INotificationRepository
import com.alejandrosahonero.courthub.domain.repository.ISportCenterRepository
import com.alejandrosahonero.courthub.domain.usecase.court.DisableCourtUseCase
import com.alejandrosahonero.courthub.utils.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCourtsUiState(
    val courts: List<Court> = emptyList(),
    val filteredCourts: List<Court> = emptyList(),
    val centers: List<SportCenter> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val error: String? = null,
    val courtToEdit: Court? = null,
    val showDeleteDialog: Court? = null,
    val courtToDisable: Court? = null,
    val showCreateSheet: Boolean = false,
    val isRefreshing: Boolean = false,
    val unreadCount: Int = 0,
    val activeFilter: AdminCourtFilter = AdminCourtFilter.ALL
)

class AdminCourtsViewModel(
    application: Application,
    private val courtRepository: ICourtRepository,
    private val disableCourtUseCase: DisableCourtUseCase,
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository,
    private val sportCenterRepository: ISportCenterRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AdminCourtsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCourts()
        loadUnreadCount()
        loadCenters()
    }

    private fun loadCenters() {
        viewModelScope.launch {
            sportCenterRepository.getSportCenters().collect { centers ->
                _uiState.update { it.copy(centers = centers) }
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

    private fun loadCourts() {
        viewModelScope.launch {
            courtRepository.getCourts().collect { courts ->
                _uiState.update {
                    it.copy(
                        courts = courts,
                        filteredCourts = filterCourts(courts, it.searchQuery, it.activeFilter),
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    private fun filterCourts(
        courts: List<Court>,
        query: String,
        filter: AdminCourtFilter
    ): List<Court> {
        var result = when (filter) {
            AdminCourtFilter.ALL -> courts
            AdminCourtFilter.ENABLED -> courts.filter { it.isEnabled }
            AdminCourtFilter.DISABLED -> courts.filter { !it.isEnabled }
            AdminCourtFilter.PRICE_ASC -> courts.sortedBy { it.pricePerHour }
            AdminCourtFilter.PRICE_DESC -> courts.sortedByDescending { it.pricePerHour }
            AdminCourtFilter.PADEL -> courts.filter { it.type == CourtType.PADEL }
            AdminCourtFilter.FUTBOL -> courts.filter { it.type == CourtType.FUTBOL }
            AdminCourtFilter.TENIS -> courts.filter { it.type == CourtType.TENIS }
            AdminCourtFilter.CRISTAL -> courts.filter { it.type == CourtType.CRISTAL }
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
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCourts = filterCourts(it.courts, query, it.activeFilter)
            )
        }
    }

    fun onFilterSelected(filter: AdminCourtFilter) {
        _uiState.update {
            it.copy(
                activeFilter = filter,
                filteredCourts = filterCourts(it.courts, it.searchQuery, filter)
            )
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun updateCourt(court: Court) {
        viewModelScope.launch {
            courtRepository.updateCourt(court)
                .onSuccess {
                    _uiState.update { it.copy(courtToEdit = null) }
                    sendLocalNotification(
                        "Pista Actualizada",
                        "La pista ${court.name} ha sido modificada."
                    )
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteCourt(courtId: String) {
        viewModelScope.launch {
            courtRepository.deleteCourt(courtId)
                .onSuccess {
                    _uiState.update { it.copy(showDeleteDialog = null) }
                    sendLocalNotification(
                        "Pista Eliminada",
                        "La pista ha sido eliminada correctamente."
                    )
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message,
                            showDeleteDialog = null
                        )
                    }
                }
        }
    }

    fun disableCourt(courtId: String, reason: String, from: Long, until: Long) {
        viewModelScope.launch {
            disableCourtUseCase(courtId, reason, from, until)
                .onSuccess {
                    sendLocalNotification(
                        "Pista Deshabilitada",
                        "Se han cancelado las reservas y deshabilitado la pista."
                    )
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun enableCourt(courtId: String) {
        viewModelScope.launch {
            courtRepository.enableCourt(courtId)
                .onSuccess {
                    sendLocalNotification("Pista Habilitada", "La pista vuelve a estar disponible.")
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    private fun sendLocalNotification(title: String, message: String) {
        val data = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

    fun onDisableRequest(court: Court) = _uiState.update { it.copy(courtToDisable = court) }
    fun onDismissDisable() = _uiState.update { it.copy(courtToDisable = null) }
    fun onEditCourt(court: Court) = _uiState.update { it.copy(courtToEdit = court) }
    fun onDismissEdit() = _uiState.update { it.copy(courtToEdit = null) }
    fun onShowCreate() = _uiState.update { it.copy(showCreateSheet = true) }
    fun onDismissCreate() = _uiState.update { it.copy(showCreateSheet = false) }

    fun createCourt(court: Court) {
        viewModelScope.launch {
            courtRepository.createCourt(court)
                .onSuccess {
                    _uiState.update { it.copy(showCreateSheet = false) }
                    sendLocalNotification(
                        "Pista Creada",
                        "La pista ${court.name} ha sido creada correctamente."
                    )
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun onDeleteRequest(court: Court) = _uiState.update { it.copy(showDeleteDialog = court) }
    fun onDismissDelete() = _uiState.update { it.copy(showDeleteDialog = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    companion object {
        fun factory(
            application: Application,
            courtRepository: ICourtRepository,
            disableCourtUseCase: DisableCourtUseCase,
            authRepository: IAuthRepository,
            notificationRepository: INotificationRepository,
            sportCenterRepository: ISportCenterRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                AdminCourtsViewModel(
                    application,
                    courtRepository,
                    disableCourtUseCase,
                    authRepository,
                    notificationRepository,
                    sportCenterRepository
                ) as T
        }
    }
}