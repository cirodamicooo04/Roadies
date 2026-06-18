package it.roadies.android_app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.roadies.android_app.client.models.travel.TravelSummaryResponse
import it.roadies.android_app.viewmodel.SearchScreenViewModel

@Composable
fun SearchScreen(navHostController: NavHostController, searchScreenViewModel: SearchScreenViewModel = hiltViewModel()){
    val uiState by searchScreenViewModel.searchScreenUiState.collectAsState()

    var safeTravels: List<TravelSummaryResponse> = emptyList<TravelSummaryResponse>()
    //test
    Column(){
        if (uiState.travels != null){
            safeTravels = uiState.travels?: emptyList()
            if (safeTravels.isEmpty()){
                Text(text = "Vuoto cazzo")
            }

            for (travel in uiState.travels){
                Text(text = travel.title?: "")
            }
        } else {
            Text(text = "Viaggi null")
        }
    }
}