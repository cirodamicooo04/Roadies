package it.roadies.android_app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.booking.BookingDraftRequest
import it.roadies.android_app.client.models.travel.ActivityDepartureResponse
import it.roadies.android_app.client.models.travel.ActivityResponse
import it.roadies.android_app.repository.ActivityRepository
import it.roadies.android_app.repository.AuthRepository
import it.roadies.android_app.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import it.roadies.android_app.client.models.review.ReplyRequest
import it.roadies.android_app.client.models.review.ReviewResponse
import it.roadies.android_app.client.models.review.ReviewUpdateRequest
import it.roadies.android_app.repository.ReviewRepository
import it.roadies.android_app.repository.UserRepository
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.client.models.travel.FavouriteListCreateRequest
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.repository.FavouriteRepository


data class ActivityDetailState(
    val isLoading: Boolean = false,
    val activity: ActivityResponse? = null,
    val organizerInfo: MinimalInformationResponseDTO? = null,
    val errorMessage: String? = null,
    val createdBookingId: UUID? = null,
    val selectedDepartureId: UUID? = null,
    val requireLogin: Boolean = false,
    val currentUserId: String? = null
)

data class ActivityDepartureState(
    val isLoading: Boolean = false,
    val departures: List<ActivityDepartureResponse>? = emptyList(),
    val errorMessage: String? = null
)

data class ActivityReviewsState(
    val isLoading: Boolean = false,
    val reviews: List<ReviewResponse>? = emptyList(),
    val usersInfo: Map<String, MinimalInformationResponseDTO> = emptyMap(),
    val errorMessage: String? = null,
    val currentUserId: String? = null
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val favouriteRepository: FavouriteRepository
): ViewModel() {
    val id: String? = savedStateHandle["id"]
    
    private val _uiState = MutableStateFlow(ActivityDetailState())
    val uiState = _uiState.asStateFlow()

    private val _departuresState = MutableStateFlow(ActivityDepartureState())
    val departuresState = _departuresState.asStateFlow()

    private val _reviewsState = MutableStateFlow(ActivityReviewsState())
    val reviewsState = _reviewsState.asStateFlow()

    init {
        val uuid = runCatching { UUID.fromString(id) }.getOrNull()
        if (uuid != null) loadActivity(uuid)
        else _uiState.value = ActivityDetailState(errorMessage = "Activity id not valid")

        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.value = _uiState.value.copy(currentUserId = authState.userId)
                _reviewsState.value = _reviewsState.value.copy(currentUserId = authState.userId)
            }
        }
    }

    private fun loadActivity(id: UUID){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val response = activityRepository.getActivityById(id)
            if (response.success){
                _uiState.value = _uiState.value.copy(activity = response.data, isLoading = false)
                response.data?.ownerId?.let { loadOrganizerInfo(it) }
                loadReviews(id)
            }
            else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage)
            }
        }
    }

    private fun loadOrganizerInfo(ownerId: String) {
        viewModelScope.launch {
            val response = userRepository.getUsersInfo(listOf(ownerId))
            if (response.success && response.data != null) {
                val organizer = response.data.firstOrNull()
                _uiState.value = _uiState.value.copy(organizerInfo = organizer)
            }
        }
    }

    private fun loadReviews(id: UUID) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val reviewsResponse = reviewRepository.getReviews(id)
            if (reviewsResponse.success && reviewsResponse.data != null){
                val reviews = reviewsResponse.data
                val userIds = reviews.map { it.userId }.toSet().toList()
                var usersMap = emptyMap<String, MinimalInformationResponseDTO>()
                
                if (userIds.isNotEmpty()) {
                    val usersResponse = userRepository.getUsersInfo(userIds)
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

    fun loadDepartures(){
        viewModelScope.launch {
            val uuid = runCatching { UUID.fromString(id) }.getOrNull()
            if (uuid != null){
                _departuresState.value = _departuresState.value.copy(isLoading = true)

                val response = activityRepository.getActivityDepartures(uuid)
                if (response.success ){
                    _departuresState.value = ActivityDepartureState(isLoading = false, departures = response.data)
                } else {
                    _departuresState.value = ActivityDepartureState(isLoading = false, errorMessage = response.errorMessage)
                }
            } else {
                _departuresState.value = ActivityDepartureState(isLoading = false, errorMessage = "Activity id not valid")
            }
        }
    }
    
    fun createDraftBooking(departureId: UUID) {
        val isLogged = authRepository.authState.value.isLogged
        if (!isLogged) {
            _uiState.value =_uiState.value.copy(requireLogin = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val request = BookingDraftRequest(travelId = null, activityId = departureId)
            val response = bookingRepository.createDraft(request)
            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    createdBookingId = response.data.bookingId,
                    selectedDepartureId = departureId
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.errorMessage ?: "Error with booking")
            }
        }
    }

    fun onBookingNavigated() {
        _uiState.value = _uiState.value.copy(createdBookingId = null, selectedDepartureId = null)
    }

    fun onLoginHandled() {
        _uiState.value = _uiState.value.copy(requireLogin = false)
    }

    fun deleteReview(reviewId: UUID) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val response = reviewRepository.deleteReview(reviewId)
            if (response.success) {
                val uuid = runCatching { UUID.fromString(id) }.getOrNull()
                if (uuid != null) loadReviews(uuid)
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error deleting review"
                )
            }
        }
    }

    fun updateReview(reviewId: UUID, rating: Int, newContent: String) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val request = ReviewUpdateRequest(rating = rating, content = newContent)
            val response = reviewRepository.updateReview(reviewId, request)
            if (response.success) {
                val uuid = runCatching { UUID.fromString(id) }.getOrNull()
                if (uuid != null) loadReviews(uuid)
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error updating review"
                )
            }
        }
    }

    fun replyToReview(reviewId: UUID, replyContent: String) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val request = ReplyRequest(content = replyContent)
            val response = reviewRepository.createReply(reviewId, request)
            if (response.success) {
                val uuid = runCatching { UUID.fromString(id) }.getOrNull()
                if (uuid != null) loadReviews(uuid)
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error replying to review"
                )
            }
        }
    }

    fun loadFavouriteLists(onResult: (List<FavouriteListResponse>) -> Unit) {
        viewModelScope.launch {
            val response = favouriteRepository.getMyLists()
            if (response.success && response.data != null) {
                onResult(response.data)
            } else {
                onResult(emptyList())
            }
        }
    }

    fun addActivityToFavouriteList(listId: UUID, activityId: UUID) {
        viewModelScope.launch {
            favouriteRepository.addActivityToList(listId, activityId)
        }
    }

    fun createFavouriteList(
        name: String,
        visibility: FavouriteListCreateRequest.Visibility,
        onCreated: (FavouriteListResponse) -> Unit
    ) {
        viewModelScope.launch {
            val request = FavouriteListCreateRequest(name = name, visibility = visibility)
            val response = favouriteRepository.createList(request)
            if (response.success && response.data != null) {
                onCreated(response.data)
            }
        }
    }


    fun editReply(replyId: UUID, newContent: String) {
        viewModelScope.launch {
            _reviewsState.value = _reviewsState.value.copy(isLoading = true)
            val request = ReplyRequest(content = newContent)
            val response = reviewRepository.updateReply(replyId, request)
            if (response.success) {
                val uuid = runCatching { UUID.fromString(id) }.getOrNull()
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
                val uuid = runCatching { UUID.fromString(id) }.getOrNull()
                if (uuid != null) loadReviews(uuid)
            } else {
                _reviewsState.value = _reviewsState.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage ?: "Error deleting reply"
                )
            }
        }
    }
}