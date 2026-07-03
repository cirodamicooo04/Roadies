package it.roadies.android_app.ui.travel.components

import android.net.Uri
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Add
import coil3.compose.AsyncImage
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.draw.clip
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import java.util.UUID
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.Slider
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.CircleShape
import it.roadies.android_app.client.models.travel.TagResponse
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder

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
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    trailingContent: @Composable () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 23.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = title ?: "", fontSize = 35.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Preferiti",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${destination ?: ""}, ${country ?: ""}", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
        val imageUrl = images[page].url?.replace("localhost","10.0.2.2")

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            SubcomposeAsyncImage(
                //non funziona localhost quindi faccio replace
                model = imageUrl?.replace("localhost","10.0.2.2"),
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

data class UploadableImage(
    val localUri: Uri,
    val imageUUID: UUID? = null,
    val isUploading: Boolean = true,
    val isFailed: Boolean = false
)



@Composable
fun ImageCarousel(
    images: List<UploadableImage>,
    onImagesSelected: (List<Uri>) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    isEditable: Boolean = true
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> onImagesSelected(uris) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isEditable) {
            item {
                Surface(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(120.dp)
                        .width(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_photo),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        items(images) { image ->
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
            ) {
                AsyncImage(
                    model = image.localUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                if (image.isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                
                if (isEditable && !image.isUploading) {
                    IconButton(
                        onClick = { onRemoveImage(image.localUri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Rimuovi immagine",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TravelTagsSection(tags: List<TagResponse>, tagScores: Map<UUID, Int>, isEditable: Boolean = true, onValueChange: (UUID, Int) -> Unit) {
    tags.forEach { tag ->
        val tagId = tag.id ?: return@forEach
        val currentScore = tagScores[tagId] ?: 0
        Slider(value = currentScore.toFloat(), enabled = isEditable, onValueChange = {newValue -> onValueChange(tagId, newValue.toInt())}, valueRange = 1f .. 5f, steps = 3)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun UpdateDatePickerField(
    label: String,
    selectedDate: java.time.LocalDate?,
    onDateSelected: (java.time.LocalDate) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    Column(modifier = modifier) {
        Box {
            androidx.compose.material3.OutlinedTextField(
                value = selectedDate?.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = {},
                label = { Text(label) },
                readOnly = true,
                isError = isError,
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = if(isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Box(modifier = Modifier.matchParentSize().clickable { showDialog = true })
        }
        if (isError && errorMessage != null) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
    }
    
    if (showDialog) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                        onDateSelected(date)
                    }
                    showDialog = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun NotEditableInfoBanner(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color(0xFF1976D2),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
