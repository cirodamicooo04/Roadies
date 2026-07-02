package it.roadies.android_app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import it.roadies.android_app.ui.travel.components.ActivityAddressPicker
import it.roadies.android_app.ui.travel.components.ActivityTitleDescriptionInput
import it.roadies.android_app.ui.travel.components.ImageCarousel
import it.roadies.android_app.ui.travel.components.BoxCentered
import it.roadies.android_app.viewmodel.ActivityCreationDepartureState
import it.roadies.android_app.viewmodel.ActivityCreationStep
import it.roadies.android_app.viewmodel.ActivityCreationUiState
import it.roadies.android_app.viewmodel.ActivityCreationViewModel

@Composable
fun ActivityCreationScreen(
    navHostController: NavHostController, 
    viewModel: ActivityCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showModalOfCancelCreation by remember { mutableStateOf(false) }
    var showModalOfCreation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isCreationSuccess) {
        if (uiState.isCreationSuccess) {
            navHostController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("activity_created", true)
            navHostController.popBackStack()
        }
    }

    BackHandler {
        if (uiState.currentStep == ActivityCreationStep.BASIC_INFO) {
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
                    label = "WizardTransitionActivity"
                ) { step ->
                    when (step) {
                        ActivityCreationStep.BASIC_INFO -> {
                            ActivityBasicInfoForm(state = uiState, viewModel = viewModel)
                        }
                        ActivityCreationStep.DEPARTURES -> {
                            ActivityDeparturesSummaryForm(
                                departures = uiState.departures,
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
                                if (uiState.currentStep == ActivityCreationStep.BASIC_INFO) {
                                    showModalOfCancelCreation = true
                                } else {
                                    viewModel.previousStep()
                                }
                            },
                            enabled = !uiState.images.any { it.isUploading } && !uiState.isCreationLoading
                        ) {
                            Text(
                                if (uiState.currentStep == ActivityCreationStep.BASIC_INFO) stringResource(
                                    R.string.cancel
                                ) else stringResource(R.string.back)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (uiState.currentStep == ActivityCreationStep.DEPARTURES) {
                                    showModalOfCreation = true
                                } else {
                                    viewModel.nextStep()
                                }
                            },
                            enabled = !uiState.images.any { it.isUploading } && !uiState.isCreationLoading
                        ) {
                            if (uiState.isCreationLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    if (uiState.currentStep == ActivityCreationStep.DEPARTURES) stringResource(
                                        R.string.create_activity_btn
                                    ) else stringResource(R.string.next)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showModalOfCancelCreation) {
        AlertDialog(
            onDismissRequest = { showModalOfCancelCreation = false },
            title = { Text(text = stringResource(R.string.cancel_activity_creation_confirm)) },
            text = { Text(text = stringResource(R.string.cancel_activity_creation_confirm_body)) },
            confirmButton = {
                Button(onClick = {
                    navHostController.popBackStack()
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
            title = { Text(text = stringResource(R.string.create_activity_confirm)) },
            text = { Text(text = stringResource(R.string.create_activity_confirm_body)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.nextStep() // Scatenerà poi la vera creazione
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

@Composable
fun ActivityBasicInfoForm(
    state: ActivityCreationUiState,
    viewModel: ActivityCreationViewModel
) {
    val suggestions by viewModel.addressSuggestions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.create_new_activity),
            fontWeight = FontWeight.SemiBold,
            fontSize = 35.sp
        )

        ImageCarousel(
            images = state.images,
            onImagesSelected = { uris -> viewModel.uploadImagesFromGallery(uris) },
            onRemoveImage = { uri -> viewModel.removeImage(uri) }
        )

        ActivityTitleDescriptionInput(
            name = state.name,
            onNameChange = { viewModel.updateName(it) },
            description = state.description,
            onDescriptionChange = { viewModel.updateDescription(it) },
            nameError = state.fieldErrors["name"]?.let { stringResource(id = it) },
            descriptionError = state.fieldErrors["description"]?.let { stringResource(id = it) }
        )

        ActivityAddressPicker(
            addressQuery = state.addressSearchQuery,
            onAddressQueryChange = { 
                viewModel.searchAddresses(it) 
            },
            suggestions = suggestions,
            onSuggestionSelected = { suggestion ->
                viewModel.selectAddressSuggestion(suggestion)
            },
            addressError = state.fieldErrors["address"]?.let { stringResource(id = it) }
        )
    }
}

@Composable
fun ActivityDeparturesSummaryForm(
    departures: List<ActivityCreationDepartureState>,
    onSaveDeparture: (ActivityCreationDepartureState) -> Unit,
    onDeleteDeparture: (UUID) -> Unit
) {
    var isModalSheetOpen by remember { mutableStateOf(false) }
    var departureToEdit by remember { mutableStateOf<ActivityCreationDepartureState?>(null) }

    if (isModalSheetOpen) {
        ActivityDepartureEditModal(
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
                departures.sortedBy { it.date }.forEach { departure ->
                    ActivityDepartureSummaryCard(
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
fun ActivityDepartureSummaryCard(
    departure: ActivityCreationDepartureState,
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
                        text = "${departure.date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} (${departure.startTime?.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${departure.endTime?.format(DateTimeFormatter.ofPattern("HH:mm"))})",
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
fun ActivityDepartureEditModal(
    initialDeparture: ActivityCreationDepartureState? = null,
    onSaveClick: (ActivityCreationDepartureState) -> Unit,
    onCancelClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(sheetState = sheetState, onDismissRequest = onCancelClick) {
        ActivityDepartureCreateForm(
            initialDeparture = initialDeparture,
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDepartureCreateForm(
    initialDeparture: ActivityCreationDepartureState? = null,
    onSaveClick: (ActivityCreationDepartureState) -> Unit,
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
    val errorTimeStr = stringResource(R.string.error_time_invalid)
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
        
        DatePickerField(
            label = stringResource(R.string.activity_date_label),
            selectedDate = date,
            onDateSelected = { date = it; dateError = null },
            isError = dateError != null,
            errorMessage = dateError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                TimePickerField(
                    label = stringResource(R.string.start_time_label),
                    selectedTime = startTime,
                    onTimeSelected = { startTime = it; startTimeError = null; endTimeError = null },
                    isError = startTimeError != null,
                    errorMessage = startTimeError
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                TimePickerField(
                    label = stringResource(R.string.end_time_label),
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
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        priceText = it
                        priceError = null
                    }
                },
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
                } else if (date!!.isBefore(LocalDate.now())) {
                    dateError = errorDateStr
                    isValid = false
                }
                
                if (startTime == null) {
                    startTimeError = errorTimeStr
                    isValid = false
                }
                
                if (endTime == null || (startTime != null && !endTime!!.isAfter(startTime))) {
                    endTimeError = errorTimeStr
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
                    val newDeparture = ActivityCreationDepartureState(
                        id = initialDeparture?.id ?: UUID.randomUUID(),
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
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
fun TimePickerField(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: 9,
        initialMinute = selectedTime?.minute ?: 0,
        is24Hour = true
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
            title = { Text(stringResource(R.string.select_time_title)) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(time)
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
        )
    }
}

