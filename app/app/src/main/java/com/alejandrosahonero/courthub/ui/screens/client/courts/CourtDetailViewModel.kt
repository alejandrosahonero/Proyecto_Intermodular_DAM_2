package com.alejandrosahonero.courthub.ui.screens.client.courts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alejandrosahonero.courthub.domain.model.Court
import com.alejandrosahonero.courthub.domain.repository.ICourtRepository
import com.alejandrosahonero.courthub.domain.usecase.court.GetAvailableSlotsUseCase
import com.alejandrosahonero.courthub.domain.usecase.court.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CourtDetailUiState(
    val court: Court? = null,
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val slots: List<TimeSlot> = emptyList(),
    val selectedSlots: List<String> = emptyList(),
    val slotsLoading: Boolean = false,
    val error: String? = null
)

class CourtDetailViewModel(
    private val courtId: String,
    private val courtRepository: ICourtRepository,
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourtDetailUiState())
    val uiState: StateFlow<CourtDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourt()
    }

    private fun loadCourt() {
        viewModelScope.launch {
            courtRepository.getCourtById(courtId)
                .onSuccess { court ->
                    _uiState.update { it.copy(court = court, isLoading = false) }
                    loadSlots()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedSlots = emptyList()) }
        loadSlots()
    }

    fun onSlotSelected(hour: String) {
        val current = _uiState.value.selectedSlots.toMutableList()
        val slots = _uiState.value.slots

        if (current.contains(hour)) {
            // Deseleccionar — elimina desde ese punto hasta el final
            val index = current.indexOf(hour)
            _uiState.update { it.copy(selectedSlots = current.take(index)) }
            return
        }

        if (current.isEmpty()) {
            _uiState.update { it.copy(selectedSlots = listOf(hour)) }
            return
        }

        // Solo permitimos slots contiguos
        val availableHours = slots.filter { it.isAvailable }.map { it.hour }
        val lastSelected = current.last()
        val lastIndex = availableHours.indexOf(lastSelected)
        val newIndex = availableHours.indexOf(hour)

        if (newIndex == lastIndex + 1) {
            _uiState.update { it.copy(selectedSlots = current + hour) }
        } else {
            // Si no es contiguo, reinicia con el nuevo slot
            _uiState.update { it.copy(selectedSlots = listOf(hour)) }
        }
    }

    fun getEndTime(): String {
        val lastSlot = _uiState.value.selectedSlots.lastOrNull() ?: return ""
        val hour = lastSlot.split(":")[0].toInt()
        return "%02d:00".format(hour + 1)
    }

    private fun loadSlots() {
        val court = _uiState.value.court ?: return
        val date = _uiState.value.selectedDate
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        viewModelScope.launch {
            _uiState.update { it.copy(slotsLoading = true) }
            getAvailableSlotsUseCase(
                courtId = courtId,
                date = date,
                disabledFrom = court.disabledFrom,
                disabledUntil = court.disabledUntil
            ).onSuccess { slots ->
                _uiState.update { it.copy(slots = slots, slotsLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(slotsLoading = false) }
            }
        }
    }

    companion object {
        fun factory(
            courtId: String,
            courtRepository: ICourtRepository,
            getAvailableSlotsUseCase: GetAvailableSlotsUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CourtDetailViewModel(courtId, courtRepository, getAvailableSlotsUseCase) as T
        }
    }
}