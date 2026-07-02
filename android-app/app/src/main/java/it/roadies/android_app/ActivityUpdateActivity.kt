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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.roadies.android_app.client.models.travel.ActivityDepartureResponse
import it.roadies.android_app.ui.travel.components.*
import it.roadies.android_app.viewmodel.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityUpdateScreen(
    navHostController: NavHostController, 
    viewModel: ActivityUpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModalOfCancel by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            navHostController.previousBackStackEntry?.savedStateHandle?.set("activity_updated", true)
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
                ActivityUpdateForm(state = uiState, viewModel = viewModel)
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
fun ActivityUpdateForm(
    state: ActivityUpdateUiState,
    viewModel: ActivityUpdateViewModel
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
            value = state.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text(stringResource(R.string.title_label)) },
            isError = state.fieldErrors.containsKey("name"),
            supportingText = {
                if (state.fieldErrors.containsKey("name")) {
                    Text(text = stringResource(id = state.fieldErrors["name"]!!))
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

        val addressSuggestions by viewModel.addressSuggestions.collectAsState()

        ExposedDropdownMenuBox(
            expanded = addressSuggestions.isNotEmpty(),
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = state.address,
                onValueChange = { viewModel.searchAddresses(it) },
                label = { Text("Indirizzo dell'attività") },
                isError = state.fieldErrors.containsKey("address"),
                supportingText = {
                    if (state.fieldErrors.containsKey("address")) {
                        Text(text = stringResource(id = state.fieldErrors["address"]!!))
                    }
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                singleLine = true,
                enabled = state.isEditable,
                readOnly = !state.isEditable
            )
            ExposedDropdownMenu(
                expanded = addressSuggestions.isNotEmpty() && state.isEditable,
                onDismissRequest = { viewModel.clearAddressSuggestions() }
            ) {
                addressSuggestions.forEach { suggestion ->
                    val displayLocation = if (!suggestion.country.isNullOrBlank()) "${suggestion.name}, ${suggestion.country}" else suggestion.name
                    DropdownMenuItem(
                        text = { Text(displayLocation) },
                        onClick = { viewModel.selectAddressSuggestion(suggestion) }
                    )
                }
            }
        }

        ActivityDeparturesUpdateSection(
            departures = state.departures,
            onSaveDeparture = { viewModel.saveDeparture(it) },
            onDeleteDeparture = { viewModel.removeDeparture(it) },
            onConfirmDeparture = { viewModel.confirmDeparture(it) },
            isEditable = state.isEditable
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDeparturesUpdateSection(
    departures: List<ActivityDepartureUpdateState>,
    onSaveDeparture: (ActivityDepartureUpdateState) -> Unit,
    onDeleteDeparture: (UUID) -> Unit,
    onConfirmDeparture: (UUID) -> Unit,
    isEditable: Boolean = true
) {
    var isModalSheetOpen by remember { mutableStateOf(false) }
    var departureToEdit by remember { mutableStateOf<ActivityDepartureUpdateState?>(null) }
    var departureToConfirm by remember { mutableStateOf<UUID?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isModalSheetOpen) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { isModalSheetOpen = false }) {
            ActivityDepartureUpdateForm(
                initialDeparture = departureToEdit,
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

    departureToConfirm?.let { departureId ->
        AlertDialog(
            onDismissRequest = { departureToConfirm = null },
            title = { Text(stringResource(R.string.confirm_departure)) },
            text = { Text(stringResource(R.string.confirm_departure_body_actvity)) },
            confirmButton = {
                TextButton(onClick = { 
                    onConfirmDeparture(departureId)
                    departureToConfirm = null
                }) {
                    Text("Sì, conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = { departureToConfirm = null }) {
                    Text("Annulla")
                }
            }
        )
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
                departures.sortedBy { it.date }.forEach { departure ->
                    ActivityDepartureUpdateSummaryCard(
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
fun ActivityDepartureUpdateSummaryCard(
    departure: ActivityDepartureUpdateState,
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
                Column {
                    Text(
                        text = departure.date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${departure.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""} - ${departure.endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                when (departure.status) {
                    ActivityDepartureResponse.Status.CONFIRMED -> {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Confermata", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    ActivityDepartureResponse.Status.FULL -> {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Al completo", color = Color(0xFFF44336), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .background(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Pianificata", color = Color(0xFFFF9800), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Prezzo: ${departure.price} €", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Posti massimi: ${departure.maxSlots}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (departure.status != ActivityDepartureResponse.Status.CONFIRMED) {
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
fun ActivityDepartureUpdateForm(
    initialDeparture: ActivityDepartureUpdateState? = null,
    onSaveClick: (ActivityDepartureUpdateState) -> Unit,
    onCancelClick: () -> Unit
) {
    var date by remember(initialDeparture?.id) { mutableStateOf<LocalDate?>(initialDeparture?.date) }
    var startTime by remember(initialDeparture?.id) { mutableStateOf<LocalTime?>(initialDeparture?.startTime) }
    var endTime by remember(initialDeparture?.id) { mutableStateOf<LocalTime?>(initialDeparture?.endTime) }
    var priceText by remember(initialDeparture?.id) { mutableStateOf(initialDeparture?.price?.toString() ?: "") }
    var maxSlotsText by remember(initialDeparture?.id) { mutableStateOf(initialDeparture?.maxSlots?.toString() ?: "") }

    var dateError by remember { mutableStateOf<String?>(null) }
    var startTimeError by remember { mutableStateOf<String?>(null) }
    var endTimeError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var maxSlotsError by remember { mutableStateOf<String?>(null) }

    val errorDateStr = stringResource(R.string.error_start_date_invalid)
    val errorDatePastStr = stringResource(R.string.error_date_past)
    val errorTimeStr = stringResource(R.string.error_time_invalid)
    val errorTimeRangeStr = stringResource(R.string.error_time_range)
    val errorPriceStr = stringResource(R.string.error_price_invalid)
    val errorMaxSlotsStr = stringResource(R.string.error_max_slots_invalid)

    val fieldErrors = initialDeparture?.fieldErrors ?: emptyMap()
    if (fieldErrors.isNotEmpty()) {
        if (fieldErrors.containsKey("date")) dateError = stringResource(fieldErrors["date"]!!)
        if (fieldErrors.containsKey("startTime")) startTimeError = stringResource(fieldErrors["startTime"]!!)
        if (fieldErrors.containsKey("endTime")) endTimeError = stringResource(fieldErrors["endTime"]!!)
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
        
        UpdateDatePickerField(
            label = "Data dell'attività",
            selectedDate = date,
            onDateSelected = { date = it; dateError = null },
            isError = dateError != null,
            errorMessage = dateError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                UpdateTimePickerField(
                    label = "Ora di inizio",
                    selectedTime = startTime,
                    onTimeSelected = { startTime = it; startTimeError = null; endTimeError = null },
                    isError = startTimeError != null,
                    errorMessage = startTimeError
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                UpdateTimePickerField(
                    label = "Ora di fine",
                    selectedTime = endTime,
                    onTimeSelected = { endTime = it; endTimeError = null; startTimeError = null },
                    isError = endTimeError != null,
                    errorMessage = endTimeError
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
                
                if (date == null) {
                    dateError = errorDateStr
                    isValid = false
                } else if (!date!!.isAfter(LocalDate.now())) {
                    dateError = errorDatePastStr
                    isValid = false
                }

                if (startTime == null) {
                    startTimeError = errorTimeStr
                    isValid = false
                }
                if (endTime == null) {
                    endTimeError = errorTimeStr
                    isValid = false
                }

                if (startTime != null && endTime != null && !endTime!!.isAfter(startTime)) {
                    endTimeError = errorTimeRangeStr
                    isValid = false
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
                    val newDeparture = ActivityDepartureUpdateState(
                        id = initialDeparture?.id ?: UUID.randomUUID(),
                        isNew = initialDeparture?.isNew ?: true,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        price = parsedPrice,
                        maxSlots = parsedSlots,
                        status = initialDeparture?.status ?: ActivityDepartureResponse.Status.PLANNED,
                        fieldErrors = emptyMap()
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
fun UpdateTimePickerField(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: 12,
        initialMinute = selectedTime?.minute ?: 0
    )

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                onValueChange = {},
                label = { Text(label) },
                readOnly = true,
                isError = isError,
                trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
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
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showDialog = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}