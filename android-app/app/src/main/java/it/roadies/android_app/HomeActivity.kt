package it.roadies.android_app

import android.R.attr.text
import android.media.Image
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.viewmodel.HomeScreenViewModel

@Composable
fun HomeScreen(navHostController: NavHostController, homeScreenViewModel: HomeScreenViewModel = hiltViewModel()){
    val uiState by homeScreenViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        homeScreenViewModel.loadRecommendedTravel()
    }

    LazyColumn(modifier = Modifier.
        fillMaxSize().
        padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SearchBar()
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Consigliati per te",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                if (uiState.isLoading || uiState.errorMessage != null){
                    CircularProgressIndicator()
                }
                RecommendedTravel(uiState.recommendedTravels)
            }
        }
    }
}

@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.insert_destination)) },
            leadingIcon = { Icons.Default.Search },
            trailingIcon = { Icons.Default.Tune },
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
        //Riga filtri LazyRow
    }
}

@Composable
fun RecommendedTravel(travels: List<TravelSummaryResponse>?) {
    val safeTravels = travels.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        LazyRow(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(safeTravels) {
                travel -> TravelCard(travel)
            }
    }
}


}

@Composable
fun TravelCard(travel: TravelSummaryResponse) {
    Card(modifier = Modifier.width(200.dp).
    height(250.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ){
        Column() {
            AsyncImage(
                //Non funziona localhost, quindi immagine momentanea
                //model = travel.images?.firstOrNull()?.url,
                model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentDescription = travel.title,
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = travel.destination ?: "Viaggio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = stringResource(R.string.starting_from, travel.startingFromPrice ?: "0", "€"))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
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
