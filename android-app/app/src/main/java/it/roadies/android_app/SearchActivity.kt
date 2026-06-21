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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import it.roadies.android_app.client.models.travel.ActivitySummaryResponse
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.viewmodel.SearchScreenViewModel

@Composable
fun BoxCentered(text: String)
{
    Box(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 16.sp, fontStyle = FontStyle.Italic)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navHostController: NavHostController, searchScreenViewModel: SearchScreenViewModel = hiltViewModel()){
    val uiState by searchScreenViewModel.searchScreenUiState.collectAsState()
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    val searchFilterState by searchScreenViewModel.searchFiltersState.collectAsState()

    if (isFilterSheetOpen && !uiState.isLoading && uiState.errorMessage == null){
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
                        Text("${priceRange.start.toInt()}€ - ${priceRange.endInclusive.toInt()}€", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RangeSlider(
                        value = priceRange,
                        onValueChange = { priceRange = it },
                        valueRange = 0f..10000f,
                        steps = 100
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.duration_days), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("${durationRange.start.toInt()} - ${durationRange.endInclusive.toInt()} ${stringResource(R.string.days)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RangeSlider(
                        value = durationRange,
                        onValueChange = { durationRange = it },
                        valueRange = 1f..30f,
                        steps = 28
                    )
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
                    onClick = { /* TODO: Aprire ordinamento*/ },
                    label = { Text(stringResource(R.string.order_by), fontWeight = FontWeight.SemiBold) },
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.order_by), modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (uiState.isLoading || uiState.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) CircularProgressIndicator()
                    if (uiState.errorMessage != null) Text(
                        text = uiState.errorMessage ?: stringResource(R.string.search_error),
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.Gray
                    )
                }
            } else {
                if (uiState.type == "ACTIVITY") {
                    ActivitiesResult(uiState.activities ?: emptyList(), onActivityClick = { activity ->
                        navHostController.navigate("activity_detail/${activity.id}")
                    })
                } else {
                    TravelsResult(uiState.travels ?: emptyList(), onTravelClick = { travel ->
                        navHostController.navigate("travel_detail/${travel.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun ActivitiesResult(activities: List<ActivitySummaryResponse>, onActivityClick: (ActivitySummaryResponse) -> Unit){
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activities) { activity -> 
                ActivityCard(activity, onCardClick = { onActivityClick(activity) })
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivitySummaryResponse, onCardClick : (ActivitySummaryResponse) -> Unit ){
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
                AsyncImage(
                    //Non funziona localhost, quindi immagine momentanea
                    //model = activity.images?.firstOrNull()?.url,
                    model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png",
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
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!activity.type.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activity.type.uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // TODO: Recuperare l'username reale al posto di questa stringa fittizia
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👤 @owner_fittizio",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
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
fun TravelsResult(travels: List<TravelSummaryResponse>, onTravelClick: (TravelSummaryResponse) -> Unit){
    if (travels.isEmpty()){
        BoxCentered(text = stringResource(R.string.no_travels_found))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                top = 8.dp, 
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items (travels){ travel -> 
                TravelCard(travel, onCardClick = { onTravelClick(travel)})
            }
        }
    }
}

@Composable
fun TravelCard(travel: TravelSummaryResponse, onCardClick: (TravelSummaryResponse) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Niente padding orizzontale qui, ci pensa la LazyColumn
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = { onCardClick(travel) }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(140.dp).fillMaxHeight()) {
                AsyncImage(
                    //Non funziona localhost, quindi immagine momentanea
                    //model = travel.images?.firstOrNull()?.url,
                    model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png",
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
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // TODO: Recuperare l'username reale al posto di questa stringa fittizia
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👤 @owner_fittizio",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
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