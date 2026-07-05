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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun Reviews(
    isLoading: Boolean,
    errorMessage: String?,
    reviews: List<it.roadies.android_app.client.models.review.ReviewResponse>?,
    usersInfo: Map<String, it.roadies.android_app.client.models.user.MinimalInformationResponseDTO>,
    currentUserId: String?,
    travelOwnerId: String?,
    onDeleteReview: (UUID) -> Unit,
    onEditReview: (UUID, Int, String) -> Unit,
    onReplyReview: (UUID, String) -> Unit
) {
    var isListSheetOpen by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<it.roadies.android_app.client.models.review.ReviewResponse?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var reviewToDelete by remember { mutableStateOf<it.roadies.android_app.client.models.review.ReviewResponse?>(null) }
    var reviewToEdit by remember { mutableStateOf<it.roadies.android_app.client.models.review.ReviewResponse?>(null) }
    var reviewToReply by remember { mutableStateOf<it.roadies.android_app.client.models.review.ReviewResponse?>(null) }
    
    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text(stringResource(R.string.delete_review_title)) },
            text = { Text(stringResource(R.string.delete_review_msg)) },
            confirmButton = {
                Button(onClick = {
                    reviewToDelete?.id?.let { onDeleteReview(it) }
                    reviewToDelete = null
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { reviewToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
    
    if (reviewToReply != null) {
        var replyContent by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reviewToReply = null },
            title = { Text(stringResource(R.string.reply_review_title)) },
            text = {
                OutlinedTextField(
                    value = replyContent,
                    onValueChange = { replyContent = it },
                    label = { Text(stringResource(R.string.type_your_reply)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToReply?.id?.let { onReplyReview(it, replyContent.trim()) }
                        reviewToReply = null
                    },
                    enabled = replyContent.trim().isNotEmpty()
                ) { Text(stringResource(R.string.submit)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { reviewToReply = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (reviewToEdit != null) {
        var editContent by remember { mutableStateOf(reviewToEdit?.content ?: "") }
        var editRating by remember { mutableStateOf(reviewToEdit?.rating ?: 5) }
        AlertDialog(
            onDismissRequest = { reviewToEdit = null },
            title = { Text(stringResource(R.string.edit_review_title)) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i <= editRating) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { editRating = i }
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text(stringResource(R.string.type_your_review)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToEdit?.id?.let { onEditReview(it, editRating, editContent.trim()) }
                        reviewToEdit = null
                    },
                    enabled = editContent.trim().isNotEmpty()
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { reviewToEdit = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        if (isLoading) {
            Text(
                text = stringResource(R.string.reviews),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Text(
                text = stringResource(R.string.reviews),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.loading_reviews_error), color = MaterialTheme.colorScheme.error)
            }
        } else if (reviews.isNullOrEmpty()) {
            Text(
                text = stringResource(R.string.reviews),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.no_reviews), color = Color.Gray)
            }
        } else {
            val totalReviews = reviews.size
            val averageRating = reviews.map { it.rating }.average()
            val formattedRating = String.format(java.util.Locale.US, "%.1f", averageRating)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.reviews),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700), // Gold color
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = formattedRating,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            val topReviews = reviews.take(5)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(topReviews) { review ->
                    ReviewCard(
                        review = review,
                        user = usersInfo[review.userId],
                        modifier = Modifier.width(300.dp).height(240.dp).padding(bottom = 8.dp),
                        currentUserId = currentUserId,
                        travelOwnerId = travelOwnerId,
                        onEditClick = { reviewToEdit = review },
                        onDeleteClick = { reviewToDelete = review },
                        onReplyClick = { reviewToReply = review },
                        onReviewClick = { selectedReview = it }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { isListSheetOpen = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.show_all_reviews, totalReviews))
            }
        }
    }

    if (isListSheetOpen && !reviews.isNullOrEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { isListSheetOpen = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .fillMaxHeight(0.9f)
            ) {
                Text(
                    text = stringResource(R.string.all_reviews, reviews.size),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reviews) { review ->
                        ReviewCard(
                            review = review,
                            user = usersInfo[review.userId],
                            modifier = Modifier.fillMaxWidth().height(240.dp).padding(bottom = 8.dp),
                            currentUserId = currentUserId,
                            travelOwnerId = travelOwnerId,
                            onEditClick = { reviewToEdit = review },
                            onDeleteClick = { reviewToDelete = review },
                            onReplyClick = { reviewToReply = review },
                            onReviewClick = { selectedReview = it }
                        )
                    }
                }
            }
        }
    }

    selectedReview?.let { review ->
        ReviewDetailSheet(
            review = review,
            user = usersInfo[review.userId],
            onDismiss = { selectedReview = null }
        )
    }
}

@Composable
fun ReviewCard(
    review: it.roadies.android_app.client.models.review.ReviewResponse, 
    user: it.roadies.android_app.client.models.user.MinimalInformationResponseDTO?, 
    modifier: Modifier = Modifier, 
    currentUserId: String? = null,
    travelOwnerId: String? = null,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onReplyClick: () -> Unit = {},
    onReviewClick: (it.roadies.android_app.client.models.review.ReviewResponse) -> Unit
) {
    val username = user?.username ?: stringResource(R.string.fictitious_user)
    val avatarInitial = username.take(1).uppercase()
    
    Card(
        modifier = modifier.clickable { onReviewClick(review) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarInitial,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i <= review.rating) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))

            ExpandableReviewText(
                text = review.content,
                onReadMoreClick = { onReviewClick(review) }
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

            if (review.reply != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReviewClick(review) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.reply_from_organizer),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            } else if (currentUserId == travelOwnerId) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onReplyClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.reply))
                }
            }

            if (currentUserId == review.userId) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.edit))
                    }
                    OutlinedButton(onClick = onDeleteClick, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableReviewText(text: String, modifier: Modifier = Modifier, onReadMoreClick: () -> Unit) {
    var isClickable by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.hasVisualOverflow) {
                    isClickable = true
                }
            }
        )

        if (isClickable) {
            Text(
                text = stringResource(R.string.read_more),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onReadMoreClick() }
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailSheet(review: it.roadies.android_app.client.models.review.ReviewResponse, user: it.roadies.android_app.client.models.user.MinimalInformationResponseDTO?, onDismiss: () -> Unit) {
    val username = user?.username ?: stringResource(R.string.fictitious_user)
    val avatarInitial = username.take(1).uppercase()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.review_details),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarInitial,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i <= review.rating) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = review.content,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            review.reply?.let { reply ->
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.organizer_reply),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reply.content,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
