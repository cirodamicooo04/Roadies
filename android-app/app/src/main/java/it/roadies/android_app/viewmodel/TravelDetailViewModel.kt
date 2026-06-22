package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TravelDetailState(
    val isLoading: Boolean = false,
    val travel: TravelResponse? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TravelDetailViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle,private val travelRepository: TravelRepository): ViewModel() {
    val id: String? = savedStateHandle["id"]
    private val _uiState = MutableStateFlow(TravelDetailState())
    val uiState = _uiState.asStateFlow()

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

}