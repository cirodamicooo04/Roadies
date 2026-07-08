package it.roadies.android_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.roadies.android_app.ui.theme.extendedColors
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.user.MinimalInformationResponseDTO
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.ui.travel.components.BoxCentered
import it.roadies.android_app.viewmodel.SearchScreenViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navHostController: NavHostController, searchScreenViewModel: SearchScreenViewModel = hiltViewModel()){
    val uiState by searchScreenViewModel.searchScreenUiState.collectAsState()

    val organizersUiState by searchScreenViewModel.organizersState.collectAsState()
    val organizers = organizersUiState.organizers

    val snackbarHostState = remember { SnackbarHostState() }

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSortSheetOpen by remember { mutableStateOf(false) }

    val searchFilterState by searchScreenViewModel.searchFiltersState.collectAsState()

    val lazyListState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?: 0
            val threshold = 3 //Quando mancano 3 elementi alla fine iniziamo a caricare

            totalItemsCount > 0 && lastVisibleItemIndex >= (totalItemsCount - threshold) //true se l'ultimo elemento visibile è dal terzultimo in poi
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoadingMore && !uiState.isLastPage) {
            searchScreenViewModel.loadNextPage()
        }
    }

    if (isFilterSheetOpen && !uiState.isLoading){
        ModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            sheetState = filterSheetState
        ) {
            var priceRange by remember(searchFilterState) {
                mutableStateOf(
                    (searchFilterState.minPrice?.toFloat() ?: 0f)..(searchFilterState.maxPrice?.toFloat() ?: 10000f)
                ) 
            }
            var durationRange by remember(searchFilterState) { 
                mutableStateOf(
                    (searchFilterState.minDurationDays?.toFloat() ?: 1f)..(searchFilterState.maxDurationDays?.toFloat() ?: 30f)
                ) 
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.filter),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    TextButton(
                        onClick = {
                            priceRange = 0f..10000f
                            durationRange = 1f..30f
                        }
                    ) {
                        Text(stringResource(R.string.reset), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.price_range), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(stringResource(R.string.price_range_format, priceRange.start.toInt().toString(), priceRange.endInclusive.toInt().toString()), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RangeSlider(
                        value = priceRange,
                        onValueChange = { priceRange = it },
                        valueRange = 0f..10000f,
                        steps = 100
                    )
                }

                if (uiState.type != "ACTIVITY") {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.duration_days), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(stringResource(R.string.duration_range_format, durationRange.start.toInt().toString(), durationRange.endInclusive.toInt().toString(), stringResource(R.string.days)), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        RangeSlider(
                            value = durationRange,
                            onValueChange = { durationRange = it },
                            valueRange = 1f..30f,
                            steps = 28
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        searchScreenViewModel.applyFilters(
                            newMinPrice = priceRange.start,
                            newMaxPrice = priceRange.endInclusive,
                            newMinDuration = durationRange.start,
                            newMaxDuration = durationRange.endInclusive
                        )
                        isFilterSheetOpen = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.apply_filters), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp)) // Spazio per la navigation bar
            }
        }
    }

    if (isSortSheetOpen && !uiState.isLoading ) {
        ModalBottomSheet(
            onDismissRequest = { isSortSheetOpen = false },
            sheetState = sortSheetState
        ) {
            var selectedSort by remember(searchFilterState) { 
                mutableStateOf(searchFilterState.sortCriteria?.firstOrNull()) 
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.order_by),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    TextButton(
                        onClick = { selectedSort = null }
                    ) {
                        Text(stringResource(R.string.reset), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                var sortOptions: List<Pair<String,String>>

                if (uiState.type == "ACTIVITY"){
                    sortOptions = listOf(
                        "startingFromPrice,asc" to stringResource(R.string.price_asc),
                        "startingFromPrice,desc" to stringResource(R.string.price_desc),
                    )
                } else {

                    sortOptions = listOf(
                        "startingFromPrice,asc" to stringResource(R.string.price_asc),
                        "startingFromPrice,desc" to stringResource(R.string.price_desc),
                        "durationDays,asc" to stringResource(R.string.duration_asc),
                        "durationDays,desc" to stringResource(R.string.duration_desc)
                    )
                }

                sortOptions.forEach { (sortKey, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSort = sortKey }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == sortKey,
                            onClick = { selectedSort = sortKey }
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = label, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        searchScreenViewModel.applySort(
                            if (selectedSort != null) listOf(selectedSort!!) else emptyList()
                        )
                        isSortSheetOpen = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.apply_sorting), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp)) // Spazio per la navigation bar
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.search_results),
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = false,
                    onClick = { isFilterSheetOpen = true },
                    label = { Text(stringResource(R.string.filter), fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = stringResource(R.string.filter) , modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(16.dp)
                )
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = false,
                    onClick = { isSortSheetOpen = true },
                    label = { Text(stringResource(R.string.order_by), fontWeight = FontWeight.SemiBold) },
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.order_by), modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (uiState.isLoading && uiState.travels?.isEmpty() == true && uiState.activities?.isEmpty() == true) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null && uiState.travels?.isEmpty() == true && uiState.activities?.isEmpty() == true) {
                BoxCentered(text = uiState.errorMessage ?: stringResource(R.string.search_error))
            } else {
                if (uiState.type == "ACTIVITY") {
                    ActivitiesResult(uiState.activities ?: emptyList(), organizers, organizersUiState.isLoading, onActivityClick = { activity ->
                        navHostController.navigate("activity_detail/${activity.id}")
                    }, onOrganizerClick = { username -> 
                        val organizer = organizers?.find { it.username == username }
                        val isMe = uiState.currentUserId != null && uiState.currentUserId == organizer?.keycloakId
                        if (isMe) {
                            navHostController.navigate("profile_graph")
                        } else {
                            navHostController.navigate("user_profile/$username")
                        }
                    }, lazyListState, uiState.isLoadingMore || (uiState.isLoading && uiState.activities?.isNotEmpty() == true))
                } else {
                    TravelsResult(uiState.travels ?: emptyList(), organizers, organizersUiState.isLoading, onTravelClick = { travel ->
                        navHostController.navigate("travel_detail/${travel.id}")
                    }, onOrganizerClick = { username -> 
                        val organizer = organizers?.find { it.username == username }
                        val isMe = uiState.currentUserId != null && uiState.currentUserId == organizer?.keycloakId
                        if (isMe) {
                            navHostController.navigate("profile_graph")
                        } else {
                            navHostController.navigate("user_profile/$username")
                        }
                    }, lazyListState, uiState.isLoadingMore || (uiState.isLoading && uiState.travels?.isNotEmpty() == true))
                }
                
                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let { errorMsg ->
                        snackbarHostState.showSnackbar(errorMsg)
                        searchScreenViewModel.clearError()
                    }
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
}

