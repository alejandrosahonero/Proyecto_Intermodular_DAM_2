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
    val selectedSlot: String? = null,
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
        _uiState.update { it.copy(selectedDate = date, selectedSlot = null) }
        loadSlots()
    }

    fun onSlotSelected(hour: String) {
        _uiState.update { it.copy(selectedSlot = hour) }
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