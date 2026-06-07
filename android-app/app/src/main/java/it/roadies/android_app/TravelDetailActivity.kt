package it.roadies.android_app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.room.util.TableInfo
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.repository.TravelRepository_Factory
import it.roadies.android_app.viewmodel.TravelDetailState
import it.roadies.android_app.viewmodel.TravelDetailViewModel
import java.util.UUID

@Composable
fun TravelDetailScreen(navHostController: NavHostController ,travelId: UUID, viewModel: TravelDetailViewModel = hiltViewModel()){
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(travelId) {
        viewModel.loadTravel(travelId)
    }

    when {
        uiState.isLoading -> CircularProgressIndicator()
        uiState.travel != null -> TravelDetailContent(uiState.travel!!)
        uiState.errorMessage != null -> Text(uiState.errorMessage!!)
    }


}

@Composable
fun TravelDetailContent(travel: TravelResponse){
    Column(){
        Text(text = travel.title ?: "Senza titolo")
        Text(text = travel.description ?: "Nessuna descrizione")
        Text(text = travel.destination?: "Nessuna destinazione")
    }
}