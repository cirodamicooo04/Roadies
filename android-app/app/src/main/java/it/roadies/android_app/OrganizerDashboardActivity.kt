package it.roadies.android_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.roadies.android_app.client.models.travel.OrganizerTravelsActivityResponse
import it.roadies.android_app.ui.travel.components.BoxCentered
import it.roadies.android_app.viewmodel.OrganizerDashboardViewModel

@Composable
fun OrganizerDashboard(navHostController: NavHostController, viewModel: OrganizerDashboardViewModel = hiltViewModel()){
    val uiState = viewModel.uiState.collectAsState()
    var travelToDelete by remember { mutableStateOf<TravelSummaryResponse?>(null) }
    var activityToDelete by remember { mutableStateOf<ActivitySummaryResponse?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }


    val savedStateHandle = navHostController.currentBackStackEntry?.savedStateHandle

    // mi prendo lo stato che gli ho passato dopo la creazione
    val travelCreated by savedStateHandle?.getStateFlow("travel_created",false)?.collectAsState() ?: remember { mutableStateOf(false) }
    val activityCreated by savedStateHandle?.getStateFlow("activity_created",false)?.collectAsState() ?: remember { mutableStateOf(false) }

    val successMessage = stringResource(R.string.travel_created)
    LaunchedEffect(travelCreated) {
        if (travelCreated){
            savedStateHandle?.remove<Boolean>("travel_created")
            viewModel.loadData()
            snackbarHostState.showSnackbar(message = successMessage)
        }
    }

    val activitySuccessMessage = stringResource(R.string.activity_created)
    LaunchedEffect(activityCreated) {
        if (activityCreated){
            savedStateHandle?.remove<Boolean>("activity_created")
            viewModel.loadData()
            snackbarHostState.showSnackbar(message = activitySuccessMessage)
        }
    }


    
    LaunchedEffect(uiState.value.deletingErrorMessage) {
        uiState.value.deletingErrorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearDeleteError()
        }
    }

    LaunchedEffect(uiState.value.isDeleting) {
        if (!uiState.value.isDeleting && uiState.value.deletingErrorMessage == null) {
            travelToDelete = null
            activityToDelete = null
        }
    }

    travelToDelete?.let {
        travel -> DeleteConfirmationDialog(
                itemName = travel.title ?: stringResource(R.string.this_travel),
                isDeleting = uiState.value.isDeleting,
                onConfirm = {
                    viewModel.deleteTravel(travel.id)
                },
                onDismiss = {
                    travelToDelete = null
                })
    }

    activityToDelete?.let {
        activity -> DeleteConfirmationDialog(
            itemName = activity.name ?: stringResource(R.string.this_activity),
            isDeleting = uiState.value.isDeleting,
            onConfirm = {
                viewModel.deleteActivity(activity.id)
            },
            onDismiss = {
                activityToDelete = null
            }
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.value.isLoading){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.value.errorMessage != null){
            BoxCentered(text = uiState.value.errorMessage)
        } else {
            TravelsActivitiesSection(items = uiState.value.organizerResponse,
                onTravelCreationClick = {
                    navHostController.navigate("create_travel")
            }, onActivityCreationClick = {
                navHostController.navigate("create_activity")

            }, onTravelOpen = {

            }, onTravelDelete = {
                travel -> travelToDelete = travel
            }, onTravelModify = {

            }, onActivityOpen = {

            }, onActivityDelete = {
                activity -> activityToDelete = activity
            }, onActivityModify = {

            })
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
fun TravelsActivitiesSection(items: OrganizerTravelsActivityResponse?, onTravelCreationClick: () -> Unit, onActivityCreationClick: () -> Unit, onTravelModify: (TravelSummaryResponse) -> Unit, onTravelDelete: (TravelSummaryResponse) -> Unit, onTravelOpen: (TravelSummaryResponse) -> Unit, onActivityOpen: (ActivitySummaryResponse) -> Unit, onActivityDelete: (ActivitySummaryResponse) -> Unit, onActivityModify: (ActivitySummaryResponse) -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.travels), stringResource(R.string.activities))

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.your_travels_and_activities),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

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
            0 -> OrganizerTravelsTab(travels = items?.travels, onTravelCreationClick = onTravelCreationClick, onTravelModify = onTravelModify, onTravelDelete = onTravelDelete, onTravelOpen = onTravelOpen)
            1 -> OrganizerActivitiesTab(activities = items?.activities, onActivityCreationClick = onActivityCreationClick, onActivityModify = onActivityModify, onActivityDelete = onActivityDelete, onActivityOpen = onActivityOpen)
        }
    }
}

