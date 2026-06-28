package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.booking.BookingDraftRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureResponse
import it.roadies.android_app.client.models.travel.ActivityResponse
import it.roadies.android_app.repository.ActivityRepository
import it.roadies.android_app.repository.AuthRepository
import it.roadies.android_app.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ActivityDetailState(
    val isLoading: Boolean = false,
    val activity: ActivityResponse? = null,
    val errorMessage: String? = null,
    val createdBookingId: UUID? = null,
    val selectedDepartureId: UUID? = null,
    val requireLogin: Boolean = false
)

data class ActivityDepartureState(
    val isLoading: Boolean = false,
    val departures: List<ActivityDepartureResponse>? = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    val id: String? = savedStateHandle["id"]
    
    private val _uiState = MutableStateFlow(ActivityDetailState())
    val uiState = _uiState.asStateFlow()

    private val _departuresState = MutableStateFlow(ActivityDepartureState())
    val departuresState = _departuresState.asStateFlow()

    init {
        val uuid = runCatching { UUID.fromString(id) }.getOrNull()
        if (uuid != null) loadActivity(uuid)
        else _uiState.value = ActivityDetailState(errorMessage = "Activity id not valid")
    }

    private fun loadActivity(id: UUID){
        viewModelScope.launch {
            _uiState.value = ActivityDetailState(isLoading = true)

            val response = activityRepository.getActivityById(id)
            if (response.success){
                _uiState.value = ActivityDetailState(activity = response.data, isLoading = false)
            }
            else {
                _uiState.value = ActivityDetailState(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

    fun loadDepartures(){
        viewModelScope.launch {
            val uuid = runCatching { UUID.fromString(id) }.getOrNull()
            if (uuid != null){
                _departuresState.value = _departuresState.value.copy(isLoading = true)

                val response = activityRepository.getActivityDepartures(uuid)
                if (response.success ){
                    _departuresState.value = ActivityDepartureState(isLoading = false, departures = response.data)
                } else {
                    _departuresState.value = ActivityDepartureState(isLoading = false, errorMessage = response.errorMessage)
                }
            } else {
                _departuresState.value = ActivityDepartureState(isLoading = false, errorMessage = "Activity id not valid")
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
            val request = BookingDraftRequest(travelId = null, activityId = departureId)
            val response = bookingRepository.createDraft(request)
            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    createdBookingId = response.data.bookingId,
                    selectedDepartureId = departureId
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage ?: "Error with booking")
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