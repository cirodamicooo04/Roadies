package it.roadies.android_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.SubcomposeAsyncImage
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.client.models.travel.LocationType
import it.roadies.android_app.ui.theme.extendedColors

import it.roadies.android_app.viewmodel.HomeScreenViewModel

enum class Type {
    TRAVEL, ACTIVITY
}

@Composable
fun HomeScreen(navHostController: NavHostController, homeScreenViewModel: HomeScreenViewModel = hiltViewModel()){
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val query by homeScreenViewModel.searchQuery.collectAsState()
    val suggestions by homeScreenViewModel.searchSuggestions.collectAsState()
    val type = uiState.typeSelected

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchTypeToggle(selectedType = type, onTypeChanged = {
                typeChanged ->  homeScreenViewModel.changeType(typeChanged)
            })
            SearchBar(
                query = query,
                suggestions = suggestions,
                onQueryChange = { homeScreenViewModel.updateSearchQuery(it) },
                onSuggestionClick = { suggestion ->
                    val route = when (suggestion.type) {
                        LocationType.CONTINENT -> "search_screen?continent=${suggestion.name}&type=$type"
                        LocationType.COUNTRY -> "search_screen?country=${suggestion.name}&type=$type"
                        LocationType.DESTINATION -> "search_screen?destination=${suggestion.name}&type=$type"
                        LocationType.ADDRESS -> "search_screen?destination=${suggestion.name}&type=$type"
                    }
                    navHostController.navigate(route)
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.discover_world),
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                    ContinentDestinations(onContinentClick = {
                        selectedContinent -> navHostController.navigate("search_screen?continent=$selectedContinent&type=$type")
                    })
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.recommended),
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    )
                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center){
                            CircularProgressIndicator()
                        }
                    } else if (uiState.errorMessage != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center){
                            Text(text = uiState.errorMessage ?: "Errore sconosciuto", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        RecommendedTravel(uiState.recommendedTravels, onTravelClick = { travel -> 
                            navHostController.navigate("travel_detail/${travel.id}")
                        })
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    suggestions: List<SearchSuggestion>,
    onQueryChange: (String) -> Unit,
    onSuggestionClick: (SearchSuggestion) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    expanded = it.isNotBlank()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                placeholder = { Text(stringResource(R.string.insert_destination)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "search") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp)
            )

            if (suggestions.isNotEmpty()) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion.name, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                val iconVector = when (suggestion.type) {
                                    LocationType.CONTINENT -> Icons.Default.Public
                                    LocationType.COUNTRY -> Icons.Default.Flag
                                    LocationType.DESTINATION -> Icons.Default.LocationOn
                                    LocationType.ADDRESS -> Icons.Default.LocationOn
                                }
                                Icon(
                                    imageVector = iconVector, 
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                expanded = false
                                onSuggestionClick(suggestion)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinentDestinations(onContinentClick: (String) -> Unit){
    val continents: List<String> = listOf<String>("EUROPE", "ASIA", "OCEANIA", "AFRICA", "AMERICA" )

    LazyRow(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(continents){
            continent -> ContinentCard(continent, onContinentClick)
        }
    }
}

@Composable
fun ContinentCard(continent: String, onCardClick: (String) -> Unit ) {
    val imageResId = when (continent.lowercase()) {
        "europe" -> R.drawable.europe_wallpaper
        "asia" -> R.drawable.asia_wallpaper
        "africa" -> R.drawable.africa_wallpaper
        "america" -> R.drawable.america_wallpaper
        "oceania" -> R.drawable.oceania_wallpaper
        else -> R.drawable.fallback
    }

    Card(modifier = Modifier.width(200.dp).height(250.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        onClick = {onCardClick(continent)}
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Immagine di $continent",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Sfumatura grigia a meta per risaltare testo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Text(
                text = continent,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun RecommendedTravel(travels: List<TravelSummaryResponse>?, onTravelClick: (TravelSummaryResponse) -> Unit) {
    val safeTravels = travels.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (safeTravels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_travels_on_platfrom),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(safeTravels) { travel ->
                RecommendedTravelCard(travel, onTravelClick = onTravelClick)
            }
        }
    }
}

@Composable
fun RecommendedTravelCard(travel: TravelSummaryResponse, onTravelClick: (TravelSummaryResponse) -> Unit) {
    Card(modifier = Modifier.width(200.dp).
    height(250.dp),
        shape = RoundedCornerShape(20.dp),
        //elevation = CardDefaults.cardElevation(6.dp),
        onClick = {onTravelClick(travel)}
    ){
        Column() {
            SubcomposeAsyncImage(
                //Non funziona localhost, quindi faccio replace
                model = (travel.images?.firstOrNull()?.url),
                modifier = Modifier.fillMaxWidth().height(140.dp),
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
                })
        }
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = travel.destination ?: "Viaggio", fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "${stringResource(R.string.starting_from)} ${travel.startingFromPrice ?: "0"} € ")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.star,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = "${travel.averageRating ?: 0.0}",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SearchTypeToggle(selectedType: Type, onTypeChanged: (Type) -> Unit ){
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == Type.TRAVEL,
            onClick = {onTypeChanged(Type.TRAVEL)},
            label = { Text(text = stringResource(R.string.travels), fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            leadingIcon = { Icon (imageVector = Icons.Default.FlightTakeoff, contentDescription = stringResource(R.string.travels), modifier = Modifier.size(20.dp))},
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(35.dp)
        )

        FilterChip(
            selected = selectedType == Type.ACTIVITY,
            onClick = {onTypeChanged(Type.ACTIVITY)},
            label = { Text(text = stringResource(R.string.activities), fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            leadingIcon = { Icon (imageVector = Icons.Default.Attractions, contentDescription = stringResource(R.string.activities), modifier = Modifier.size(20.dp))},
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(35.dp)
        )
    }
}
