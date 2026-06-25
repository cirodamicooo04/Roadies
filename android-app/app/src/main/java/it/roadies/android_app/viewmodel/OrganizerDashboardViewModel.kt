package it.roadies.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.OrganizerTravelsActivityResponse
import it.roadies.android_app.repository.ActivityRepository
import it.roadies.android_app.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OrganizerDashboardUiState (
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val organizerResponse: OrganizerTravelsActivityResponse? = null,
    val isDeleting: Boolean = false,
    val deletingErrorMessage: String? = null
)

@HiltViewModel
class OrganizerDashboardViewModel @Inject constructor(private val travelRepository: TravelRepository, private val activityRepository: ActivityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(OrganizerDashboardUiState())
    val uiState = _uiState.asStateFlow()


    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = OrganizerDashboardUiState(isLoading = true)

            val response = travelRepository.getOrganizerTravelsActivities()
            if (response.success && response.data != null) {
                _uiState.value =
                    OrganizerDashboardUiState(isLoading = false, organizerResponse = response.data)
            } else {
                _uiState.value = OrganizerDashboardUiState(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error while loading data"
                )
            }
        }
    }

    fun deleteTravel(id: UUID?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            if (id == null) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deletingErrorMessage = "Travel not found"
                )
            } else {
                val response = travelRepository.deleteTravel(id)
                if (response.success) {
                    val currentResponse = _uiState.value.organizerResponse
                    val updatedTravels = currentResponse?.travels?.filter { it.id != id }

                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deletingErrorMessage = null,
                        organizerResponse = currentResponse?.copy(travels = updatedTravels)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deletingErrorMessage = response.errorMessage ?: "Error while deleting"
                    )
                }
            }
        }
    }

    fun deleteActivity(id: UUID?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            if (id == null) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deletingErrorMessage = "Activity not found"
                )
            } else {
                val response = activityRepository.deleteActivity(id)
                if (response.success) {
                    val currentResponse = _uiState.value.organizerResponse
                    val updatedActivities = currentResponse?.activities?.filter { it.id != id }

                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deletingErrorMessage = null,
                        organizerResponse = currentResponse?.copy(activities = updatedActivities)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deletingErrorMessage = response.errorMessage ?: "Error while deleting"
                    )
                }
            }
        }
    }

    fun clearDeleteError() {
        _uiState.value = _uiState.value.copy(deletingErrorMessage = null)
    }

}