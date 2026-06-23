package it.roadies.android_app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Card
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.SubcomposeAsyncImage
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import it.roadies.android_app.client.models.travel.ActivityResponse
import it.roadies.android_app.client.models.travel.ImageResponse
import it.roadies.android_app.client.models.travel.TravelDepartureResponse
import it.roadies.android_app.client.models.travel.TravelResponse
import it.roadies.android_app.client.models.travel.TravelTagResponse
import it.roadies.android_app.viewmodel.TravelDetailViewModel
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelDetailScreen(navHostController: NavHostController, viewModel: TravelDetailViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsState()
    val departuresState by viewModel.departuresState.collectAsState()

    val departuresSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isDeparturesSheetOpen by remember { mutableStateOf(false) }

    if (uiState.isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
    } else if (uiState.errorMessage != null){
            BoxCentered(text = uiState.errorMessage)
        } else {
        TravelDetail(uiState.travel, onCheckAvailability = {
            viewModel.loadDepartures()
            isDeparturesSheetOpen = true
        })
    }

    LaunchedEffect(uiState.createdBookingId) {
        uiState.createdBookingId?.let { bookingId ->
            uiState.selectedDepartureId?.let { departureId ->
                navHostController.navigate("booking_people/$bookingId/$departureId")
                viewModel.onBookingNavigated()
            }
        }
    }

    if (isDeparturesSheetOpen && !uiState.isLoading && uiState.errorMessage == null){

        ModalBottomSheet(sheetState = departuresSheetState, onDismissRequest = {isDeparturesSheetOpen = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(0.8f)){
                Text(
                    text = stringResource(R.string.available_departures),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (departuresState.isLoading){
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                } else if (departuresState.errorMessage != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center){
                        Text(text = departuresState.errorMessage ?: "")
                    }
                } else {
                    val departuresList = departuresState.departures
                    if (departuresList.isNullOrEmpty()){
                        BoxCentered(text = stringResource(R.string.no_available_departures))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                            items(departuresList) { departure ->
                                DepartureCard(
                                    departure = departure,
                                    onBookClick = { departureId ->
                                        val uuid = runCatching { java.util.UUID.fromString(departureId) }.getOrNull()
                                        if (uuid != null) {
                                            viewModel.createDraftBooking(uuid)
                                            isDeparturesSheetOpen = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TravelDetail(travel: TravelResponse?, onCheckAvailability: () -> Unit){
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
                TravelMap(lon = travel.longitude, lat = travel.latitude)

                //travel activity con ogni attività collasabile
                Activities(travel.activities)

                // recensioni

            }
            
            DepartureButton(onClick = {
                onCheckAvailability()
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
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 18.dp)
                .height(220.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.travel_placeholder),
                contentDescription = stringResource(R.string.activity_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
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
            SubcomposeAsyncImage(
                // TODO: Da risolvere problema url
                //model = imageUrl,
                model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png",
                contentDescription = "Foto del viaggio",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Errore immagine", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    }
}

@Composable
fun TravelTags(tags: List<TravelTagResponse>?) {
    if (tags.isNullOrEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = stringResource(R.string.is_this_travel_for_me), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
        
        tags.forEach { tag ->
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
            .padding(vertical = 16.dp,)
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
                text = if (isExpanded) stringResource(R.string.show_less) else stringResource(R.string.show_more),
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { isExpanded = !isExpanded },
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
fun TravelMap(lon: Double?, lat: Double?) {

    if (lon == null || lat == null) return

    val destinationPoint = GeoPoint(lat, lon)
    val cameraState = rememberCameraState {
        geoPoint = destinationPoint
        zoom = 13.0
    }
    val markerState = rememberMarkerState(geoPoint = destinationPoint)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        OpenStreetMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState
        ) {
            Marker(
                state = markerState,
            )
        }
    }
}

@Composable
fun Activities(activities: List<ActivityResponse>?){
    if (activities.isNullOrEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(top= 20.dp, bottom = 15.dp),){
        Text(text = stringResource(R.string.activities), fontWeight = FontWeight.Bold, fontSize = 24.sp)

        val sortedActivities = activities.sortedBy { it.dayNumber }

        for (activity in sortedActivities){
            CollasableActivityCard(activity)
        }
    }


}

@Composable
fun CollasableActivityCard(activity: ActivityResponse?){
    var isExpanded by remember { mutableStateOf(false) }

    //animazione per la freccetta
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "ArrowRotation")

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom= 16.dp)
            .clickable { isExpanded = !isExpanded }
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            Row (modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                //immagine + giorno e titolo
                Row(modifier = Modifier.weight(1f).padding(start = 3.dp, bottom = 8.dp, end = 8.dp, top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically){
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(70.dp)
                    ) {
                        SubcomposeAsyncImage(
                            // TODO: Da risolvere problema url
                            //model = imageUrl,
                            model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png",
                            contentDescription = stringResource(R.string.activity_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                }
                            },
                            error = {
                                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Errore immagine", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)){
                        Text(text = "${stringResource(R.string.day)} ${activity?.dayNumber ?: ""}", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = activity?.name ?: "", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }

                //freccetta
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.expand),
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            if (isExpanded) {
                Text(
                    text = activity?.description ?: stringResource(R.string.no_description_available),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DepartureCard(departure: TravelDepartureResponse, onBookClick: (String) -> Unit) {
    val isAvailable = (departure.availableSlots ?: 0) > 0
    val isConfirmed = departure.status == TravelDepartureResponse.Status.CONFIRMED
    val isBookable = isAvailable && isConfirmed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Sezione date - Prima riga
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${departure.startDate ?: stringResource(R.string.to_be_decided_abbr)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${departure.endDate ?: stringResource(R.string.to_be_decided_abbr)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sezione prezzo, disponibilità e pulsante - Seconda riga
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info prezzo e disponibilità a sinistra
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${departure.price ?: stringResource(R.string.not_available_abbr)} €",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!isAvailable) {
                        Text(
                            text = stringResource(R.string.sold_out),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.available_seats, departure.availableSlots ?: 0),
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }

                // Pulsante prenota a destra
                Button(
                    onClick = { departure.id?.let { onBookClick(it.toString()) } },
                    enabled = isBookable,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isConfirmed) stringResource(R.string.book_now) else stringResource(R.string.in_planning))
                }
            }
        }
    }
}