@Composable
fun ActivitiesResult(activities: List<ActivitySummaryResponse>, organizers: List<MinimalInformationResponseDTO>?, isOrganizersLoading: Boolean = false, onActivityClick: (ActivitySummaryResponse) -> Unit, onOrganizerClick: (String) -> Unit, lazyListState: LazyListState, isLoadingMore: Boolean){
    if (activities.isEmpty()){
        BoxCentered(text = stringResource(R.string.no_activities_found))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                top = 8.dp, 
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = lazyListState
        ) {
            items(activities) { activity -> 
                ActivityCard(activity, organizers, isOrganizersLoading, onCardClick = { onActivityClick(activity) }, onOrganizerClick = onOrganizerClick)
            }

            if (isLoadingMore){
                item {
                    Box (modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivitySummaryResponse, organizers: List<MinimalInformationResponseDTO>?, isOrganizersLoading: Boolean = false, onCardClick: (ActivitySummaryResponse) -> Unit, onOrganizerClick: (String) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = { onCardClick(activity) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Immagine a Sinistra
            Box(modifier = Modifier.width(140.dp).fillMaxHeight()) {
                SubcomposeAsyncImage(
                    model = activity.images?.firstOrNull()?.url,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = activity.name,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    },
                    error = {
                        Image(
                            painter = painterResource(id = R.drawable.travel_placeholder),
                            contentDescription = "Immagine di default",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activity.destination ?: ""}, ${activity.country ?: ""}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val organizer = organizers?.find { it.keycloakId == activity.ownerId }
                    val organizerName = if (isOrganizersLoading) "Caricamento..." else organizer?.username ?: "Sconosciuto"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = organizer?.username != null) {
                            organizer?.username?.let { onOrganizerClick(it) }
                        }
                    ) {
                        if (isOrganizersLoading || organizer == null) {
                            Text(
                                text = "👤 @$organizerName",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            val avatarUrl = organizer.avatarUrl
                            if (!avatarUrl.isNullOrEmpty()) {
                                SubcomposeAsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(20.dp),
                                    success = {
                                        SubcomposeAsyncImageContent(modifier = Modifier.clip(CircleShape))
                                    },
                                    error = {
                                        Text(text = "👤", fontSize = 14.sp)
                                    },
                                    loading = {
                                        Text(text = "👤", fontSize = 14.sp)
                                    }
                                )
                            } else {
                                Text(
                                    text = "👤",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "@${organizer.username}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End, // Allinea a destra dato che manca la durata
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.extendedColors.star,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activity.averageRating ?: 0.0}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prezzo separato su una riga propria, allineato a destra
                    Text(
                        text = "${stringResource(R.string.starting_from)}: ${activity.startingFromPrice ?: 0} €",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun TravelsResult(travels: List<TravelSummaryResponse>, organizers: List<MinimalInformationResponseDTO>?, isOrganizersLoading: Boolean = false, onTravelClick: (TravelSummaryResponse) -> Unit, onOrganizerClick: (String) -> Unit, lazyListState: LazyListState, isLoadingMore: Boolean){
    if (travels.isEmpty()){
        BoxCentered(text = stringResource(R.string.no_travels_found))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                top = 8.dp, 
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items (travels){ travel -> 
                TravelCard(travel, organizers, isOrganizersLoading, onCardClick = { onTravelClick(travel)}, onOrganizerClick = onOrganizerClick)
            }

            if (isLoadingMore){
                item {
                    Box (modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun TravelCard(travel: TravelSummaryResponse, organizers: List<MinimalInformationResponseDTO>?, isOrganizersLoading: Boolean = false, onCardClick: (TravelSummaryResponse) -> Unit, onOrganizerClick: (String) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = { onCardClick(travel) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(140.dp).fillMaxHeight()) {
                SubcomposeAsyncImage(
                    model = travel.images?.firstOrNull()?.url,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = travel.title,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    },
                    error = {
                        Image(
                            painter = painterResource(id = R.drawable.travel_placeholder),
                            contentDescription = "Immagine di default",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${travel.destination ?: ""}, ${travel.country ?: ""}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val organizer = organizers?.find { it.keycloakId == travel.ownerId }
                    val organizerName = if (isOrganizersLoading) stringResource(R.string.loading) else organizer?.username ?: stringResource(R.string.organizer)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = organizer?.username != null) {
                            organizer?.username?.let { onOrganizerClick(it) }
                        }
                    ) {
                        if (isOrganizersLoading || organizer == null) {
                            Text(
                                text = "👤 @$organizerName",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            val avatarUrl = organizer.avatarUrl
                            if (!avatarUrl.isNullOrEmpty()) {
                                SubcomposeAsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(20.dp),
                                    success = {
                                        SubcomposeAsyncImageContent(modifier = Modifier.clip(CircleShape))
                                    },
                                    error = {
                                        Text(text = "👤", fontSize = 14.sp)
                                    },
                                    loading = {
                                        Text(text = "👤", fontSize = 14.sp)
                                    }
                                )
                            } else {
                                Text(
                                    text = "👤",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "@${organizer.username}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Duration",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${travel.durationDays ?: 0} ${stringResource(R.string.days)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }

                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = MaterialTheme.extendedColors.star,
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${stringResource(R.string.starting_from)}: ${travel.startingFromPrice ?: 0} €",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}