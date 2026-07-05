package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.OrganizerTravelsActivityResponse
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.repository.TravelRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrganizerTravelsUiState(
    val isLoading: Boolean = false,
    val user: MinimalInformationResponseDTO? = null,
    val items: OrganizerTravelsActivityResponse? = null,
    val errorMessage: String? = null

)

@HiltViewModel
class OrganizerTravelsViewModel @Inject constructor(private val travelRepository: TravelRepository ,private  val userRepository: UserRepository , private val savedStateHandle: SavedStateHandle): ViewModel(){
    private val _uiState = MutableStateFlow(OrganizerTravelsUiState())
    val uiState = _uiState.asStateFlow()

    val username: String? = savedStateHandle["username"]

    init {
        if (username != null) loadUserAndItems(username)
        else _uiState.value = OrganizerTravelsUiState(errorMessage = "Organizer username not valid")
    }

    private fun loadUserAndItems(username: String){
        viewModelScope.launch {
            _uiState.value = OrganizerTravelsUiState(isLoading = true)

            val userResponse = userRepository.getOrganizerInfo(username)
            if (userResponse.success && userResponse.data != null){
                _uiState.value = _uiState.value.copy(user = userResponse.data)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = userResponse.errorMessage?: "Error while fetching organizer info")
                return@launch
            }

            val userId = _uiState.value.user?.keycloakId
            if (userId != null){
                val response = travelRepository.getOrganizerTravelsActivities(userId)
                if (response.success && response.data != null){
                    _uiState.value = _uiState.value.copy(isLoading = false, items = response.data)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage?: "Error while loading travels and activities")
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error while loading travels and activities")
            }

        }
    }
}