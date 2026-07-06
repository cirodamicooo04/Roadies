package it.roadies.android_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import it.roadies.android_app.client.models.travel.TravelDepartureResponse
import it.roadies.android_app.client.models.travel.TravelUpdateRequest
import it.roadies.android_app.ui.travel.components.*
import it.roadies.android_app.utils.ContinentMapper
import it.roadies.android_app.viewmodel.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelUpdateScreen(
    navHostController: NavHostController, 
    viewModel: TravelUpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModalOfCancel by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            navHostController.previousBackStackEntry?.savedStateHandle?.set("travel_updated", true)
            navHostController.popBackStack()
        }
    }

    val onNavigateBack = {
        navHostController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.isInitialLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            BoxCentered(text = uiState.errorMessage)
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                TravelUpdateForm(state = uiState, viewModel = viewModel)
            }

            if (showModalOfCancel) {
                AlertDialog(
                    onDismissRequest = { showModalOfCancel = false },
                    title = { Text(stringResource(R.string.cancel_update_confirm)) },
                    text = { Text(stringResource(R.string.cancel_creation_confirm_body)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showModalOfCancel = false
                            onNavigateBack()
                        }) { Text(stringResource(R.string.confirm)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showModalOfCancel = false }) { Text(stringResource(R.string.cancel)) }
                    }
                )
            }

            if (uiState.updateErrorMessage != null) {
                Text(
                    text = uiState.updateErrorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showModalOfCancel = true },
                    enabled = !uiState.images.any { it.isUploading } && !uiState.isUpdating
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (uiState.isEditable) {
                    Button(
                        onClick = { viewModel.submitUpdate() },
                        enabled = !uiState.images.any { it.isUploading } && !uiState.isUpdating
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelUpdateForm(
    state: TravelUpdateUiState,
    viewModel: TravelUpdateViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.basic_info),
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp
        )

        if (!state.isEditable) {
            NotEditableInfoBanner(text = stringResource(R.string.not_editable_travel))
        }

        ImageCarousel(
            images = state.images,
            onImagesSelected = { uris -> viewModel.uploadImagesFromGallery(uris) },
            onRemoveImage = { uri -> viewModel.removeImage(uri) },
            isEditable = state.isEditable
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text(stringResource(R.string.title_label)) },
            isError = state.fieldErrors.containsKey("title"),
            supportingText = {
                if (state.fieldErrors.containsKey("title")) {
                    Text(text = stringResource(id = state.fieldErrors["title"]!!))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = state.isEditable,
            readOnly = !state.isEditable
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text(stringResource(R.string.description)) },
            isError = state.fieldErrors.containsKey("description"),
            supportingText = {
                if (state.fieldErrors.containsKey("description")) {
                    Text(text = stringResource(id = state.fieldErrors["description"]!!))
                }
            },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5,
            enabled = state.isEditable,
            readOnly = !state.isEditable
        )

        OutlinedTextField(
            value = state.durationDays?.toString() ?: "",
            onValueChange = { viewModel.updateDurationDays(it) },
            label = { Text(stringResource(R.string.duration_days)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.fieldErrors.containsKey("durationDays"),
            supportingText = {
                if (state.fieldErrors.containsKey("durationDays")) {
                    Text(text = stringResource(id = state.fieldErrors["durationDays"]!!))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = state.isEditable,
            readOnly = !state.isEditable
        )

        val suggestions by viewModel.locationSuggestions.collectAsState()

        ExposedDropdownMenuBox(
            expanded = suggestions.isNotEmpty(),
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = state.destination,
                onValueChange = { viewModel.searchDestinations(it) },
                label = { Text("Destinazione") },
                isError = state.fieldErrors.containsKey("destination"),
                supportingText = {
                    if (state.fieldErrors.containsKey("destination")) {
                        Text(text = stringResource(id = state.fieldErrors["destination"]!!))
                    }
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                singleLine = true,
                enabled = state.isEditable,
                readOnly = !state.isEditable
            )
            ExposedDropdownMenu(
                expanded = suggestions.isNotEmpty() && state.isEditable,
                onDismissRequest = { viewModel.clearLocationSuggestions() }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion.name) },
                        onClick = {
                            viewModel.updateLocation(
                                destination = suggestion.name,
                                country = suggestion.country ?: "",
                                continent = TravelUpdateRequest.Continent.valueOf(ContinentMapper.getContinent(suggestion.countryCode).name),
                                latitude = suggestion.latitude ?: 0.0,
                                longitude = suggestion.longitude ?: 0.0
                            )
                        }
                    )
                }
            }
        }

        TravelTagsSection(
            tags = state.tags,
            tagScores = state.tagScores,
            isEditable = state.isEditable,
            onValueChange = { uuid, score -> viewModel.updateTagScore(uuid, score) }
        )

        val addressSuggestions by viewModel.addressSuggestions.collectAsState()

        ActivitiesUpdateSection(
            activities = state.activities,
            suggestions = addressSuggestions,
            onSearchAddress = { viewModel.searchAddresses(it) },
            onClearSuggestions = { viewModel.clearAddressSuggestions() },
            onSaveActivity = { viewModel.saveActivity(it) },
            onDeleteActivity = { viewModel.removeActivity(it) },
            alreadySelectedDays = state.activities.map { it.dayNumber },
            travelDurationDays = state.durationDays ?: 1,
            travelContinent = state.continent,
            travelCountry = state.country,
            travelDestination = state.destination,
            isEditable = state.isEditable
        )

        DeparturesUpdateSection(
            departures = state.departures,
            travelDurationDays = state.durationDays ?: 1,
            onSaveDeparture = { viewModel.saveDeparture(it) },
            onDeleteDeparture = { viewModel.removeDeparture(it) },
            onConfirmDeparture = { viewModel.confirmDeparture(it) },
            isEditable = state.isEditable
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesUpdateSection(
    activities: List<TravelActivityUpdateState>,
    suggestions: List<SearchSuggestion>,
    onSearchAddress: (String) -> Unit,
    onClearSuggestions: () -> Unit,
    onSaveActivity: (TravelActivityUpdateState) -> Unit,
    onDeleteActivity: (UUID) -> Unit,
    alreadySelectedDays: List<Int>,
    travelDurationDays: Int,
    travelContinent: TravelUpdateRequest.Continent,
    travelCountry: String,
    travelDestination: String,
    isEditable: Boolean = true
) {
    var isModalSheetOpen by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<TravelActivityUpdateState?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isModalSheetOpen) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { isModalSheetOpen = false}) {
            val initial = remember(activityToEdit) {
                activityToEdit ?: TravelActivityUpdateState(
                    isNew = true,
                    continent = travelContinent,
                    country = travelCountry,
                    destination = travelDestination
                )
            }
            TravelActivityUpdateForm(
                initialActivity = initial,
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

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)){
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
            Text(text = stringResource(R.string.activities), fontWeight = FontWeight.Bold, fontSize = 24.sp)
            if (isEditable) {
                Button(onClick = {
                    activityToEdit = null
                    isModalSheetOpen = true
                }, enabled = alreadySelectedDays.size < travelDurationDays) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.create_new_activity))
                }
            }
        }

        if (activities.isEmpty()){
            BoxCentered(text = stringResource(R.string.itinerary_empty))
        } else {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val groupedActivities = activities.groupBy { it.dayNumber }.toSortedMap()
                
                groupedActivities.forEach { (day, dayActivities) ->
                    Text(
                        text = "${stringResource(R.string.day)} $day", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 20.sp, 
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    dayActivities.forEach { activity -> 
                        ActivityUpdateSummaryCard(
                            activity = activity,
                            onEditClick = {
                                activityToEdit = activity
                                isModalSheetOpen = true
                            },
                            onDeleteClick = {
                                onDeleteActivity(activity.id)
                            },
                            isEditable = isEditable
                        ) 
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityUpdateSummaryCard(
    activity: TravelActivityUpdateState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isEditable: Boolean = true
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
                
                if (isEditable) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = onEditClick) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeparturesUpdateSection(
    departures: List<TravelDepartureUpdateState>,
    travelDurationDays: Int,
    onSaveDeparture: (TravelDepartureUpdateState) -> Unit,
    onDeleteDeparture: (UUID) -> Unit,
    onConfirmDeparture: (UUID) -> Unit,
    isEditable: Boolean = true
) {
    var isModalSheetOpen by remember { mutableStateOf(false) }
    var departureToEdit by remember { mutableStateOf<TravelDepartureUpdateState?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var departureToConfirm by remember { mutableStateOf<UUID?>(null)}

    departureToConfirm?.let { departureId ->
        AlertDialog(
            onDismissRequest = {departureToConfirm = null},
            title = { Text(stringResource(R.string.confirm_departure)) },
            text = { Text(stringResource(R.string.confirm_departure_body_travel))},
            confirmButton = {
                Button(onClick = {
                    onConfirmDeparture(departureId)
                    departureToConfirm = null
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    departureToConfirm = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (isModalSheetOpen) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { isModalSheetOpen = false }) {
            DepartureUpdateForm(
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

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.departures), fontWeight = FontWeight.Bold, fontSize = 24.sp)
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
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                departures.sortedBy { it.startDate }.forEach { departure ->
                    DepartureUpdateSummaryCard(
                        departure = departure,
                        onEditClick = {
                            departureToEdit = departure
                            isModalSheetOpen = true
                        },
                        onDeleteClick = {
                            onDeleteDeparture(departure.id)
                        },
                        onConfirmClick = {
                            departureToConfirm = departure.id
                        },
                        isEditable = isEditable
                    )
                }
            }
        }
    }
}

@Composable
fun DepartureUpdateSummaryCard(
    departure: TravelDepartureUpdateState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmClick: () -> Unit,
    isEditable: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${departure.startDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: ""} - ${departure.endDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                when (departure.status) {
                    TravelDepartureResponse.Status.CONFIRMED -> {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = stringResource(R.string.confirmed), color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    TravelDepartureResponse.Status.FULL -> {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = stringResource(R.string.sold_out_status), color = Color(0xFFF44336), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = stringResource(R.string.planned), color = Color(0xFFFF9800), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.price_format, departure.price.toString()), style = MaterialTheme.typography.bodyMedium)
                Text(text = stringResource(R.string.max_slots_format, departure.maxSlots.toString()), style = MaterialTheme.typography.bodyMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (departure.status != TravelDepartureResponse.Status.CONFIRMED) {
                    IconButton(onClick = onConfirmClick) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Conferma", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureUpdateForm(
    initialDeparture: TravelDepartureUpdateState? = null,
    travelDurationDays: Int,
    onSaveClick: (TravelDepartureUpdateState) -> Unit,
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

    val fieldErrors = initialDeparture?.fieldErrors ?: emptyMap()
    if (fieldErrors.isNotEmpty()) {
        if (fieldErrors.containsKey("startDate")) startDateError = stringResource(fieldErrors["startDate"]!!)
        if (fieldErrors.containsKey("endDate")) endDateError = stringResource(fieldErrors["endDate"]!!)
        if (fieldErrors.containsKey("duration")) endDateError = stringResource(fieldErrors["duration"]!!, travelDurationDays)
        if (fieldErrors.containsKey("price")) priceError = stringResource(fieldErrors["price"]!!)
        if (fieldErrors.containsKey("maxSlots")) maxSlotsError = stringResource(fieldErrors["maxSlots"]!!)
    }

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
                UpdateDatePickerField(
                    label = stringResource(R.string.departure_start_date),
                    selectedDate = startDate,
                    onDateSelected = { startDate = it; startDateError = null; endDateError = null },
                    isError = startDateError != null,
                    errorMessage = startDateError
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                UpdateDatePickerField(
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
                val parsedPrice = priceText.toBigDecimalOrNull()
                val parsedSlots = maxSlotsText.toIntOrNull()
                
                val newDeparture = TravelDepartureUpdateState(
                    id = initialDeparture?.id ?: UUID.randomUUID(),
                    isNew = initialDeparture?.isNew ?: true,
                    startDate = startDate,
                    endDate = endDate,
                    price = parsedPrice,
                    maxSlots = parsedSlots,
                    status = initialDeparture?.status ?: TravelDepartureResponse.Status.PLANNED,
                    fieldErrors = emptyMap()
                )
                onSaveClick(newDeparture)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

