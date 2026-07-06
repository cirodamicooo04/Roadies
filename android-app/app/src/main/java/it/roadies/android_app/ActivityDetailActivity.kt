package it.roadies.android_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.roadies.android_app.client.models.travel.ActivityResponse
import it.roadies.android_app.client.models.travel.ActivityDepartureResponse
import it.roadies.android_app.ui.travel.components.BoxCentered
import it.roadies.android_app.ui.travel.components.CheckAvailabilityButton
import it.roadies.android_app.ui.travel.components.DetailHeader
import it.roadies.android_app.ui.travel.components.DetailImageCarousel
import it.roadies.android_app.ui.travel.components.ExpandableDescription
import it.roadies.android_app.ui.travel.components.LocationMap
import it.roadies.android_app.ui.travel.components.OrganizerCard
import it.roadies.android_app.ui.travel.components.Reviews
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.viewmodel.ActivityDetailViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(navHostController: NavHostController, onLoginRequest: () -> Unit, viewModel: ActivityDetailViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsState()
    val departuresState by viewModel.departuresState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()

    val departuresSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isDeparturesSheetOpen by remember { mutableStateOf(false) }

    if (uiState.isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
    } else if (uiState.errorMessage != null){
        BoxCentered(text = uiState.errorMessage)
    } else {
        ActivityDetail(
            activity = uiState.activity, 
            organizerInfo = uiState.organizerInfo,
            onCheckAvailability = {
                viewModel.loadDepartures()
                isDeparturesSheetOpen = true
            }, 
            onFavoriteClick = { activityId -> //vincenzo usa activity id
            },
            reviewsState = reviewsState,
            onDeleteReview = { reviewId -> viewModel.deleteReview(reviewId) },
            onEditReview = { reviewId, rating, content -> viewModel.updateReview(reviewId, rating, content) },
            onReplyReview = { reviewId, content -> viewModel.replyToReview(reviewId, content) },
            onOrganizerClick = { username -> 
                val isMe = uiState.currentUserId == uiState.organizerInfo?.keycloakId
                if (isMe) {
                    navHostController.navigate("profile_graph")
                } else {
                    navHostController.navigate("user_profile/$username")
                }
            }
        )
    }

    LaunchedEffect(uiState.requireLogin) {
        if (uiState.requireLogin){
            onLoginRequest()
            viewModel.onLoginHandled()
        }
    }

    LaunchedEffect(uiState.createdBookingId) {
        uiState.createdBookingId?.let { bookingId ->
            uiState.selectedDepartureId?.let { departureId ->
                navHostController.navigate("booking_people/$bookingId?activityId=$departureId")
                viewModel.onBookingNavigated()
            }
        }
    }

    if (isDeparturesSheetOpen && !uiState.isLoading && uiState.errorMessage == null){

        ModalBottomSheet(sheetState = departuresSheetState, onDismissRequest = {isDeparturesSheetOpen = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(0.8f)){
                Text(
                    text = stringResource(R.string.available_departures),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (departuresState.isLoading){
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                } else if (departuresState.errorMessage != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center){
                        Text(text = departuresState.errorMessage ?: "")
                    }
                } else {
                    val departuresList = departuresState.departures
                    if (departuresList.isNullOrEmpty()){
                        BoxCentered(text = stringResource(R.string.no_available_departures))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                            items(departuresList) { departure ->
                                DepartureCard(
                                    departure = departure,
                                    onBookClick = {
                                        val uuid = runCatching { java.util.UUID.fromString(departure.id.toString()) }.getOrNull()
                                        if (uuid != null) {
                                            viewModel.createDraftBooking(uuid)
                                            isDeparturesSheetOpen = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityDetail(
    activity: ActivityResponse?, 
    organizerInfo: MinimalInformationResponseDTO?,
    onCheckAvailability: () -> Unit, 
    onFavoriteClick: (UUID) -> Unit,
    reviewsState: it.roadies.android_app.viewmodel.ActivityReviewsState,
    onDeleteReview: (UUID) -> Unit,
    onEditReview: (UUID, Int, String) -> Unit,
    onReplyReview: (UUID, String) -> Unit,
    onOrganizerClick: (String) -> Unit
){
    var isFavorite by remember { mutableStateOf(false) }

    if (activity == null){
        BoxCentered(text = stringResource(R.string.error_loading_travel))
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                //Header
                DetailHeader(
                    title = activity.name,
                    destination = activity.destination,
                    country = activity.country,
                    isFavorite = isFavorite,
                    onFavoriteClick = { activity.id?.let { id -> onFavoriteClick(id) } }
                ) {
                    val durationHours = activity.departures?.firstOrNull()?.let {
                        if (it.startTimestamp != null && it.endTimestamp != null) {
                            Duration.between(it.startTimestamp, it.endTimestamp).toHours()
                        } else null
                    }

                    if (durationHours != null && durationHours > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Duration",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$durationHours ${stringResource(R.string.hours)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    } else if (!activity.type.isNullOrEmpty()) {
                        Text(
                            text = activity.type.uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                //Immagini (LazyRow)
                DetailImageCarousel(activity.images)

                //activity description
                ExpandableDescription(activity.description)

                //mappa
                LocationMap(lon = activity.longitude, lat = activity.latitude)

                OrganizerCard(
                    organizerInfo = organizerInfo,
                    onOrganizerClick = onOrganizerClick
                )

                Reviews(
                    isLoading = reviewsState.isLoading,
                    errorMessage = reviewsState.errorMessage,
                    reviews = reviewsState.reviews,
                    usersInfo = reviewsState.usersInfo,
                    currentUserId = reviewsState.currentUserId,
                    travelOwnerId = activity.ownerId,
                    onDeleteReview = onDeleteReview,
                    onEditReview = onEditReview,
                    onReplyReview = onReplyReview
                )
            }

            CheckAvailabilityButton(onClick = {
                onCheckAvailability()
            })
        }
    }
}



@Composable
fun DepartureCard(departure: ActivityDepartureResponse, onBookClick: () -> Unit) {
    val isAvailable = (departure.availableSlots ?: 0) > 0
    val isConfirmed = departure.status == ActivityDepartureResponse.Status.CONFIRMED
    val isBookable = isAvailable && isConfirmed

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    val formattedDate = departure.startTimestamp?.toLocalDate()?.format(dateFormatter) ?: stringResource(R.string.to_be_decided_abbr)
    val startTime = departure.startTimestamp?.toLocalTime()?.format(timeFormatter) ?: ""
    val endTime = departure.endTimestamp?.toLocalTime()?.format(timeFormatter) ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Sezione date - Prima riga
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$startTime - $endTime",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sezione prezzo, disponibilità e pulsante - Seconda riga
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info prezzo e disponibilità a sinistra
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${departure.price ?: stringResource(R.string.not_available_abbr)} €",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!isAvailable) {
                        Text(
                            text = stringResource(R.string.sold_out),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.available_seats, departure.availableSlots ?: 0),
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }

                // Pulsante prenota a destra
                Button(
                    onClick = onBookClick,
                    enabled = isBookable,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isConfirmed) stringResource(R.string.book_now) else stringResource(R.string.in_planning))
                }
            }
        }
    }
}