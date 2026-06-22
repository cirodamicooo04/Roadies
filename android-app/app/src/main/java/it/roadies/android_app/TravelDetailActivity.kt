package it.roadies.android_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Card
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import it.roadies.android_app.client.models.travel.ImageResponse
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.client.models.travel.TravelTagResponse
import it.roadies.android_app.viewmodel.TravelDetailViewModel

@Composable
fun TravelDetailScreen(navHostController: NavHostController, viewModel: TravelDetailViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
    } else if (uiState.errorMessage != null){
            BoxCentered(text = uiState.errorMessage)
        } else {
        TravelDetail(uiState.travel)
    }
}

@Composable
fun TravelDetail(travel: TravelResponse?){
    if (travel == null){
        BoxCentered(text = stringResource(R.string.error_loading_travel))
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                //Header (Nome + sottotitolo: Luogo e durata)
                TravelHeader(travel.title, travel.destination, travel.country, travel.durationDays)

                //Immagini (LazyRow)
                TravelImages(travel.images)

                //Separatore

                //travel tag
                TravelTags(travel.tagScores)

                //travel description
                TravelDescription(travel.description)

                //mappa

                //travel activity con ogni attività collasabile

                // recensioni

            }
            
            DepartureButton(onClick = {
                //TODO: Aprire sheet modale per le partenze disponibili
            })
        }
    }
}
@Composable
fun DepartureButton(onClick: () -> Unit){
    Button(modifier = Modifier.fillMaxWidth().padding(12.dp), onClick = onClick) {
        Text(text = stringResource(R.string.check_availability), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun TravelHeader(travelTitle: String?, travelDestination: String?, travelCountry: String?, duration: Int?){
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 23.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = travelTitle ?: "Travel", fontSize = 35.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${travelDestination?: ""}, ${travelCountry?: ""}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Row() {
                Icon(imageVector = Icons.Filled.AccessTime, contentDescription = "Duration", modifier = Modifier.padding(end = 5.dp))
                Text(text = "${duration ?: ""} ${stringResource(R.string.days)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun TravelImages(travelImages: List<ImageResponse>?){
    if (travelImages.isNullOrEmpty()) {
        Text("Ciao")
        //TODO: Vedere cosa fare in caso di immagini vuote o null
        return
    }

    val pagerState = rememberPagerState(pageCount = { travelImages.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.padding(bottom = 18.dp)
    ) { page ->
        val imageUrl = travelImages[page].url

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            AsyncImage(
                // TODO: Da risolvere problema url
                //model = imageUrl,
                model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png",
                contentDescription = "Foto del viaggio",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun TravelTags(tags: List<TravelTagResponse>?) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = stringResource(R.string.is_this_travel_for_me), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
        
        tags?.forEach { tag ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tag.tagName ?: "", 
                    fontSize = 16.sp,
                    modifier = Modifier.weight(0.3f)
                )
                
                val score = tag.score ?: 0
                Row(
                    modifier = Modifier.weight(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..5) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (i <= score) Color.Black else Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TravelDescription(description: String?) {
    if (description.isNullOrEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    var showReadMore by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .animateContentSize()
    ) {
        Text(
            text = stringResource(R.string.description),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = description,
            fontSize = 16.sp,
            color = Color.DarkGray,
            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                    showReadMore = true
                }
            }
        )
        
        if (showReadMore) {
            Text(
                text = if (isExpanded) "Mostra meno" else "Leggi di più",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { isExpanded = !isExpanded }
            )
        }
    }
}

