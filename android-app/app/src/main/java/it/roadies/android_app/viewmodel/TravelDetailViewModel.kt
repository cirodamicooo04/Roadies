package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.booking.BookingDraftRequest
import it.roadies.android_app.client.models.travel.TravelDepartureResponse
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.repository.AuthRepository
import it.roadies.android_app.repository.BookingRepository
import it.roadies.android_app.repository.TravelRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TravelDetailState(
    val isLoading: Boolean = false,
    val travel: TravelResponse? = null,
    val errorMessage: String? = null,
    val createdBookingId: UUID? = null,
    val selectedDepartureId: UUID? = null,
    val requireLogin: Boolean = false
)

data class TravelDepartureState(
    val isLoading: Boolean = false,
    val departures: List<TravelDepartureResponse>? = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TravelDetailViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle,private val travelRepository: TravelRepository,private val bookingRepository: BookingRepository, private val authRepository: AuthRepository): ViewModel() {
    val id: String? = savedStateHandle["id"]
    private val _uiState = MutableStateFlow(TravelDetailState())
    val uiState = _uiState.asStateFlow()

    private val _departuresState = MutableStateFlow(TravelDepartureState())
    val departuresState = _departuresState.asStateFlow()

    init {
        val uuid = runCatching { UUID.fromString(id) }.getOrNull()
        if (uuid != null) loadTravel(uuid)
        else _uiState.value = TravelDetailState(errorMessage = "Travel id not valid")
    }

    private fun loadTravel(id: UUID){
        viewModelScope.launch {
            _uiState.value = TravelDetailState(isLoading = true)

            val response = travelRepository.getTravelById(id)
            if (response.success){
                _uiState.value = TravelDetailState(travel = response.data, isLoading = false)
            }
            else {
                _uiState.value = TravelDetailState(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

    fun loadDepartures(){
        viewModelScope.launch {
            val uuid = runCatching { UUID.fromString(id) }.getOrNull()
            if (uuid != null){
                _departuresState.value = _departuresState.value.copy(isLoading = true)

                val response = travelRepository.getTravelDepartures(uuid)
                if (response.success ){
                    _departuresState.value = TravelDepartureState(isLoading = false, departures = response.data)
                } else {
                    _departuresState.value = TravelDepartureState(isLoading = false, errorMessage = response.errorMessage)
                }


            } else {
                _departuresState.value = TravelDepartureState(isLoading = false, errorMessage = "Travel id not valid")
            }
        }

    }
    fun createDraftBooking(departureId: UUID) {
        val isLogged = authRepository.authState.value.isLogged
        if (!isLogged) {
            _uiState.value =_uiState.value.copy(requireLogin = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val request = BookingDraftRequest(travelId = departureId, activityId = null)
            val response = bookingRepository.createDraft(request)
            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    createdBookingId = response.data.bookingId,
                    selectedDepartureId = departureId
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage ?: "Error with booking"
                )
            }
        }
    }

    fun onBookingNavigated() {
        _uiState.value = _uiState.value.copy(createdBookingId = null, selectedDepartureId = null)
    }

    fun onLoginHandled() {
        _uiState.value = _uiState.value.copy(requireLogin = false)
    }
}