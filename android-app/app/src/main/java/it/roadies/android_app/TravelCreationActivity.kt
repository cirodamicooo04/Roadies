package it.roadies.android_app

import android.graphics.drawable.Icon
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import it.roadies.android_app.client.models.travel.TagResponse
import it.roadies.android_app.ui.travel.components.BoxCentered
import it.roadies.android_app.utils.ContinentMapper
import it.roadies.android_app.viewmodel.CreateTravelStep
import it.roadies.android_app.viewmodel.CreateTravelUiState
import it.roadies.android_app.viewmodel.TravelActivityState
import it.roadies.android_app.viewmodel.TravelCreationViewModel
import it.roadies.android_app.viewmodel.UploadableImage
import java.util.UUID

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TravelCreationScreen(
    navHostController: NavHostController, 
    viewModel: TravelCreationViewModel = hiltViewModel(), 
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            BoxCentered(text = uiState.errorMessage)
        } else {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    label = "WizardTransition"
                ) { step ->
                    when (step) {
                        CreateTravelStep.BASIC_INFO -> {
                            BasicInfoForm(state = uiState, viewModel = viewModel)
                        }

                        CreateTravelStep.ACTIVITIES -> {
                            ActivitiesSummaryForm(activities = uiState.activities, onAddClick = {

                            })
                        }

                        CreateTravelStep.DEPARTURES -> {
                            // TODO: Inserisci qui la UI per la lista delle partenze
                        }
                    }
                }
            }

            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (uiState.currentStep == CreateTravelStep.BASIC_INFO) {
                                onNavigateBack()
                            } else {
                                viewModel.previousStep()
                            }
                        },
                        enabled = !uiState.images.any {it.isUploading}
                    ) {
                        Text(
                            if (uiState.currentStep == CreateTravelStep.BASIC_INFO) stringResource(
                                R.string.cancel
                            ) else stringResource(R.string.back)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.nextStep() },
                        enabled = !uiState.images.any {it.isUploading}
                    ) {
                        Text(
                            if (uiState.currentStep == CreateTravelStep.DEPARTURES) stringResource(
                                R.string.create_travel_btn
                            ) else stringResource(R.string.next)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoForm(
    state: CreateTravelUiState,
    viewModel: TravelCreationViewModel
) {
    // Usiamo una Column con verticalScroll per permettere di scorrere se lo schermo è piccolo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.create_new_travel),
            fontWeight = FontWeight.SemiBold,
            fontSize = 35.sp
        )

        ImageCarousel(
            images = state.images,
            onImagesSelected = { uris -> viewModel.uploadImagesFromGallery(uris) },
            onRemoveImage = { uri -> viewModel.removeImage(uri) }
        )

        //title
        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text(stringResource(R.string.title_label)) },
            placeholder = { Text(stringResource(R.string.title_placeholder)) },
            isError = state.fieldErrors.containsKey("title"),
            supportingText = {
                if (state.fieldErrors.containsKey("title")) {
                    Text(text = stringResource(id = state.fieldErrors["title"]!!))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        //description
        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text(stringResource(R.string.description)) },
            placeholder = { Text(stringResource(R.string.description_placeholder)) },
            isError = state.fieldErrors.containsKey("description"),
            supportingText = {
                if (state.fieldErrors.containsKey("description")) {
                    Text(text = stringResource(id = state.fieldErrors["description"]!!))
                }
            },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5
        )

        //duration days
        OutlinedTextField(
            value = state.durationDays,
            onValueChange = { viewModel.updateDurationDays(it) },
            label = { Text(stringResource(R.string.duration_days)) },
            placeholder = { Text(stringResource(R.string.duration_placeholder)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = state.fieldErrors.containsKey("durationDays"),
            supportingText = {
                if (state.fieldErrors.containsKey("durationDays")) {
                    Text(text = stringResource(id = state.fieldErrors["durationDays"]!!))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        //destination
        val suggestions by viewModel.locationSuggestions.collectAsState()
        
        ExposedDropdownMenuBox(
            expanded = suggestions.isNotEmpty(),
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = state.destination,
                onValueChange = { viewModel.searchDestinations(it) },
                label = { Text(stringResource(R.string.insert_destination)) },
                placeholder = { Text(stringResource(R.string.destination_placeholder)) },
                isError = state.fieldErrors.containsKey("destination"),
                supportingText = {
                    if (state.fieldErrors.containsKey("destination")) {
                        Text(text = stringResource(id = state.fieldErrors["destination"]!!))
                    }
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                singleLine = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
            )

            DropdownMenu(
                expanded = suggestions.isNotEmpty(),
                onDismissRequest = { viewModel.clearLocationSuggestions() },
                properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text("${suggestion.name}, ${suggestion.country ?: ""}") },
                        onClick = {
                            val continent = ContinentMapper.getContinent(suggestion.countryCode)
                            val displayLocation = if (!suggestion.country.isNullOrBlank()) "${suggestion.name}, ${suggestion.country}" else suggestion.name
                            viewModel.updateLocation(
                                destination = displayLocation,
                                country = suggestion.country ?: "",
                                continent = continent,
                                latitude = suggestion.latitude ?: 0.0,
                                longitude = suggestion.longitude ?: 0.0
                            )
                            viewModel.clearLocationSuggestions()
                        }
                    )
                }
            }
        }

        TravelTagsSection(tags = state.tags, tagScores = state.tagScores ,onValueChange = { id , score ->
            viewModel.updateTagScore(tagId = id, score = score)
        })
        
    }
}

@Composable
fun TravelTagsSection(tags: List<TagResponse>, tagScores: Map<UUID, Int> , onValueChange: (UUID, Int) -> Unit) {
    if(tags.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)){
        Text(text = stringResource(R.string.rate_trip_type), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 10.dp))

        tags.forEach { tag ->
            val tagId = tag.id?: return@forEach
            val currentScore = tagScores[tagId]?: 3

            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = tag.name ?: "", fontWeight = FontWeight.SemiBold)
                    Text(text = "$currentScore", fontWeight = FontWeight.Bold)
                }
                Slider(value = currentScore.toFloat(), onValueChange = {newValue -> onValueChange(tagId, newValue.toInt())}, valueRange = 1f .. 5f, steps = 3)
            }
        }
    }
}

@Composable
fun ImageCarousel(
    images: List<UploadableImage>,
    onImagesSelected: (List<Uri>) -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImagesSelected(uris)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.travel_photos_label),
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
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

            items(images) { image ->
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .width(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = image.localUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (image.isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else if (image.isFailed) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.photo_upload_failed), color = Color.White)
                        }
                    }

                    if (!image.isUploading) {
                        IconButton(
                            onClick = { onRemoveImage(image.localUri) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Rimuovi",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitiesSummaryForm(activities: List<TravelActivityState>, onAddClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)){
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
            Text(text = stringResource(R.string.activities), fontWeight = FontWeight.Bold, fontSize = 35.sp)
            Button(onClick = {
                //apro la modale di inserimento attività
            }) {
                //Text(text = stringResource(R.string.new_activity))
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.new_activity))
            }
        }

        if (activities.isEmpty()){
            BoxCentered(text = stringResource(R.string.itinerary_empty))
        } else {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                //mostro il riepilogo delle attività
            }

        }

    }
}
