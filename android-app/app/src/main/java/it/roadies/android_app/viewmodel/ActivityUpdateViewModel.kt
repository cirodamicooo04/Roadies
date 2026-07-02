package it.roadies.android_app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.roadies.android_app.R
import it.roadies.android_app.client.models.travel.ActivityDepartureCreateRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureResponse
import it.roadies.android_app.client.models.travel.ActivityDepartureUpdateRequest
import it.roadies.android_app.client.models.travel.ActivityUpdateRequest
import it.roadies.android_app.client.models.travel.LocationType
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.repository.ActivityRepository
import it.roadies.android_app.repository.LocationRepository
import it.roadies.android_app.ui.travel.components.UploadableImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class ActivityDepartureUpdateState(
    val id: UUID = UUID.randomUUID(),
    val isNew: Boolean = false,
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val price: BigDecimal? = null,
    val maxSlots: Int? = null,
    val status: ActivityDepartureResponse.Status? = null,
    val fieldErrors: Map<String, Int> = emptyMap()
)

data class ActivityUpdateUiState(
    val isInitialLoading: Boolean = false,
    val errorMessage: String? = null,
    
    val isUpdating: Boolean = false,
    val updateErrorMessage: String? = null,
    val isUpdateSuccess: Boolean = false,

    val name: String = "",
    val description: String = "",
    val address: String = "",
    val continent: ActivityUpdateRequest.Continent = ActivityUpdateRequest.Continent.EUROPE,
    val country: String = "",
    val destination: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val dayNumber: Int = 1,
    
    val images: List<UploadableImage> = emptyList(),
    val departures: List<ActivityDepartureUpdateState> = emptyList(),
    
    val fieldErrors: Map<String, Int> = emptyMap(),
    val isEditable: Boolean = true
)

