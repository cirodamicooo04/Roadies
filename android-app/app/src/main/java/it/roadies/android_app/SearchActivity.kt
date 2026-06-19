package it.roadies.android_app

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun SearchScreen(navHostController: NavHostController, searchScreenViewModel: SearchScreenViewModel = hiltViewModel()){
    val uiState by searchScreenViewModel.searchScreenUiState.collectAsState()

    if (uiState.isLoading || uiState.errorMessage != null){
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) CircularProgressIndicator()
            if (uiState.errorMessage != null) Text(text = uiState.errorMessage?: "Error while loading your search", fontSize = 16.sp, fontStyle = FontStyle.Italic)
        }
    } else {

        if (uiState.type == "ACTIVITY"){
            ActivitiesResult(uiState.activities?: emptyList(), onActivityClick = { activity ->
                navHostController.navigate("activity_detail/${activity.id}")
            })
        } else {
            TravelsResult(uiState.travels?: emptyList(), onTravelClick = { travel ->
                navHostController.navigate("travel_detail/${travel.id}")
            })
        }
    }
}

@Composable
fun ActivitiesResult(activities: List<ActivitySummaryResponse>, onActivityClick: (ActivitySummaryResponse) -> Unit){
    if (activities.isEmpty()){
        BoxCentered(text = stringResource(R.string.no_activities_found))
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(activities){
            activity -> ActivityCard(activity, onCardClick = { onActivityClick(activity) })
        }
    }

}

@Composable
fun ActivityCard(activity: ActivitySummaryResponse, onCardClick : (ActivitySummaryResponse) -> Unit ){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Adjusted height for horizontal layout
            .padding(horizontal = 16.dp, vertical = 6.dp), // Similar padding
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

            // Dettagli a Destra
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Nome e Location
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

                // Bottom: Owner, Rating, Prezzo
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

                    // Rating
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
    }
    LazyColumn(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items (travels){
            travel -> TravelCard(travel, onCardClick = { onTravelClick(travel)})
        }
    }

}

@Composable
fun TravelCard(travel: TravelSummaryResponse, onCardClick: (TravelSummaryResponse) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp),
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