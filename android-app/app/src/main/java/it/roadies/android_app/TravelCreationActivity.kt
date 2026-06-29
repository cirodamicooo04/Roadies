package it.roadies.android_app

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.client.models.travel.TagResponse
import it.roadies.android_app.client.models.travel.TravelCreateRequest
import it.roadies.android_app.ui.travel.components.ActivityAddressPicker
import it.roadies.android_app.ui.travel.components.ActivityTitleDescriptionInput
import it.roadies.android_app.ui.travel.components.BoxCentered
import it.roadies.android_app.ui.travel.components.UploadableImage
import it.roadies.android_app.utils.ContinentMapper
import it.roadies.android_app.viewmodel.CreateTravelStep
import it.roadies.android_app.viewmodel.CreateTravelUiState
import it.roadies.android_app.viewmodel.TravelActivityState
import it.roadies.android_app.viewmodel.TravelCreationDepartureState
import it.roadies.android_app.viewmodel.TravelCreationViewModel
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TravelCreationScreen(
    navHostController: NavHostController, 
    viewModel: TravelCreationViewModel = hiltViewModel(), 
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showModalOfCancelCreation by remember { mutableStateOf(false) }
    var showModalOfCreation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isCreationSuccess) {
        if (uiState.isCreationSuccess) {
            navHostController.previousBackStackEntry?.savedStateHandle?.set("travel_created", true)
            navHostController.popBackStack()
        }
    }

    BackHandler {
        if (uiState.currentStep == CreateTravelStep.BASIC_INFO) {
            showModalOfCancelCreation = true
        } else {
            viewModel.previousStep()
        }
    }

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
                            val suggestions by viewModel.addressSuggestions.collectAsState()
                            ActivitiesSummaryForm(
                                activities = uiState.activities,
                                suggestions = suggestions,
                                onSearchAddress = { viewModel.searchAddresses(it) },
                                onClearSuggestions = { viewModel.clearAddressSuggestions() },
                                onSaveActivity = { activity -> viewModel.saveActivity(activity) },
                                onDeleteActivity = { activityId -> viewModel.removeActivity(activityId) },
                                travelDurationDays = uiState.durationDays ?: 1,
                                travelContinent = uiState.continent,
                                travelCountry = uiState.country,
                                travelDestination = uiState.destination
                            )
                        }

                        CreateTravelStep.DEPARTURES -> {
                            val durationDays = uiState.durationDays ?: 1
                            DeparturesSummaryForm(
                                departures = uiState.departures,
                                travelDurationDays = durationDays,
                                onSaveDeparture = { viewModel.saveDeparture(it) },
                                onDeleteDeparture = { viewModel.removeDeparture(it) }
                            )
                        }
                    }
                }
            }

            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.creationErrorMessage != null) {
                        Text(
                            text = uiState.creationErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (uiState.currentStep == CreateTravelStep.BASIC_INFO) {
                                    showModalOfCancelCreation = true
                                } else {
                                    viewModel.previousStep()
                                }
                            },
                            enabled = !uiState.images.any {it.isUploading} && !uiState.isCreationLoading
                        ) {
                            Text(
                                if (uiState.currentStep == CreateTravelStep.BASIC_INFO) stringResource(
                                    R.string.cancel
                                ) else stringResource(R.string.back)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (uiState.currentStep == CreateTravelStep.DEPARTURES) {
                                    showModalOfCreation = true
                                } else {
                                    viewModel.nextStep()
                                }
                            },
                            enabled = !uiState.images.any {it.isUploading} && !uiState.isCreationLoading
                        ) {
                            if (uiState.isCreationLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
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
    }

    if (showModalOfCancelCreation){
        AlertDialog(
            onDismissRequest = {showModalOfCancelCreation = false},
            title = {Text(text = stringResource(R.string.cancel_creation_confirm))},
            text = {Text(text = stringResource(R.string.cancel_creation_confirm_body))},
            confirmButton = {
                Button(onClick = {
                    onNavigateBack()
                    showModalOfCancelCreation = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showModalOfCancelCreation = false
                }) {
                    Text(text = stringResource(R.string.cancel))
                }

            }
        )
    }

    if (showModalOfCreation) {
        AlertDialog(
            onDismissRequest = { showModalOfCreation = false },
            title = { Text(text = stringResource(R.string.create_travel_confirm)) },
            text = { Text(text = stringResource(R.string.create_travel_confirm_body)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.nextStep()
                    showModalOfCreation = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showModalOfCreation = false
                }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
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
            value = state.durationDays?.toString() ?: "",
            onValueChange = {
                val intDuration = it.toIntOrNull()
                if (intDuration == null || intDuration < state.activities.size){

                }
                viewModel.updateDurationDays(it)
                            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesSummaryForm(
    activities: List<TravelActivityState>,
    suggestions: List<SearchSuggestion>,
    onSearchAddress: (String) -> Unit,
    onClearSuggestions: () -> Unit,
    onSaveActivity: (TravelActivityState) -> Unit,
    onDeleteActivity: (UUID) -> Unit,
    travelDurationDays: Int,
    travelContinent: TravelCreateRequest.Continent,
    travelCountry: String,
    travelDestination: String
) {
    var isModalSheetOpen by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<TravelActivityState?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val alreadySelectedDays = activities.filter { it.id != activityToEdit?.id }.map { it.dayNumber }

    if (isModalSheetOpen){
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { isModalSheetOpen = false}) {
            TravelActivityCreationForm(
                initialActivity = activityToEdit,
                suggestions = suggestions,
                onSearchAddress = onSearchAddress,
                onClearSuggestions = onClearSuggestions,
                onSaveClick = { newActivity ->
                    onSaveActivity(newActivity)
                    isModalSheetOpen = false
                },
                onCancelClick = {
                    isModalSheetOpen = false
                },
                alreadySelectedDays = alreadySelectedDays,
                travelDurationDays = travelDurationDays,
                travelContinent = travelContinent,
                travelCountry = travelCountry,
                travelDestination = travelDestination

            )
        }
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)){
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
            Text(text = stringResource(R.string.activities), fontWeight = FontWeight.Bold, fontSize = 35.sp)
            Button(onClick = {
                activityToEdit = null
                isModalSheetOpen = true
            }, enabled = alreadySelectedDays.size < travelDurationDays) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.new_activity))
            }
        }

        if (activities.isEmpty()){
            BoxCentered(text = stringResource(R.string.itinerary_empty))
        } else {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val groupedActivities = activities.groupBy { it.dayNumber }.toSortedMap()
                
                groupedActivities.forEach { (day, dayActivities) ->
                    Text(
                        text = "${stringResource(R.string.day)} $day", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 24.sp, 
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    dayActivities.forEach { activity -> 
                        ActivitySummaryCard(
                            activity = activity,
                            onEditClick = {
                                activityToEdit = activity
                                isModalSheetOpen = true
                            },
                            onDeleteClick = {
                                onDeleteActivity(activity.id)
                            }
                        ) 
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitySummaryCard(
    activity: TravelActivityState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            if (activity.images.isNotEmpty()) {
                val image = activity.images.first()
                AsyncImage(
                    model = image.localUri,
                    contentDescription = activity.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activity.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelActivityCreationForm(
    initialActivity: TravelActivityState? = null,
    suggestions: List<SearchSuggestion>,
    onSearchAddress: (String) -> Unit,
    onClearSuggestions: () -> Unit,
    alreadySelectedDays: List<Int>,
    onSaveClick: (TravelActivityState) -> Unit,
    onCancelClick: () -> Unit,
    travelDurationDays: Int,
    travelContinent: TravelCreateRequest.Continent,
    travelCountry: String,
    travelDestination: String
) {
    var name by remember(initialActivity?.id) { mutableStateOf(initialActivity?.name ?: "") }
    var description by remember(initialActivity?.id) { mutableStateOf(initialActivity?.description ?: "") }
    var dayNumber by remember(initialActivity?.id) { mutableIntStateOf(initialActivity?.dayNumber ?: 0) }
    var addressQuery by remember(initialActivity?.id) { mutableStateOf(initialActivity?.address ?: "") }
    var selectedLatitude by remember(initialActivity?.id) { mutableStateOf<Double?>(initialActivity?.latitude) }
    var selectedLongitude by remember(initialActivity?.id) { mutableStateOf<Double?>(initialActivity?.longitude) }
    var images by remember(initialActivity?.id) { mutableStateOf(initialActivity?.images ?: emptyList<UploadableImage>()) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.create_new_activity),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        ImageCarousel(
            images = images,
            onImagesSelected = { uris -> 
                val newImages = uris.map { uri ->
                    UploadableImage(localUri = uri, isUploading = false)
                }
                images = images + newImages
            },
            onRemoveImage = { uri -> 
                images = images.filter { it.localUri != uri }
            }
        )

        ActivityTitleDescriptionInput(
            name = name,
            onNameChange = { name = it; nameError = null },
            description = description,
            onDescriptionChange = { description = it; descriptionError = null },
            nameError = nameError,
            descriptionError = descriptionError
        )

        ActivityAddressPicker(
            addressQuery = addressQuery,
            onAddressQueryChange = { 
                addressQuery = it
                addressError = null
                onSearchAddress(it)
            },
            suggestions = suggestions,
            onSuggestionSelected = { suggestion ->
                val displayLocation = if (!suggestion.country.isNullOrBlank()) "${suggestion.name}, ${suggestion.country}" else suggestion.name
                addressQuery = displayLocation
                selectedLatitude = suggestion.latitude
                selectedLongitude = suggestion.longitude
                addressError = null
                onClearSuggestions()
            },
            addressError = addressError
        )

        var expandedDayDropdown by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedDayDropdown,
            onExpandedChange = { expandedDayDropdown = it }
        ) {
            OutlinedTextField(
                value = if (dayNumber > 0) dayNumber.toString() else "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.day_number_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDayDropdown)
                }
            )
            DropdownMenu(
                expanded = expandedDayDropdown,
                onDismissRequest = { expandedDayDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                for (i in 1..travelDurationDays) {
                    if (i !in alreadySelectedDays){
                        DropdownMenuItem(
                        text = { Text(stringResource(R.string.day) + " $i") },
                        onClick = {
                            dayNumber = i
                            expandedDayDropdown = false
                        })
                    }
                }
            }
        }

        val titleInvalidError = stringResource(R.string.error_title_invalid)
        val descriptionInvalidError = stringResource(R.string.error_description_invalid)
        val addressInvalidError = stringResource(R.string.insert_address)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancelClick) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    var isValid = true

                    if (name.length !in 3..50) {
                        nameError = titleInvalidError
                        isValid = false
                    }
                    if (description.isNotBlank() && description.length !in 20..1000) {
                        descriptionError = descriptionInvalidError
                        isValid = false
                    }
                    if (selectedLatitude == null || selectedLongitude == null || addressQuery.isBlank()) {
                        addressError = addressInvalidError
                        isValid = false
                    }

                    if (isValid) {
                        val newActivity = TravelActivityState(
                            id = initialActivity?.id ?: UUID.randomUUID(),
                            name = name,
                            description = description,
                            address = addressQuery,
                            latitude = selectedLatitude,
                            longitude = selectedLongitude,
                            dayNumber = dayNumber,
                            continent = travelContinent,
                            country = travelCountry,
                            destination = travelDestination,
                            images = images
                        )
                        onSaveClick(newActivity)
                    }
                },
                enabled = dayNumber > 0
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeparturesSummaryForm(
    departures: List<TravelCreationDepartureState>,
    travelDurationDays: Int,
    onSaveDeparture: (TravelCreationDepartureState) -> Unit,
    onDeleteDeparture: (UUID) -> Unit
) {
    var isModalSheetOpen by remember { mutableStateOf(false) }
    var departureToEdit by remember { mutableStateOf<TravelCreationDepartureState?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isModalSheetOpen) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { isModalSheetOpen = false }) {
            DepartureCreateForm(
                initialDeparture = departureToEdit,
                travelDurationDays = travelDurationDays,
                onSaveClick = { departureState ->
                    onSaveDeparture(departureState)
                    isModalSheetOpen = false
                },
                onCancelClick = {
                    isModalSheetOpen = false
                }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.departures), fontWeight = FontWeight.Bold, fontSize = 35.sp)
            Button(onClick = {
                departureToEdit = null
                isModalSheetOpen = true
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.new_departure))
            }
        }

        if (departures.isEmpty()) {
            BoxCentered(text = stringResource(R.string.departures_empty))
        } else {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                departures.sortedBy { it.startDate }.forEach { departure ->
                    DepartureSummaryCard(
                        departure = departure,
                        onEditClick = {
                            departureToEdit = departure
                            isModalSheetOpen = true
                        },
                        onDeleteClick = {
                            onDeleteDeparture(departure.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DepartureSummaryCard(
    departure: TravelCreationDepartureState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "${departure.startDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} - ${departure.endDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${stringResource(R.string.departure_price)}: ${departure.price} €", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${stringResource(R.string.departure_max_slots)}: ${departure.maxSlots}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureCreateForm(
    initialDeparture: TravelCreationDepartureState? = null,
    travelDurationDays: Int,
    onSaveClick: (TravelCreationDepartureState) -> Unit,
    onCancelClick: () -> Unit
) {
    var startDate by remember(initialDeparture?.id) { mutableStateOf<LocalDate?>(initialDeparture?.startDate) }
    var endDate by remember(initialDeparture?.id) { mutableStateOf<LocalDate?>(initialDeparture?.endDate) }
    var priceText by remember(initialDeparture?.id) { mutableStateOf(initialDeparture?.price?.toString() ?: "") }
    var maxSlotsText by remember(initialDeparture?.id) { mutableStateOf(initialDeparture?.maxSlots?.toString() ?: "") }

    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var maxSlotsError by remember { mutableStateOf<String?>(null) }

    val errorStartDateStr = stringResource(R.string.error_start_date_invalid)
    val errorEndDateStr = stringResource(R.string.error_end_date_invalid)
    val errorDurationStr = stringResource(R.string.error_departure_duration, travelDurationDays)
    val errorPriceStr = stringResource(R.string.error_price_invalid)
    val errorMaxSlotsStr = stringResource(R.string.error_max_slots_invalid)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(if(initialDeparture == null) R.string.new_departure else R.string.edit), 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onCancelClick) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Chiudi")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DatePickerField(
                    label = stringResource(R.string.departure_start_date),
                    selectedDate = startDate,
                    onDateSelected = { startDate = it; startDateError = null; endDateError = null },
                    isError = startDateError != null,
                    errorMessage = startDateError
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DatePickerField(
                    label = stringResource(R.string.departure_end_date),
                    selectedDate = endDate,
                    onDateSelected = { endDate = it; endDateError = null; startDateError = null },
                    isError = endDateError != null,
                    errorMessage = endDateError
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it; priceError = null },
                label = { Text(stringResource(R.string.departure_price)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = priceError != null,
                supportingText = priceError?.let { { Text(it) } },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = maxSlotsText,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() }) {
                        maxSlotsText = it
                        maxSlotsError = null 
                    }
                },
                label = { Text(stringResource(R.string.departure_max_slots)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = maxSlotsError != null,
                supportingText = maxSlotsError?.let { { Text(it) } },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                var isValid = true
                
                if (startDate == null) {
                    startDateError = errorStartDateStr
                    isValid = false
                }
                if (endDate == null || (startDate != null && endDate!!.isBefore(startDate))) {
                    endDateError = errorEndDateStr
                    isValid = false
                }
                
                if (startDate != null && endDate != null && !endDate!!.isBefore(startDate)) {
                    val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
                    if (daysBetween != travelDurationDays) {
                        endDateError = errorDurationStr
                        isValid = false
                    }
                }

                val parsedPrice = priceText.toBigDecimalOrNull()
                if (parsedPrice == null || parsedPrice <= BigDecimal.ZERO) {
                    priceError = errorPriceStr
                    isValid = false
                }
                val parsedSlots = maxSlotsText.toIntOrNull()
                if (parsedSlots == null || parsedSlots <= 0) {
                    maxSlotsError = errorMaxSlotsStr
                    isValid = false
                }

                if (isValid) {
                    val newDeparture = TravelCreationDepartureState(
                        id = initialDeparture?.id ?: UUID.randomUUID(),
                        startDate = startDate,
                        endDate = endDate,
                        price = parsedPrice,
                        maxSlots = parsedSlots
                    )
                    onSaveClick(newDeparture)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
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
            // Invisible box over the text field to capture clicks while disabled
            Box(modifier = Modifier.matchParentSize().clickable { showDialog = true })
        }
        if (isError && errorMessage != null) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onDateSelected(date)
                    }
                    showDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
