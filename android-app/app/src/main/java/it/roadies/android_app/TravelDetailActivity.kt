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
fun TravelDetailScreen(navHostController: NavHostController ,id: UUID, viewModel: TravelDetailViewModel = hiltViewModel()){
    Text(text = "$id")
}
