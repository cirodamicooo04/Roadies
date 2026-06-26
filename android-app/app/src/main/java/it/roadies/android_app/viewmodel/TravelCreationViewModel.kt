package it.roadies.android_app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.roadies.android_app.client.models.travel.TravelCreateRequest
import it.roadies.android_app.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import it.roadies.android_app.repository.LocationRepository
import it.roadies.android_app.client.models.travel.LocationType
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.client.models.travel.TagResponse
import it.roadies.android_app.repository.MetadataRepository
import java.util.UUID
import javax.inject.Inject

enum class CreateTravelStep {
    BASIC_INFO,
    ACTIVITIES,
    DEPARTURES
}

data class UploadableImage(
    val localUri: Uri,
    val imageUUID: UUID? = null,
    val isUploading: Boolean = true,
    val isFailed: Boolean = false
)

data class TravelActivityState(
    val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val addressSearchQuery: String = "", 
    val latitude: Double? = null, 
    val longitude: Double? = null,
    val dayNumber: String = "",
    val images: List<UploadableImage> = emptyList(),
    val fieldErrors: Map<String, Int> = emptyMap()
)

data class CreateTravelUiState(
    val currentStep: CreateTravelStep = CreateTravelStep.BASIC_INFO,
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, Int> = emptyMap(),
    val tags: List<TagResponse> = emptyList(),
    val errorMessage: String? = null,


    //STEP 1:
    val title: String = "",
    val description: String = "",
    val continent: TravelCreateRequest.Continent = TravelCreateRequest.Continent.EUROPE,
    val country: String = "",
    val destination: String = "",
    val durationDays: String = "",

    val latitude: Double? = null,
    val longitude: Double? = null,

    val images: List<UploadableImage> = emptyList(),
    val tagScores: Map<UUID, Int> = emptyMap(),

    //STEP 2: Activities
    val activities: List<TravelActivityState> = emptyList(),
    val currentEditingActivity: TravelActivityState? = null

    //STEP 3: DEPARTURES
)

@HiltViewModel
class TravelCreationViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val locationRepository: LocationRepository,
    private val metadataRepository: MetadataRepository,
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateTravelUiState())
    val uiState = _uiState.asStateFlow()

    private val _locationSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val locationSuggestions = _locationSuggestions.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags(){
        viewModelScope.launch {
            _uiState.value = CreateTravelUiState(isLoading = true)

            val response = metadataRepository.getTags()
            if (response.success && response.data != null){
                _uiState.value = _uiState.value.copy(tags = response.data, isLoading = false )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

    fun searchDestinations(query: String) {
        updateDestinationSearchQuery(query)

        if (query.length > 2) {
            viewModelScope.launch {
                val results = locationRepository.getSuggestions(query)
                _locationSuggestions.value = results.filter { it.type == LocationType.DESTINATION }
            }
        } else {
            _locationSuggestions.value = emptyList()
        }
    }

    fun clearLocationSuggestions() {
        _locationSuggestions.value = emptyList()
    }

    fun uploadImagesFromGallery(uris: List<Uri>){
        val newImages = uris.map { UploadableImage(localUri = it) }
        _uiState.update { it.copy(images = it.images + newImages) }

        uris.forEach { uri ->  
            viewModelScope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
                        val response = travelRepository.uploadImage(part)
                        
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
        _uiState.update { state ->
            state.copy(images = state.images.filter { it.localUri != uri })
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, fieldErrors = it.fieldErrors - "title") }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription, fieldErrors = it.fieldErrors - "description") }
    }

    fun updateDurationDays(newDuration: String) {
        if (newDuration.all { it.isDigit() }) {
            _uiState.update { it.copy(durationDays = newDuration, fieldErrors = it.fieldErrors - "durationDays") }
        }
    }

    fun updateLocation(
        destination: String,
        country: String,
        continent: TravelCreateRequest.Continent,
        latitude: Double,
        longitude: Double
    ) {
        _uiState.value = _uiState.value.copy(
            destination = destination,
            country = country,
            continent = continent,
            latitude = latitude,
            longitude = longitude
        )
    }

    fun updateDestinationSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            destination = query,
            country = "",
            latitude = null,
            longitude = null,
            fieldErrors = _uiState.value.fieldErrors - "destination"
        )
    }


    fun nextStep(){
        val currentState = _uiState.value

        when (currentState.currentStep){
            CreateTravelStep.BASIC_INFO -> {
                val errors = mutableMapOf<String, Int>()

                if (currentState.title.isBlank() || currentState.title.length < 3 || currentState.title.length > 150) {
                    errors["title"] = it.roadies.android_app.R.string.error_title_invalid
                }

                if (currentState.description.isBlank() || currentState.description.length > 10000 || currentState.description.length < 20) {
                    errors["description"] = it.roadies.android_app.R.string.error_description_invalid
                }

                if (currentState.latitude == null || currentState.longitude == null || 
                    currentState.country.isBlank() || currentState.destination.isBlank()) {
                    errors["destination"] = it.roadies.android_app.R.string.error_destination_invalid
                } else {
                    if (currentState.latitude !in -90.0..90.0 || currentState.longitude !in -180.0..180.0) {
                        errors["destination"] = it.roadies.android_app.R.string.error_destination_invalid
                    }
                }

                val duration = currentState.durationDays.toIntOrNull()
                if (duration == null || duration < 1) {
                    errors["durationDays"] = it.roadies.android_app.R.string.error_duration_invalid
                }

                if (errors.isNotEmpty()) {
                    _uiState.value = currentState.copy(fieldErrors = errors)
                    return
                }

                _uiState.value = currentState.copy(
                    fieldErrors = emptyMap(), 
                    currentStep = CreateTravelStep.ACTIVITIES
                )
            }
            CreateTravelStep.ACTIVITIES -> {
                //validazioni
                _uiState.value = _uiState.value.copy(currentStep = CreateTravelStep.DEPARTURES)
            }
            CreateTravelStep.DEPARTURES -> {
                //validazioni
                createTravel()
            }

        }
    }

    fun previousStep(){
        val currentState = _uiState.value
        when (currentState.currentStep){
            CreateTravelStep.DEPARTURES -> {
                _uiState.value = _uiState.value.copy(currentStep = CreateTravelStep.ACTIVITIES)
            }
            CreateTravelStep.ACTIVITIES -> {
                _uiState.value = _uiState.value.copy(currentStep = CreateTravelStep.BASIC_INFO)
            }
            CreateTravelStep.BASIC_INFO -> {
                //sono gia al primo passo , faccio chiudere la ui
            }
        }
    }

    private fun createTravel() {
        TODO("Not yet implemented")
    }

    fun updateTagScore(tagId: UUID, score: Int) {
        val newTagScores = _uiState.value.tagScores.toMutableMap()
        newTagScores[tagId] = score
        _uiState.value = _uiState.value.copy(
            tagScores = newTagScores
        )
    }
}