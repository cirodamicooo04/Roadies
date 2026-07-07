package it.roadies.android_app.ui.bookingHome

import android.net.Uri.encode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import it.roadies.android_app.R
import it.roadies.android_app.client.models.booking.BookingHomeResponse
import it.roadies.android_app.viewmodel.bookingHome.BookingFilterType
import it.roadies.android_app.viewmodel.bookingHome.BookingHomeState
import it.roadies.android_app.viewmodel.bookingHome.BookingHomeViewModel
import it.roadies.android_app.viewmodel.bookingHome.BookingPagingState
import java.math.BigDecimal


@Composable
fun BookingHomeScreen(navHostController: NavHostController, viewModel: BookingHomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val reviewSuccessMessage = stringResource(R.string.booking_review_success)

    BookingHomeScreenContent(
        state = state, 
        onLoadMoreActive = {viewModel.loadMoreActive()},
        onLoadMorePast = {viewModel.loadMorePast()},
        onFilterChanged = { viewModel.setFilterType(it) },
        onBookingClick = { booking ->
            //uso l'encode per evitare visione di caraterri speciali
            val travelNameEncoded = encode(booking.travelName ?: "")
            navHostController.navigate("booking_detail/${booking.principalId}/$travelNameEncoded/${booking.peopleCount ?: 1}/${booking.totalPrice}/${booking.startDate}/${booking.endDate}/${booking.departureType?.name ?: "TRAVEL"}")
        },
        onDeleteBooking = { booking ->
            booking.bookingId?.let { viewModel.deleteBooking(it) }
        },
        onAddReview = { booking, rating, content ->
            viewModel.submitReview(booking, rating, content, reviewSuccessMessage)
        },
        onErrorShown = { viewModel.clearError() },
        onInfoShown = { viewModel.clearInfo() }
    )
}

@Composable
fun BookingHomeScreenContent(state: BookingHomeState, onLoadMoreActive: () -> Unit = {}, onLoadMorePast: () -> Unit = {}, onFilterChanged: (BookingFilterType) -> Unit = {}, onBookingClick: (BookingHomeResponse) -> Unit = {}, onDeleteBooking: ((BookingHomeResponse) -> Unit)? = null, onAddReview: ((BookingHomeResponse, Int, String) -> Unit)? = null, onErrorShown: () -> Unit = {}, onInfoShown: () -> Unit = {}) {
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoggedIn) {
            BookingHomeContent(state, onLoadMoreActive, onLoadMorePast, onFilterChanged, onBookingClick, onDeleteBooking, onAddReview)
        } else {
            BookingHomeNotLogged()
        }

        LaunchedEffect(state.errorMessage) {
            state.errorMessage?.let { errorMsg ->
                snackbarHostState.showSnackbar(errorMsg)
                onErrorShown()
            }
        }

        LaunchedEffect(state.infoMessage) {
            state.infoMessage?.let { infoMsg ->
                snackbarHostState.showSnackbar(infoMsg)
                onInfoShown()
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun BookingHomeContent(
    state: BookingHomeState,
    onLoadMoreActive: () -> Unit,
    onLoadMorePast: () -> Unit,
    onFilterChanged: (BookingFilterType) -> Unit,
    onBookingClick: (BookingHomeResponse) -> Unit,
    onDeleteBooking: ((BookingHomeResponse) -> Unit)? = null,
    onAddReview: ((BookingHomeResponse, Int, String) -> Unit)? = null
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(stringResource(R.string.booking_tab_active), stringResource(R.string.booking_tab_past))

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.filterType == BookingFilterType.ALL,
                onClick = { onFilterChanged(BookingFilterType.ALL) },
                label = { Text(stringResource(R.string.all)) }
            )
            FilterChip(
                selected = state.filterType == BookingFilterType.TRAVEL,
                onClick = { onFilterChanged(BookingFilterType.TRAVEL) },
                label = { Text(stringResource(R.string.travels)) }
            )
            FilterChip(
                selected = state.filterType == BookingFilterType.ACTIVITY,
                onClick = { onFilterChanged(BookingFilterType.ACTIVITY) },
                label = { Text(stringResource(R.string.activities)) }
            )
        }
        
        val filteredActiveItems = state.active.items.filter { 
            state.filterType == BookingFilterType.ALL || it.departureType?.name == state.filterType.name 
        }
        val filteredPastItems = state.past.items.filter { 
            state.filterType == BookingFilterType.ALL || it.departureType?.name == state.filterType.name 
        }

        when (selectedTab) {
            // delete solo per booking attivi, review solo per booking passati
            0 -> BookingListSection(state.active.copy(items = filteredActiveItems), R.string.booking_empty_active, onLoadMoreActive, onBookingClick, onDeleteBooking)
            1 -> BookingListSection(state.past.copy(items = filteredPastItems), R.string.booking_empty_past, onLoadMorePast, onBookingClick, onAddReview = onAddReview)
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun BookingListSection(
    paging: BookingPagingState,
    emptyMessageRes: Int,
    onLoadMore: () -> Unit,
    onBookingClick: (BookingHomeResponse) -> Unit,
    onDeleteBooking: ((BookingHomeResponse) -> Unit)? = null,
    onAddReview: ((BookingHomeResponse, Int, String) -> Unit)? = null
) {
    when {
        paging.isLoading && paging.items.isEmpty() -> CenteredBox { CircularProgressIndicator() }
        paging.items.isEmpty() -> CenteredBox {
            Text(
                text = stringResource(emptyMessageRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(paging.items) { _, booking ->
                BookingCard(
                    booking = booking,
                    onClick = { onBookingClick(booking) },
                    onDelete = if (onDeleteBooking != null) { { onDeleteBooking(booking) } } else null,
                    onAddReview = if (onAddReview != null) { { rating, content -> onAddReview(booking, rating, content) } } else null
                )
            }
            if (!paging.isLast) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (paging.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Button(onClick = onLoadMore) { Text(stringResource(R.string.booking_load_more)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: BookingHomeResponse, onClick: () -> Unit = {}, onDelete: (() -> Unit)? = null, onAddReview: ((Int, String) -> Unit)? = null) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showReviewDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.travelName ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (onAddReview != null) {
                    IconButton(onClick = { showReviewDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = stringResource(R.string.booking_add_review),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.booking_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.booking_people_count, booking.peopleCount ?: 0),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.booking_total_price, booking.totalPrice ?: BigDecimal.ZERO),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }

    if (showDeleteDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.booking_delete_dialog_title)) },
            text = { Text(stringResource(R.string.booking_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.booking_delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.booking_delete_dialog_dismiss))
                }
            }
        )
    }

    if (showReviewDialog && onAddReview != null) {
        var reviewRating by rememberSaveable { mutableStateOf(5) }
        var reviewContent by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text(stringResource(R.string.booking_add_review_title)) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= reviewRating) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { reviewRating = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewContent,
                        onValueChange = { reviewContent = it },
                        label = { Text(stringResource(R.string.type_your_review)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddReview(reviewRating, reviewContent.trim())
                        showReviewDialog = false
                    },
                    enabled = reviewContent.trim().isNotEmpty()
                ) { Text(stringResource(R.string.submit)) }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun BookingHomeNotLogged() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.booking_not_logged_in),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}