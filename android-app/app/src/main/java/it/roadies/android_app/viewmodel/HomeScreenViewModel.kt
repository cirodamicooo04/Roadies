package it.roadies.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.repository.AuthRepository
import it.roadies.android_app.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject
import it.roadies.android_app.repository.LocationRepository
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.client.models.travel.LocationType

data class HomeScreenUiState(
    val isLoading: Boolean = false,
    val recommendedTravels: List<TravelSummaryResponse>? = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    //Lo teniamo separato perché ci serve il debounce
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val availableContinents = listOf("Europe", "Asia", "Oceania", "Africa", "America")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchSuggestions = _searchQuery
        .debounce(200L) // Aspetta 200ms di inattività prima di cercare
        .mapLatest { query -> //map latest permette di vedere l'ultimissimo aggiornamento
            if (query.trim().length < 2) return@mapLatest emptyList()

            val suggestions = mutableListOf<SearchSuggestion>()

            val matchingContinents = availableContinents.filter {
                it.contains(query, ignoreCase = true)
            }
            suggestions.addAll(matchingContinents.map {
                SearchSuggestion(name = it, type = LocationType.CONTINENT)
            })

            val apiResults = locationRepository.getSuggestions(query.trim())
            suggestions.addAll(apiResults)

            return@mapLatest suggestions
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            //collectLatest: ogni volta che lo stato auth cambia esegue quel blocco di codice
            authRepository.authState.collectLatest { authState ->
                if (!authState.isLoading) {
                    loadRecommendedTravel(authState.isLogged)
                }
            }
        }
    }


    //TODO: Chiamarlo quando implementerò il pull to refresh box
    fun refreshRecommendedTravel() {
        viewModelScope.launch {
            val authState = authRepository.authState.value

            if (!authState.isLoading) {
                loadRecommendedTravel(authState.isLogged)
            }
        }
    }

    private suspend fun loadRecommendedTravel(isLogged: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        if (!isLogged) {
            val response  = travelRepository.getPublicRecommendations()

            if (response.success && response.data != null){
                _uiState.value = HomeScreenUiState(isLoading = false, recommendedTravels = response.data)
            } else {
                _uiState.value = HomeScreenUiState(isLoading = false, errorMessage = response.errorMessage)
            }

        } else {
            val response = travelRepository.getRecommendations()

            if (response.success && response.data != null) {
                _uiState.value = HomeScreenUiState(isLoading = false, recommendedTravels = response.data)
            } else {
                _uiState.value = HomeScreenUiState(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }
}
