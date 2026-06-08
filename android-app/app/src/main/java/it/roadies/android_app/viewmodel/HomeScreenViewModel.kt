package it.roadies.android_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.repository.AuthRepository
import it.roadies.android_app.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeScreenUiState(
    val isLoading: Boolean = false,
    val recommendedTravels: List<TravelSummaryResponse>? = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(private val travelRepository: TravelRepository, private val authRepository: AuthRepository) : ViewModel(){
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun loadRecommendedTravel(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            if (!authRepository.getAuthState().isAuthorized){
                //faccio chiamata a endpoint che mi da 10 viaggi casuali
                val travels = emptyList<TravelSummaryResponse>()
                _uiState.value = HomeScreenUiState(isLoading = false, recommendedTravels = travels)
            } else {

                val response = travelRepository.getRecommendations()

                if (response.success && response.data != null){
                    _uiState.value = HomeScreenUiState(isLoading = false, recommendedTravels = response.data)
                } else {
                    _uiState.value = HomeScreenUiState(errorMessage = response.errorMessage)
                }
            }
        }
    }

}