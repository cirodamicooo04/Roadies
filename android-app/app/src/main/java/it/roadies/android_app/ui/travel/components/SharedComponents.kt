package it.roadies.android_app.ui.travel.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import it.roadies.android_app.R
import it.roadies.android_app.client.models.travel.ImageResponse
import org.osmdroid.util.GeoPoint

@Composable
fun BoxCentered(text: String? = null) {
    Box(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text ?: "", fontSize = 16.sp, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun DetailHeader(
    title: String?, 
    destination: String?, 
    country: String?, 
    trailingContent: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 23.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = title ?: "", fontSize = 35.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${destination ?: ""}, ${country ?: ""}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            trailingContent()
        }
    }
}

@Composable
fun DetailImageCarousel(images: List<ImageResponse>?) {
    if (images.isNullOrEmpty()) {
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

    val pagerState = rememberPagerState(pageCount = { images.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.padding(bottom = 18.dp)
    ) { page ->
        val imageUrl = images[page].url

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            SubcomposeAsyncImage(
                //Risolvere problema ip
                //model = imageUrl,
                model = "http://10.0.2.2:9000/travels/69b14ce2-af34-497f-8d03-f2555600700e-Screenshot_2026-04-11_alle_20.38.04_(2).png",
                contentDescription = "Foto",
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
fun ExpandableDescription(description: String?) {
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
fun LocationMap(lon: Double?, lat: Double?) {
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
fun CheckAvailabilityButton(onClick: () -> Unit){
    Button(modifier = Modifier.fillMaxWidth().padding(12.dp), onClick = onClick) {
        Text(text = stringResource(R.string.check_availability), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}


