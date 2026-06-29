package it.roadies.android_app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.roadies.android_app.R
import it.roadies.android_app.client.models.travel.ActivityCreateRequest
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
import it.roadies.android_app.client.models.travel.TravelDepartureCreateRequest
import it.roadies.android_app.client.models.travel.TravelTagRequest
import it.roadies.android_app.repository.MetadataRepository
import java.math.BigDecimal
import java.time.LocalDate
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
    val dayNumber: Int = 1,
    val continent: TravelCreateRequest.Continent = TravelCreateRequest.Continent.EUROPE,
    val country: String = "",
    val destination: String = "",
    val images: List<UploadableImage> = emptyList(),
    val fieldErrors: Map<String, Int> = emptyMap()
)

data class TravelCreationDepartureState(
    val id: UUID = UUID.randomUUID(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val price: BigDecimal? = null,
    val maxSlots: Int? = null
)

data class CreateTravelUiState(
    val currentStep: CreateTravelStep = CreateTravelStep.BASIC_INFO,
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, Int> = emptyMap(),
    val tags: List<TagResponse> = emptyList(),
    val errorMessage: String? = null,
    val isCreationLoading: Boolean = false,
    val creationErrorMessage: String? = null,
    val isCreationSuccess: Boolean = false,


    //STEP 1:
    val title: String = "",
    val description: String = "",
    val continent: TravelCreateRequest.Continent = TravelCreateRequest.Continent.EUROPE,
    val country: String = "",
    val destination: String = "",
    val durationDays: Int? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,

    val images: List<UploadableImage> = emptyList(),
    val tagScores: Map<UUID, Int> = emptyMap(),

    //STEP 2: Activities
    val activities: List<TravelActivityState> = emptyList(),
    val currentEditingActivity: TravelActivityState? = null,

    //STEP 3: DEPARTURES
    val departures: List<TravelCreationDepartureState> = emptyList()
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

    private val _addressSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val addressSuggestions = _addressSuggestions.asStateFlow()

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

    fun searchAddresses(query: String) {
        if (query.length > 2) {
            viewModelScope.launch {
                val results = locationRepository.getAddressSuggestions(
                    query = query,
                    lat = _uiState.value.latitude,
                    lon = _uiState.value.longitude
                )
                _addressSuggestions.value = results
            }
        } else {
            _addressSuggestions.value = emptyList()
        }
    }

    fun clearAddressSuggestions() {
        _addressSuggestions.value = emptyList()
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

    fun saveActivity(activity: TravelActivityState) {
        _uiState.update { state ->
            val exists = state.activities.any { it.id == activity.id }
            val newActivities = if (exists) {
                state.activities.map { if (it.id == activity.id) activity else it }
            } else {
                state.activities + activity
            }
            state.copy(activities = newActivities)
        }
    }

    fun removeActivity(activityId: UUID) {
        _uiState.update { state ->
            state.copy(activities = state.activities.filter { it.id != activityId })
        }
    }

    fun saveDeparture(departure: TravelCreationDepartureState) {
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

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, fieldErrors = it.fieldErrors - "title") }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription, fieldErrors = it.fieldErrors - "description") }
    }

    fun updateDurationDays(newDuration: String) {
        if (newDuration.isBlank()) {
            _uiState.update { it.copy(durationDays = null, fieldErrors = it.fieldErrors - "durationDays") }
        } else if (newDuration.all { it.isDigit() }) {
            _uiState.update { it.copy(durationDays = newDuration.toIntOrNull(), fieldErrors = it.fieldErrors - "durationDays") }
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
                    errors["title"] = R.string.error_title_invalid
                }

                if (currentState.description.isBlank() || currentState.description.length > 10000 || currentState.description.length < 20) {
                    errors["description"] = R.string.error_description_invalid
                }

                if (currentState.latitude == null || currentState.longitude == null || 
                    currentState.country.isBlank() || currentState.destination.isBlank()) {
                    errors["destination"] = R.string.error_destination_invalid
                } else {
                    if (currentState.latitude !in -90.0..90.0 || currentState.longitude !in -180.0..180.0) {
                        errors["destination"] = R.string.error_destination_invalid
                    }
                }

                val duration = currentState.durationDays
                if (duration == null || duration < 1) {
                    errors["durationDays"] = R.string.error_duration_invalid
                }
                if (duration != null && duration < _uiState.value.activities.size){
                    errors["durationDays"] = R.string.duration_less_than_activties
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
        val state = uiState.value
        val travelImgIds: List<UUID> = state.images.mapNotNull { it.imageUUID }

        val mappedActivities = state.activities.map { act ->
            ActivityCreateRequest(
                name = act.name,
                description = act.description,
                destination = act.destination,
                continent = ActivityCreateRequest.Continent.valueOf(act.continent.name),
                address = act.address,
                country = act.country,
                latitude = act.latitude,
                longitude = act.longitude,
                dayNumber = act.dayNumber,
                imageIds = act.images.mapNotNull { it.imageUUID },
                departures = null
            )
        }

        val mappedDepartures = state.departures.map { dep ->
            TravelDepartureCreateRequest(
                startDate = dep.startDate!!,
                endDate = dep.endDate!!,
                price = dep.price!!,
                maxSlots = dep.maxSlots!!
            )
        }

        val mappedTags = state.tags.mapNotNull { tag ->
            tag.id?.let { uuid ->
                TravelTagRequest(
                    tagId = uuid,
                    score = state.tagScores[uuid] ?: 3
                )
            }
        }

        val travelCreationDto = TravelCreateRequest(
            title = state.title,
            description = state.description,
            continent = state.continent,
            durationDays = state.durationDays ?: 1,
            country = state.country.ifBlank { null },
            destination = state.destination.ifBlank { null },
            latitude = state.latitude,
            longitude = state.longitude,
            imageIds = travelImgIds,
            departures = mappedDepartures,
            activities = mappedActivities,
            tagScores = mappedTags
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isCreationLoading = true, creationErrorMessage = null) }
            val response = travelRepository.createTravel(travelCreationDto)
            if (response.success && response.data != null) {
                _uiState.update { it.copy(isCreationLoading = false, isCreationSuccess = true) }
            } else {
                _uiState.update { it.copy(isCreationLoading = false, creationErrorMessage = response.errorMessage ?: "Errore sconosciuto durante la creazione") }
            }
        }
    }

    fun updateTagScore(tagId: UUID, score: Int) {
        _uiState.update { state ->
            val newTagScores = state.tagScores.toMutableMap()
            newTagScores[tagId] = score
            state.copy(tagScores = newTagScores)
        }
    }
}