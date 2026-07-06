package it.roadies.android_app.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.R
import it.roadies.android_app.client.models.travel.LocationType
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.client.models.travel.TagResponse
import it.roadies.android_app.client.models.travel.TravelTagRequest
import it.roadies.android_app.client.models.travel.TravelUpdateRequest
import it.roadies.android_app.repository.LocationRepository
import it.roadies.android_app.repository.MetadataRepository
import it.roadies.android_app.repository.ReviewRepository
import it.roadies.android_app.repository.TravelRepository
import it.roadies.android_app.repository.UserRepository
import it.roadies.android_app.client.models.review.ReplyRequest
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.ui.travel.components.UploadableImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.core.net.toUri
import it.roadies.android_app.client.models.travel.ActivityCreateRequest
import it.roadies.android_app.client.models.travel.ActivityUpdateRequest
import it.roadies.android_app.client.models.travel.TravelDepartureUpdateRequest
import java.time.LocalDate
import java.math.BigDecimal
import it.roadies.android_app.client.models.travel.TravelDepartureCreateRequest
import it.roadies.android_app.client.models.travel.TravelDepartureResponse

data class TravelActivityUpdateState(
    val id: UUID = UUID.randomUUID(),
    val isNew: Boolean = false,
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val addressSearchQuery: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val dayNumber: Int = 0,
    val continent: TravelUpdateRequest.Continent = TravelUpdateRequest.Continent.EUROPE,
    val country: String = "",
    val destination: String = "",
    val images: List<UploadableImage> = emptyList(),
    val fieldErrors: Map<String, Int> = emptyMap()
)

data class TravelDepartureUpdateState(
    val id: UUID = UUID.randomUUID(),
    val isNew: Boolean = false,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val price: BigDecimal? = null,
    val maxSlots: Int? = null,
    val status: TravelDepartureResponse.Status? = null,
    val fieldErrors: Map<String, Int> = emptyMap()
)

data class TravelUpdateUiState(
    val isInitialLoading: Boolean = false,
    val errorMessage: String? = null,
    
    val isUpdating: Boolean = false,
    val updateErrorMessage: String? = null,
    val isUpdateSuccess: Boolean = false,

    val title: String = "",
    val description: String = "",
    val continent: TravelUpdateRequest.Continent = TravelUpdateRequest.Continent.EUROPE,
    val country: String = "",
    val destination: String = "",
    val durationDays: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    val images: List<UploadableImage> = emptyList(),
    val tags: List<TagResponse> = emptyList(),
    val tagScores: Map<UUID, Int> = emptyMap(),
    
    val activities: List<TravelActivityUpdateState> = emptyList(),
    val departures: List<TravelDepartureUpdateState> = emptyList(),
    
    val fieldErrors: Map<String, Int> = emptyMap(),
    val isEditable: Boolean = true
)

