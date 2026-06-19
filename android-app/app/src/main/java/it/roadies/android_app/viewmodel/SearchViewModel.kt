package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.apis.travel.ViaggiApi
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.repository.ActivityRepository
import it.roadies.android_app.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchScreenUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLastPage: Boolean = false,
    val travels: List<TravelSummaryResponse>? = emptyList(),
    val activities: List<ActivitySummaryResponse>? = emptyList(),
    val errorMessage: String? = null,
    val type: String? = "TRAVEL"
)

@HiltViewModel
class SearchScreenViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle ,private val travelRepository: TravelRepository, private val activityRepository: ActivityRepository) : ViewModel(){
    //hilt inietta automaticamente SavedStateHandle per prendere i parametri della rotta search
    val continent: String? = savedStateHandle["continent"];
    val country: String? = savedStateHandle["country"]
    val destination: String? = savedStateHandle["destination"]
    val minPrice: String? = savedStateHandle["minPrice"]
    val maxPrice: String? = savedStateHandle["maxPrice"]
    val minDurationDays: String? = savedStateHandle["minDurationDays"]
    val maxDurationDays: String? = savedStateHandle["maxDurationDays"]
    val type: String? = savedStateHandle["type"]

    private val _searchScreenUiState = MutableStateFlow(SearchScreenUiState())
    val searchScreenUiState = _searchScreenUiState.asStateFlow()

    private var currentPage = 0

    init {
        _searchScreenUiState.value = _searchScreenUiState.value.copy(type = type)
        loadItems(continent,country,destination,minPrice,maxPrice,minDurationDays,maxDurationDays,type)
    }

    fun loadNextPage() {
        val state = _searchScreenUiState.value
        if (!state.isLastPage && !state.isLoadingMore && !state.isLoading) {
            currentPage++
            loadItems(continent, country, destination, minPrice, maxPrice, minDurationDays, maxDurationDays, type, isLoadMore = true)
        }
    }

    private fun loadItems(continentStr: String?, country: String?, destination: String?, minPriceStr: String?, maxPriceStr: String?, minDurationDaysStr: String?, maxDurationDaysStr: String?, type: String?, isLoadMore: Boolean = false){
        viewModelScope.launch {
            if (isLoadMore) {
                _searchScreenUiState.value = _searchScreenUiState.value.copy(isLoadingMore = true)
            } else {
                _searchScreenUiState.value = _searchScreenUiState.value.copy(isLoading = true, errorMessage = null, isLastPage = false)
                currentPage = 0
            }
            
            // Convertiamo le stringhe nei tipi previsti dall'API
            val minPrice = minPriceStr?.toBigDecimalOrNull()
            val maxPrice = maxPriceStr?.toBigDecimalOrNull()
            val minDurationDays = minDurationDaysStr?.toIntOrNull()
            val maxDurationDays = maxDurationDaysStr?.toIntOrNull()
            
            val continentEnum = try {
                continentStr?.let {
                    ViaggiApi.ContinentSearchTravels.valueOf(it.uppercase()) }
            } catch (e: IllegalArgumentException) {
                null
            }

            if (type == "ACTIVITY") {
                val response = activityRepository.searchActivities(
                    destination = destination,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    minDurationDays = minDurationDays,
                    maxDurationDays = maxDurationDays,
                    continent = continentEnum,
                    country = country,
                    page = currentPage
                )

                if (response.success && response.data != null) {
                    val pageData = response.data
                    val lastPage = currentPage + 1 >= pageData.totalPages
                    val currentList = if (isLoadMore) _searchScreenUiState.value.activities ?: emptyList() else emptyList()
                    
                    _searchScreenUiState.value = _searchScreenUiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isLastPage = lastPage,
                        activities = currentList + pageData.content,
                        travels = emptyList(),
                        errorMessage = null
                    )
                } else {
                    _searchScreenUiState.value = _searchScreenUiState.value.copy(
                        isLoading = false,
                        errorMessage = response.errorMessage ?: "Errore nella ricerca attività"
                    )
                }
            } else {
                val response = travelRepository.searchTravels(
                    destination = destination,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    minDurationDays = minDurationDays,
                    maxDurationDays = maxDurationDays,
                    continent = continentEnum,
                    country = country,
                    page = currentPage
                )

                if (response.success && response.data != null) {
                    val pageData = response.data
                    val lastPage = currentPage + 1 >= pageData.totalPages
                    val currentList = if (isLoadMore) _searchScreenUiState.value.travels ?: emptyList() else emptyList()

                    _searchScreenUiState.value = _searchScreenUiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isLastPage = lastPage,
                        travels = currentList + pageData.content,
                        activities = emptyList(),
                        errorMessage = null
                    )
                } else {
                    _searchScreenUiState.value = _searchScreenUiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = response.errorMessage ?: "Errore nella ricerca viaggi"
                    )
                }
            }
        }
    }
}