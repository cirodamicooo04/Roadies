package it.roadies.android_app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import it.roadies.android_app.ActivityCard
import it.roadies.android_app.R
import it.roadies.android_app.TravelCard
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.ui.travel.components.BoxCentered
import it.roadies.android_app.viewmodel.OrganizerTravelsViewModel

@Composable
fun OrganizerTravelsScreen(
    navHostController: NavHostController,
    viewModel: OrganizerTravelsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.travels), stringResource(R.string.activities))

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            BoxCentered(text = uiState.errorMessage!!)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                val user = uiState.user
                if (user != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navHostController.navigate("user_profile/${user.username}") },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarUrl = user.avatarUrl
                            
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!avatarUrl.isNullOrEmpty()) {
                                    SubcomposeAsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        success = { SubcomposeAsyncImageContent() },
                                        error = { 
                                            Icon(
                                                imageVector = Icons.Default.Person, 
                                                contentDescription = "Avatar Placeholder", 
                                                modifier = Modifier.size(30.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        },
                                        loading = { CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person, 
                                        contentDescription = "Avatar Placeholder", 
                                        modifier = Modifier.size(30.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                Text(
                                    text = "@${user.username}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.travel_activities_organized),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Caricamento profilo...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> OrganizerPublicTravelsTab(
                        travels = uiState.items?.travels,
                        user = user,
                        navHostController = navHostController
                    )
                    1 -> OrganizerPublicActivitiesTab(
                        activities = uiState.items?.activities,
                        user = user,
                        navHostController = navHostController
                    )
                }
            }
        }
    }
}

@Composable
fun OrganizerPublicTravelsTab(
    travels: List<TravelSummaryResponse>?,
    user: MinimalInformationResponseDTO?,
    navHostController: NavHostController
) {
    if (travels.isNullOrEmpty()) {
        BoxCentered(text = stringResource(R.string.no_travels_found))
    } else {
        val organizersList = user?.let { listOf(it) } ?: emptyList()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(travels, key = { it.id!! }) { travel ->
                TravelCard(
                    travel = travel,
                    organizers = organizersList,
                    isOrganizersLoading = false,
                    onCardClick = { navHostController.navigate("travel_detail/${travel.id}") },
                    onOrganizerClick = { navHostController.navigate("user_profile/${user?.username}") }
                )
            }
        }
    }
}

@Composable
fun OrganizerPublicActivitiesTab(
    activities: List<ActivitySummaryResponse>?,
    user: MinimalInformationResponseDTO?,
    navHostController: NavHostController
) {
    if (activities.isNullOrEmpty()) {
        BoxCentered(text = stringResource(R.string.no_activities_found))
    } else {
        val organizersList = user?.let { listOf(it) } ?: emptyList()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activities, key = { it.id!! }) { activity ->
                ActivityCard(
                    activity = activity,
                    organizers = organizersList,
                    isOrganizersLoading = false,
                    onCardClick = { navHostController.navigate("activity_detail/${activity.id}") },
                    onOrganizerClick = { navHostController.navigate("user_profile/${user?.username}") }
                )
            }
        }
    }
}