@Composable
fun OrganizerTravelsTab(travels: List<TravelSummaryResponse>?, onTravelCreationClick: () -> Unit, onTravelModify: (TravelSummaryResponse) -> Unit, onTravelDelete: (TravelSummaryResponse) -> Unit, onTravelOpen: (TravelSummaryResponse) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (travels.isNullOrEmpty()){
                BoxCentered(text = stringResource(R.string.no_travels))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(travels, key = { it.id!! }){
                        travel -> OrganizerTravelCard(travel=travel, onEditClick = onTravelModify, onDeleteClick = onTravelDelete, onCardClick = onTravelOpen )
                    }
                }
            }

        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onTravelCreationClick, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Icon(
                imageVector = Icons.Filled.FlightTakeoff,
                contentDescription = stringResource(R.string.create_new_travel)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.create_new_travel),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrganizerActivitiesTab(activities: List<ActivitySummaryResponse>?, onActivityCreationClick: () -> Unit, onActivityOpen: (ActivitySummaryResponse) -> Unit, onActivityModify: (ActivitySummaryResponse) -> Unit, onActivityDelete: (ActivitySummaryResponse) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (activities.isNullOrEmpty()){
                BoxCentered(text = stringResource(R.string.no_activities))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activities, key = {it.id!!}){
                        activity -> OrganizerActivityCard(activity=activity, onCardClick = onActivityOpen, onEditClick = onActivityModify, onDeleteClick = onActivityDelete)
                    }
                }
            }

        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onActivityCreationClick, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Icon(
                imageVector = Icons.Filled.LocalActivity,
                contentDescription = stringResource(R.string.create_new_activity)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.create_new_activity),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrganizerTravelCard(
    travel: TravelSummaryResponse,
    onCardClick: (TravelSummaryResponse) -> Unit,
    onEditClick: (TravelSummaryResponse) -> Unit,
    onDeleteClick: (TravelSummaryResponse) -> Unit
) {
    val editable = travel.editable?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = { onCardClick(travel) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(140.dp).fillMaxHeight()) {
                AsyncImage(
                    model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png", // travel.images?.firstOrNull()?.url
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = travel.title,
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = travel.title ?: "Senza Titolo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${travel.destination ?: ""}, ${travel.country ?: ""}",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Duration",
                                tint = Color.Gray,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${travel.durationDays ?: 0} ${stringResource(R.string.days)}",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }

                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${travel.averageRating ?: 0.0}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedIconButton(
                            onClick = { onEditClick(travel) },
                            enabled = editable,
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, if (editable) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        OutlinedIconButton(
                            onClick = { onDeleteClick(travel) },
                            enabled = editable,
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, if (editable) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrganizerActivityCard(
    activity: ActivitySummaryResponse,
    onCardClick: (ActivitySummaryResponse) -> Unit,
    onEditClick: (ActivitySummaryResponse) -> Unit,
    onDeleteClick: (ActivitySummaryResponse) -> Unit
) {
    val editable = activity.editable?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = { onCardClick(activity) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(140.dp).fillMaxHeight()) {
                AsyncImage(
                    model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png", // activity.images?.firstOrNull()?.url
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = activity.name,
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = activity.name ?: "Senza Nome",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activity.destination ?: ""}, ${activity.country ?: ""}",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activity.averageRating ?: 0.0}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedIconButton(
                            onClick = { onEditClick(activity) },
                            enabled = editable,
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, if (editable) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        OutlinedIconButton(
                            onClick = { onDeleteClick(activity) },
                            enabled = editable,
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, if (editable) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(itemName: String, isDeleting: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() }, 
        title = {
            Text(text = stringResource(R.string.confirm_deleting))
        }, 
        text = {
            Text(text = stringResource(R.string.confirm_deleting_body, itemName))
        }, 
        confirmButton = {
            TextButton(
                onClick = onConfirm, 
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ){
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.delete))
                }
            }
        }, 
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
