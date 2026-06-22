package it.roadies.android_app.ui.bookingHome

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.roadies.android_app.R
import it.roadies.android_app.client.models.booking.BookingHomeResponse
import it.roadies.android_app.viewmodel.bookingHome.BookingHomeState
import it.roadies.android_app.viewmodel.bookingHome.BookingHomeViewModel
import it.roadies.android_app.viewmodel.bookingHome.BookingPagingState
import java.math.BigDecimal


@Composable
fun BookingHomeScreen(viewModel: BookingHomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BookingHomeScreenContent(state = state, onLoadMoreActive = {viewModel.loadMoreActive()}, onLoadMorePast = {viewModel.loadMorePast()})
}

@Composable
fun BookingHomeScreenContent(state: BookingHomeState, onLoadMoreActive: () -> Unit = {}, onLoadMorePast: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoggedIn) {
            BookingHomeContent(state, onLoadMoreActive, onLoadMorePast)
        } else {
            BookingHomeNotLogged()
        }
    }
}

@Composable
fun BookingHomeContent(
    state: BookingHomeState,
    onLoadMoreActive: () -> Unit,
    onLoadMorePast: () -> Unit
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
        when (selectedTab) {
            0 -> BookingListSection(state.active, R.string.booking_empty_active, onLoadMoreActive)
            1 -> BookingListSection(state.past, R.string.booking_empty_past, onLoadMorePast)
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
    onLoadMore: () -> Unit
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
                BookingCard(booking)
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
fun BookingCard(booking: BookingHomeResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = booking.travelName ?: "",
                style = MaterialTheme.typography.titleMedium
            )
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