@HiltViewModel
class ActivityUpdateViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    val activityId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(ActivityUpdateUiState())
    val uiState = _uiState.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val addressSuggestions: StateFlow<List<SearchSuggestion>> = _addressSuggestions.asStateFlow()

    init {
        loadActivity()
    }

    private fun loadActivity() {
        viewModelScope.launch {
            if (_uiState.value.name.isEmpty()) {
                _uiState.value = _uiState.value.copy(isInitialLoading = true)
            }
            val uuid = runCatching { UUID.fromString(activityId) }.getOrNull()
            
            if (uuid != null) {
                val response = activityRepository.getActivityById(uuid)
                if (response.success && response.data != null) {
                    val activity = response.data

                    val mappedContinent = try {
                        ActivityUpdateRequest.Continent.valueOf(activity.continent?.value ?: "EUROPE")
                    } catch (e: Exception) {
                        ActivityUpdateRequest.Continent.EUROPE
                    }

                    val existingImages = activity.images?.mapNotNull { img -> 
                        img.id?.let { imgId ->
                            val fixedUrl = img.url?.replace("localhost", "10.0.2.2")?.replace("127.0.0.1", "10.0.2.2") ?: ""
                            UploadableImage(
                                localUri = fixedUrl.toUri(),
                                imageUUID = imgId, 
                                isUploading = false
                            )
                        }
                    } ?: emptyList()
                    
                    val mappedDepartures = activity.departures?.map { departure ->
                        val localStart = departure.startTimestamp?.toZonedDateTime()?.withZoneSameInstant(ZoneId.systemDefault())
                        val localEnd = departure.endTimestamp?.toZonedDateTime()?.withZoneSameInstant(ZoneId.systemDefault())

                        ActivityDepartureUpdateState(
                            id = departure.id ?: UUID.randomUUID(),
                            isNew = false,
                            date = localStart?.toLocalDate(),
                            startTime = localStart?.toLocalTime(),
                            endTime = localEnd?.toLocalTime(),
                            price = departure.price,
                            maxSlots = departure.maxSlots,
                            status = departure.status
                        )
                    } ?: emptyList()

                    _uiState.value = _uiState.value.copy(
                        isInitialLoading = false,
                        name = activity.name ?: "",
                        description = activity.description ?: "",
                        address = activity.address ?: "",
                        continent = mappedContinent,
                        country = activity.country ?: "",
                        destination = activity.destination ?: "",
                        latitude = activity.latitude,
                        longitude = activity.longitude,
                        dayNumber = activity.dayNumber ?: 1,
                        images = existingImages,
                        departures = mappedDepartures,
                        isEditable = activity.editable ?: true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isInitialLoading = false, errorMessage = response.errorMessage ?: "Failed to load activity details")
                }
            } else {
                _uiState.value = _uiState.value.copy(isInitialLoading = false, errorMessage = "Activity id not valid")
            }
        }
    }

    fun updateName(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName, fieldErrors = _uiState.value.fieldErrors - "name")
    }

    fun updateDescription(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription, fieldErrors = _uiState.value.fieldErrors - "description")
    }

    fun selectAddressSuggestion(suggestion: SearchSuggestion) {
        val displayLocation = if (!suggestion.country.isNullOrBlank()) "${suggestion.name}, ${suggestion.country}" else suggestion.name
        val mappedContinent = try {
            ActivityUpdateRequest.Continent.valueOf(it.roadies.android_app.utils.ContinentMapper.getContinent(suggestion.countryCode).name)
        } catch (e: Exception) {
            ActivityUpdateRequest.Continent.EUROPE
        }

        _uiState.value = _uiState.value.copy(
            address = displayLocation,
            destination = suggestion.name,
            country = suggestion.country ?: "",
            continent = mappedContinent,
            latitude = suggestion.latitude,
            longitude = suggestion.longitude,
            fieldErrors = _uiState.value.fieldErrors - "address"
        )
        clearAddressSuggestions()
    }

    fun searchAddresses(query: String) {
        _uiState.value = _uiState.value.copy(address = query)
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

    fun uploadImagesFromGallery(uris: List<Uri>) {
        val newImages = uris.map { UploadableImage(localUri = it, isUploading = false) }
        _uiState.value = _uiState.value.copy(images = _uiState.value.images + newImages)
    }

    fun removeImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(images = _uiState.value.images.filter { img -> img.localUri != uri })
    }

    fun submitUpdate() {
        val currentState = _uiState.value
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

        val uuid = runCatching { UUID.fromString(activityId) }.getOrNull()
        if (uuid != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
                
                val currentImages = _uiState.value.images
                val newImagesToUpload = currentImages.filter { it.imageUUID == null }
                var uploadFailed = false

                for (img in newImagesToUpload) {
                    _uiState.value = _uiState.value.copy(images = _uiState.value.images.map { if (it.localUri == img.localUri) it.copy(isUploading = true) else it })
                    try {
                        val inputStream = context.contentResolver.openInputStream(img.localUri)
                        if (inputStream != null) {
                            val bytes = inputStream.readBytes()
                            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
                            
                            val uploadResponse = activityRepository.uploadImage(body)
                            if (uploadResponse.success && uploadResponse.data?.id != null) {
                                _uiState.value = _uiState.value.copy(images = _uiState.value.images.map { 
                                    if (it.localUri == img.localUri) it.copy(isUploading = false, imageUUID = uploadResponse.data.id) else it 
                                })
                            } else {
                                uploadFailed = true
                                _uiState.value = _uiState.value.copy(images = _uiState.value.images.map { if (it.localUri == img.localUri) it.copy(isUploading = false, isFailed = true) else it })
                            }
                        }
                    } catch (e: Exception) {
                        uploadFailed = true
                        _uiState.value = _uiState.value.copy(images = _uiState.value.images.map { if (it.localUri == img.localUri) it.copy(isUploading = false, isFailed = true) else it })
                    }
                }

                if (!uploadFailed) {
                    val finalImageIds = _uiState.value.images.mapNotNull { it.imageUUID }

                    val request = ActivityUpdateRequest(
                        name = currentState.name,
                        description = currentState.description,
                        continent = currentState.continent,
                        country = currentState.country,
                        destination = currentState.destination,
                        address = currentState.address,
                        latitude = currentState.latitude,
                        longitude = currentState.longitude,
                        dayNumber = currentState.dayNumber,
                        imageIds = finalImageIds
                    )

                    val updateResponse = activityRepository.updateActivity(uuid, request)
                    
                    if (updateResponse.success) {
                        _uiState.value = _uiState.value.copy(isUpdating = false, isUpdateSuccess = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = updateResponse.errorMessage ?: "Update failed.")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Failed to upload some images.")
                }
            }
        }
    }

    fun saveDeparture(departure: ActivityDepartureUpdateState) {
        viewModelScope.launch {
            val errors = mutableMapOf<String, Int>()
            if (departure.date == null) {
                errors["date"] = R.string.error_start_date_invalid
            } else if (!departure.date.isAfter(LocalDate.now())) {
                errors["date"] = R.string.error_date_past
            }

            if (departure.startTime == null) errors["startTime"] = R.string.error_time_invalid
            if (departure.endTime == null) errors["endTime"] = R.string.error_time_invalid

            if (departure.startTime != null && departure.endTime != null) {
                if (!departure.endTime.isAfter(departure.startTime)) {
                    errors["endTime"] = R.string.error_time_range
                }
            }

            if (departure.price == null || departure.price <= BigDecimal.ZERO) errors["price"] = R.string.error_price_invalid
            if (departure.maxSlots == null || departure.maxSlots <= 0) errors["maxSlots"] = R.string.error_max_slots_invalid

            if (errors.isNotEmpty()) {
                val updatedDepartures = _uiState.value.departures.map {
                    if (it.id == departure.id) it.copy(fieldErrors = errors) else it
                }
                _uiState.value = _uiState.value.copy(departures = updatedDepartures)
                return@launch //si usa per uscire dalla coroutine senza errore, con return normale da errore
            }

            _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
            val aId = runCatching { UUID.fromString(activityId) }.getOrNull()
            if (aId != null) {
                if (departure.isNew) {
                    val request = ActivityDepartureCreateRequest(
                        startTimestamp = ZonedDateTime.of(departure.date, departure.startTime, ZoneId.systemDefault()).toOffsetDateTime(),
                        endTimestamp = ZonedDateTime.of(departure.date, departure.endTime, ZoneId.systemDefault()).toOffsetDateTime(),
                        price = departure.price!!,
                        maxSlots = departure.maxSlots!!
                    )
                    val response = activityRepository.addDeparture(aId, request)
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                        loadActivity()
                    } else {
                        _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to create departure")
                    }
                } else {
                    val request = ActivityDepartureUpdateRequest(
                        id = departure.id,
                        startTimestamp = ZonedDateTime.of(departure.date, departure.startTime, ZoneId.systemDefault()).toOffsetDateTime(),
                        endTimestamp = ZonedDateTime.of(departure.date, departure.endTime, ZoneId.systemDefault()).toOffsetDateTime(),
                        price = departure.price!!,
                        maxSlots = departure.maxSlots!!
                    )
                    val response = activityRepository.updateDeparture(aId, departure.id, request)
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                        loadActivity()
                    } else {
                        _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to update departure")
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Activity ID invalid")
            }
        }
    }

    fun removeDeparture(departureId: UUID) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
            val aId = runCatching { UUID.fromString(activityId) }.getOrNull()
            if (aId != null) {
                val response = activityRepository.deleteDeparture(aId, departureId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadActivity()
                } else {
                    _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to delete departure")
                }
            } else {
                _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Activity ID invalid")
            }
        }
    }

    fun confirmDeparture(departureId: UUID) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
            val aId = runCatching { UUID.fromString(activityId) }.getOrNull()
            if (aId != null) {
                val response = activityRepository.confirmDeparture(aId, departureId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadActivity()
                } else {
                    _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to confirm departure")
                }
            } else {
                _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Activity ID invalid")
            }
        }
    }
}