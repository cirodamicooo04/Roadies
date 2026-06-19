package it.roadies.android_app.viewmodel

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
class TravelDetailViewModel @Inject constructor(private val travelRepository: TravelRepository): ViewModel() {

}