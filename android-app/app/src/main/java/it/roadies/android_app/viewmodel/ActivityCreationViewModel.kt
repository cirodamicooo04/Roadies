package it.roadies.android_app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.roadies.android_app.R
import it.roadies.android_app.client.models.travel.ActivityCreateRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureCreateRequest
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.client.models.travel.TravelCreateRequest
import it.roadies.android_app.repository.ActivityRepository
import it.roadies.android_app.repository.LocationRepository
import it.roadies.android_app.ui.travel.components.UploadableImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

enum class ActivityCreationStep {
    BASIC_INFO,
    DEPARTURES
}

data class ActivityCreationDepartureState(
    val id: UUID = UUID.randomUUID(),
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val price: BigDecimal? = null,
    val maxSlots: Int? = null
)

data class ActivityCreationUiState(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val addressSearchQuery: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val continent: TravelCreateRequest.Continent = TravelCreateRequest.Continent.EUROPE,
    val country: String = "",
    val destination: String = "",
    val images: List<UploadableImage> = emptyList(),

    //STEP 2
    val departures: List<ActivityCreationDepartureState> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, Int> = emptyMap(),
    
    val currentStep: ActivityCreationStep = ActivityCreationStep.BASIC_INFO,
    val isCreationLoading: Boolean = false,
    val isCreationSuccess: Boolean = false,
    val creationErrorMessage: String? = null
)

@HiltViewModel
class ActivityCreationViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository,
    @ApplicationContext private val context: Context
): ViewModel(){
    private val _uiState = MutableStateFlow(ActivityCreationUiState())
    val uiState = _uiState.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val addressSuggestions = _addressSuggestions.asStateFlow()

    fun saveDeparture(departure: ActivityCreationDepartureState) {
        _uiState.update { state ->
            val exists = state.departures.any { it.id == departure.id }
            val newDepartures = if (exists) {
                state.departures.map { if (it.id == departure.id) departure else it }
            } else {
                state.departures + departure
            }
            state.copy(departures = newDepartures)
        }
    }

    fun removeDeparture(departureId: UUID) {
        _uiState.update { state ->
            state.copy(departures = state.departures.filter { it.id != departureId })
        }
    }

    fun nextStep() {
        val currentState = _uiState.value

        when (currentState.currentStep) {
            ActivityCreationStep.BASIC_INFO -> {
                val errors = mutableMapOf<String, Int>()

                if (currentState.name.isBlank() || currentState.name.length < 3 || currentState.name.length > 150) {
                    errors["name"] = R.string.error_title_invalid
                }

                if (currentState.description.isBlank() || currentState.description.length > 10000 || currentState.description.length < 20) {
                    errors["description"] = R.string.error_description_invalid
                }

                if (currentState.latitude == null || currentState.longitude == null || 
                    currentState.country.isBlank() || currentState.destination.isBlank()) {
                    errors["address"] = R.string.error_destination_invalid
                } else {
                    if (currentState.latitude !in -90.0..90.0 || currentState.longitude !in -180.0..180.0) {
                        errors["address"] = R.string.error_destination_invalid
                    }
                }

                if (errors.isNotEmpty()) {
                    _uiState.value = currentState.copy(fieldErrors = errors)
                    return
                }

                _uiState.value = currentState.copy(
                    fieldErrors = emptyMap(), 
                    currentStep = ActivityCreationStep.DEPARTURES
                )
            }
            ActivityCreationStep.DEPARTURES -> {
                createActivity()
            }
        }
    }

    private fun createActivity() {
        val state = uiState.value
        val imageIds: List<UUID> = state.images.mapNotNull { it.imageUUID }

        val mappedDepartures = state.departures.map { dep ->
            ActivityDepartureCreateRequest(
                startTimestamp = ZonedDateTime.of(dep.date, dep.startTime, ZoneId.systemDefault()).toOffsetDateTime(),
                endTimestamp = ZonedDateTime.of(dep.date, dep.endTime, ZoneId.systemDefault()).toOffsetDateTime(),
                price = dep.price!!,
                maxSlots = dep.maxSlots!!
            )
        }

        val activityCreationDto = ActivityCreateRequest(
            name = state.name,
            description = state.description,
            continent = ActivityCreateRequest.Continent.valueOf(state.continent.name),
            country = state.country.ifBlank { null },
            destination = state.destination,
            address = state.address,
            latitude = state.latitude,
            longitude = state.longitude,
            imageIds = imageIds,
            departures = mappedDepartures,
            dayNumber = null
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isCreationLoading = true, creationErrorMessage = null) }
            val response = activityRepository.createActivity(activityCreationDto)
            if (response.success && response.data != null) {
                _uiState.update { it.copy(isCreationLoading = false, isCreationSuccess = true) }
            } else {
                _uiState.update { it.copy(isCreationLoading = false, creationErrorMessage = response.errorMessage ?: "Errore sconosciuto durante la creazione") }
            }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep == ActivityCreationStep.DEPARTURES) {
            _uiState.value = _uiState.value.copy(currentStep = ActivityCreationStep.BASIC_INFO)
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, fieldErrors = it.fieldErrors - "name") }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, fieldErrors = it.fieldErrors - "description") }
    }

    fun searchAddresses(query: String) {
        _uiState.update { it.copy(
            addressSearchQuery = query,
            address = query,
            country = "",
            latitude = null,
            longitude = null,
            fieldErrors = it.fieldErrors - "address"
        ) }

        if (query.length > 2) {
            viewModelScope.launch {
                val results = locationRepository.getAddressSuggestions(query = query, lat = uiState.value.latitude, lon = uiState.value.longitude )
                _addressSuggestions.value = results
            }
        } else {
            _addressSuggestions.value = emptyList()
        }
    }

    fun clearAddressSuggestions() {
        _addressSuggestions.value = emptyList()
    }
    
    fun selectAddressSuggestion(suggestion: SearchSuggestion) {
        val displayLocation = if (!suggestion.country.isNullOrBlank()) "${suggestion.name}, ${suggestion.country}" else suggestion.name
        _uiState.value = _uiState.value.copy(
            addressSearchQuery = displayLocation,
            address = displayLocation,
            destination = suggestion.name,
            country = suggestion.country ?: "",
            latitude = suggestion.latitude,
            longitude = suggestion.longitude
        )
        clearAddressSuggestions()
    }
    
    fun uploadImagesFromGallery(uris: List<Uri>) {
        val newImages = uris.map { UploadableImage(localUri = it) }
        _uiState.update { it.copy(images = it.images + newImages) }

        uris.forEach { uri ->  
            viewModelScope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
                        val response = activityRepository.uploadImage(part)
                        
                        if (response.success && response.data != null) {
                            val serverUUID = response.data.id
                            _uiState.update { state ->
                                state.copy(images = state.images.map { img ->
                                    if (img.localUri == uri) img.copy(imageUUID = serverUUID, isUploading = false)
                                    else img
                                })
                            }
                        } else {
                            throw Exception("Upload failed with error")
                        }
                    } else {
                        throw Exception("Cannot read file bytes")
                    }
                } catch (e: Exception) {
                    _uiState.update { state ->
                        state.copy(images = state.images.map { img ->
                            if (img.localUri == uri) img.copy(isUploading = false, isFailed = true)
                            else img
                        })
                    }
                }
            } 
        }
    }
    
    fun removeImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            images = _uiState.value.images.filter { it.localUri != uri }
        )
    }
}