@HiltViewModel
class TravelUpdateViewModel @Inject constructor(
    private val travelRepository: TravelRepository,
    private val locationRepository: LocationRepository,
    private val metadataRepository: MetadataRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val travelId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(TravelUpdateUiState())
    val uiState = _uiState.asStateFlow()

    private val _locationSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val locationSuggestions: StateFlow<List<SearchSuggestion>> = _locationSuggestions.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val addressSuggestions: StateFlow<List<SearchSuggestion>> = _addressSuggestions.asStateFlow()

    private val _reviewsState = MutableStateFlow(TravelReviewsState())
    val reviewsState = _reviewsState.asStateFlow()

    init {
        loadTagsAndTravel()

        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            _reviewsState.value = _reviewsState.value.copy(currentUserId = user?.id)
        }

        val uuid = runCatching { UUID.fromString(travelId) }.getOrNull()
        if (uuid != null) loadReviews(uuid)
    }

    private fun loadReviews(id: UUID) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val reviewsResponse = reviewRepository.getReviews(id)
            if (reviewsResponse.success && reviewsResponse.data != null) {
                val reviews = reviewsResponse.data
                val userIds = reviews.map { it.userId }.toSet().toList()
                var usersMap = emptyMap<String, MinimalInformationResponseDTO>()

                if (userIds.isNotEmpty()) {
                    val usersResponse = userRepository.getOrganizersInfo(userIds)
                    if (usersResponse.success && usersResponse.data != null) {
                        usersMap = usersResponse.data.associateBy { it.keycloakId ?: "" }.filterKeys { it.isNotEmpty() }
                    }
                }

                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    reviews = reviews,
                    usersInfo = usersMap,
                    errorMessage = null
                )
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = reviewsResponse.errorMessage ?: "Error while fetching reviews"
                )
            }
        }
    }

    fun replyToReview(reviewId: UUID, replyContent: String) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val response = reviewRepository.createReply(reviewId, ReplyRequest(content = replyContent))
            if (response.success) {
                val uuid = runCatching { UUID.fromString(travelId) }.getOrNull()
                if (uuid != null) loadReviews(uuid)
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error replying to review"
                )
            }
        }
    }

    fun editReply(replyId: UUID, newContent: String) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val response = reviewRepository.updateReply(replyId, ReplyRequest(content = newContent))
            if (response.success) {
                val uuid = runCatching { UUID.fromString(travelId) }.getOrNull()
                if (uuid != null) loadReviews(uuid)
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error updating reply"
                )
            }
        }
    }

    fun deleteReply(replyId: UUID) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val response = reviewRepository.deleteReply(replyId)
            if (response.success) {
                val uuid = runCatching { UUID.fromString(travelId) }.getOrNull()
                if (uuid != null) loadReviews(uuid)
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error deleting reply"
                )
            }
        }
    }

    private fun loadTagsAndTravel() {
        viewModelScope.launch {
            if (_uiState.value.title.isEmpty()) {
                _uiState.value = _uiState.value.copy(isInitialLoading = true)
            }
            
            val tagsResponse = metadataRepository.getTags()
            if (tagsResponse.success && tagsResponse.data != null) {
                val availableTags = tagsResponse.data

                val uuid = runCatching { UUID.fromString(travelId) }.getOrNull()
                if (uuid != null) {
                    val travelResponse = travelRepository.getTravelById(id = uuid)
                    if (travelResponse.success && travelResponse.data != null) {
                        val travel = travelResponse.data

                        val mappedContinent = try {
                            TravelUpdateRequest.Continent.valueOf(travel.continent?.value ?: "EUROPE")
                        } catch (e: Exception) {
                            TravelUpdateRequest.Continent.EUROPE
                        }

                        val existingImages = travel.images?.mapNotNull { img -> 
                            img.id?.let { imgId ->
                                val fixedUrl = img.url?.replace("localhost", "10.133.123.48")?.replace("127.0.0.1", "10.133.123.48") ?: ""
                                UploadableImage(
                                    localUri = fixedUrl.toUri(),
                                    imageUUID = imgId, 
                                    isUploading = false
                                )
                            }
                        } ?: emptyList()
                        
                        val existingScores = travel.tagScores?.associate { 
                            it.tagId!! to (it.score ?: 3) 
                        } ?: emptyMap()

                        val mappedActivities = travel.activities?.map { activity ->
                            val activityContinent = try {
                                TravelUpdateRequest.Continent.valueOf(activity.continent?.value ?: "EUROPE")
                            } catch (e: Exception) {
                                TravelUpdateRequest.Continent.EUROPE
                            }
                            val activityImages = activity.images?.mapNotNull { img ->
                                img.id?.let { imgId ->
                                    val fixedUrl = img.url?.replace("localhost", "10.133.123.48")?.replace("127.0.0.1", "10.133.123.48") ?: ""
                                    UploadableImage(
                                        localUri = fixedUrl.toUri(),
                                        imageUUID = imgId,
                                        isUploading = false
                                    )
                                }
                            } ?: emptyList()

                            TravelActivityUpdateState(
                                id = activity.id ?: UUID.randomUUID(),
                                isNew = false,
                                name = activity.name ?: "",
                                description = activity.description ?: "",
                                address = activity.address ?: "",
                                addressSearchQuery = activity.address ?: "",
                                latitude = activity.latitude,
                                longitude = activity.longitude,
                                dayNumber = activity.dayNumber ?: 1,
                                continent = activityContinent,
                                country = activity.country ?: "",
                                destination = activity.destination ?: "",
                                images = activityImages
                            )
                        } ?: emptyList()

                        val mappedDepartures = travel.departures?.map { departure ->
                            TravelDepartureUpdateState(
                                id = departure.id ?: UUID.randomUUID(),
                                isNew = false,
                                startDate = departure.startDate,
                                endDate = departure.endDate,
                                price = departure.price,
                                maxSlots = departure.maxSlots,
                                status = departure.status
                            )
                        } ?: emptyList()

                        _uiState.value = _uiState.value.copy(
                            isInitialLoading = false,
                            tags = availableTags,
                            title = travel.title ?: "",
                            description = travel.description ?: "",
                            continent = mappedContinent,
                            country = travel.country ?: "",
                            destination = travel.destination ?: "",
                            durationDays = travel.durationDays,
                            latitude = travel.latitude,
                            longitude = travel.longitude,
                            images = existingImages,
                            tagScores = existingScores,
                            activities = mappedActivities,
                            departures = mappedDepartures,
                            isEditable = travel.editable ?: true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isInitialLoading = false, errorMessage = travelResponse.errorMessage ?: "Failed to load travel details")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isInitialLoading = false, errorMessage = "Travel id not valid")
                }
            } else {
                _uiState.value = _uiState.value.copy(isInitialLoading = false, errorMessage = tagsResponse.errorMessage ?: "Failed to load tags")
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle, fieldErrors = _uiState.value.fieldErrors - "title")
    }

    fun updateDescription(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription, fieldErrors = _uiState.value.fieldErrors - "description")
    }

    fun updateDurationDays(newDuration: String) {
        val days = newDuration.toIntOrNull()
        _uiState.value = _uiState.value.copy(durationDays = days, fieldErrors = _uiState.value.fieldErrors - "durationDays")
    }

    fun updateTagScore(tagId: UUID, score: Int) {
        val newScores = _uiState.value.tagScores.toMutableMap()
        newScores[tagId] = score
        _uiState.value = _uiState.value.copy(tagScores = newScores)
    }

    fun updateLocation(destination: String, country: String, continent: TravelUpdateRequest.Continent, latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            destination = destination,
            country = country,
            continent = continent,
            latitude = latitude,
            longitude = longitude,
            fieldErrors = _uiState.value.fieldErrors - "destination"
        )
    }

    fun searchDestinations(query: String) {
        _uiState.value = _uiState.value.copy(destination = query, fieldErrors = _uiState.value.fieldErrors - "destination")
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

        if (errors.isNotEmpty()) {
            _uiState.value = currentState.copy(fieldErrors = errors)
            return
        }

        val uuid = runCatching { UUID.fromString(travelId) }.getOrNull()
        if (uuid != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
                
                val currentImages = _uiState.value.images
                val newImagesToUpload = currentImages.filter { it.imageUUID == null }
                val successfullyUploadedIds = mutableListOf<UUID>()
                var uploadFailed = false

                for (img in newImagesToUpload) {
                    _uiState.value = _uiState.value.copy(images = _uiState.value.images.map { if (it.localUri == img.localUri) it.copy(isUploading = true) else it })
                    
                    try {
                        val inputStream = context.contentResolver.openInputStream(img.localUri)
                        if (inputStream != null) {
                            val bytes = inputStream.readBytes()
                            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
                            
                            val uploadResponse = travelRepository.uploadImage(body)
                            if (uploadResponse.success && uploadResponse.data?.id != null) {
                                successfullyUploadedIds.add(uploadResponse.data.id)
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

                    val currentState = _uiState.value
                    val request = TravelUpdateRequest(
                        title = currentState.title,
                        description = currentState.description,
                        continent = currentState.continent,
                        country = currentState.country,
                        destination = currentState.destination,
                        latitude = currentState.latitude,
                        longitude = currentState.longitude,
                        durationDays = currentState.durationDays,
                        imageIds = finalImageIds,
                        tagScores = currentState.tagScores.map { TravelTagRequest(tagId = it.key, score = it.value) }
                    )

                    val updateResponse = travelRepository.updateTravel(uuid, request)
                    
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

    fun removeActivity(activityId: UUID) {
        val tId = runCatching { UUID.fromString(travelId) }.getOrNull()
        if (tId != null) {
            viewModelScope.launch {
                val response = travelRepository.deleteActivity(tId, activityId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(activities = _uiState.value.activities.filter { it.id != activityId })
                } else {
                    _uiState.value = _uiState.value.copy(updateErrorMessage = response.errorMessage ?: "Failed to delete activity")
                }
            }
        }
    }

    fun saveActivity(activity: TravelActivityUpdateState) {
        val id = runCatching { UUID.fromString(travelId) }.getOrNull()
        if (id != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)

                val currentImages = activity.images
                val newImagesToUpload = currentImages.filter { it.imageUUID == null }
                var uploadFailed = false
                val updatedImagesList = currentImages.toMutableList()

                for (img in newImagesToUpload) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(img.localUri)
                        if (inputStream != null) {
                            val bytes = inputStream.readBytes()
                            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
                            
                            val uploadResponse = travelRepository.uploadImage(body)
                            if (uploadResponse.success && uploadResponse.data?.id != null) {
                                val idx = updatedImagesList.indexOfFirst { it.localUri == img.localUri }
                                if (idx != -1) {
                                    updatedImagesList[idx] = updatedImagesList[idx].copy(isUploading = false, imageUUID = uploadResponse.data.id)
                                }
                            } else {
                                uploadFailed = true
                            }
                        }
                    } catch (e: Exception) {
                        uploadFailed = true
                    }
                }

                if (!uploadFailed) {
                    val finalImageIds = updatedImagesList.mapNotNull { it.imageUUID }
                    
                    if (activity.isNew) {
                        val request = ActivityCreateRequest(
                            name = activity.name,
                            description = activity.description,
                            continent = ActivityCreateRequest.Continent.valueOf(activity.continent.name),
                            country = activity.country,
                            destination = activity.destination,
                            address = activity.address,
                            latitude = activity.latitude,
                            longitude = activity.longitude,
                            dayNumber = activity.dayNumber,
                            imageIds = finalImageIds
                        )
                        val response = travelRepository.createActivity(id, request)
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(isUpdating = false)
                            loadTagsAndTravel()
                        } else {
                            _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to create activity")
                        }
                    } else {
                        val request = ActivityUpdateRequest(
                            continent = ActivityUpdateRequest.Continent.valueOf(activity.continent.name),
                            name = activity.name,
                            description = activity.description,
                            country = activity.country,
                            destination = activity.destination,
                            address = activity.address,
                            latitude = activity.latitude,
                            longitude = activity.longitude,
                            dayNumber = activity.dayNumber,
                            imageIds = finalImageIds
                        )
                        val response = travelRepository.updateActivity(id, activity.id, request)
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(isUpdating = false)
                            loadTagsAndTravel()
                        } else {
                            _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to update activity")
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Failed to upload some images for activity")
                }
            }
        }
    }

    fun saveDeparture(departure: TravelDepartureUpdateState) {
        viewModelScope.launch {
            val errors = mutableMapOf<String, Int>()
            if (departure.startDate == null) {
                errors["startDate"] = R.string.error_start_date_invalid
            } else if (!departure.startDate.isAfter(LocalDate.now())) {
                errors["startDate"] = R.string.error_date_past
            }

            if (departure.endDate == null || (departure.startDate != null && departure.endDate.isBefore(departure.startDate))) {
                errors["endDate"] = R.string.error_end_date_invalid
            }

            if (departure.startDate != null && departure.endDate != null && !departure.endDate.isBefore(departure.startDate)) {
                val travelDuration = _uiState.value.durationDays
                if (travelDuration != null) {
                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(departure.startDate, departure.endDate).toInt()
                    if (daysBetween != travelDuration) {
                        errors["duration"] = R.string.error_departure_duration
                    }
                }
            }

            if (departure.price == null || departure.price <= BigDecimal.ZERO) errors["price"] = R.string.error_price_invalid
            if (departure.maxSlots == null || departure.maxSlots <= 0) errors["maxSlots"] = R.string.error_max_slots_invalid

            if (errors.isNotEmpty()) {
                val updatedDepartures = _uiState.value.departures.map {
                    if (it.id == departure.id) it.copy(fieldErrors = errors) else it
                }
                _uiState.value = _uiState.value.copy(departures = updatedDepartures)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
            val tId = runCatching { UUID.fromString(travelId) }.getOrNull()
            if (tId != null) {
                if (departure.isNew) {
                    val request = TravelDepartureCreateRequest(
                        startDate = departure.startDate!!,
                        endDate = departure.endDate!!,
                        price = departure.price!!,
                        maxSlots = departure.maxSlots!!
                    )
                    val response = travelRepository.addDeparture(tId, request)
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                        loadTagsAndTravel()
                    } else {
                        _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to create departure")
                    }
                } else {
                    val request = TravelDepartureUpdateRequest(
                        startDate = departure.startDate!!,
                        endDate = departure.endDate!!,
                        price = departure.price!!,
                        maxSlots = departure.maxSlots!!

                    )
                    val response = travelRepository.updateDeparture(tId, departure.id, request)
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(isUpdating = false)
                        loadTagsAndTravel()
                    } else {
                        _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to update departure")
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Travel ID invalid")
            }
        }
    }

    fun removeDeparture(departureId: UUID) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
            val tId = runCatching { UUID.fromString(travelId) }.getOrNull()
            if (tId != null) {
                val response = travelRepository.deleteDeparture(tId, departureId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadTagsAndTravel()
                } else {
                    _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to delete departure")
                }
            } else {
                _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Travel ID invalid")
            }
        }
    }

    fun confirmDeparture(departureId: java.util.UUID) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, updateErrorMessage = null)
            val tId = runCatching { UUID.fromString(travelId) }.getOrNull()
            if (tId != null) {
                val response = travelRepository.confirmDeparture(tId, departureId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                    loadTagsAndTravel()
                } else {
                    _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = response.errorMessage ?: "Failed to confirm departure")
                }
            } else {
                _uiState.value = _uiState.value.copy(isUpdating = false, updateErrorMessage = "Travel ID invalid")
            }
        }
    }
}