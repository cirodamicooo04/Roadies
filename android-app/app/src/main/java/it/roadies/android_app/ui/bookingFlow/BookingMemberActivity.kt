package it.roadies.android_app.ui.bookingFlow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.roadies.android_app.client.models.booking.MemberDocumentRequest
import it.roadies.android_app.ui.bookingFlow.components.BookingErrorText
import it.roadies.android_app.ui.bookingFlow.components.BookingExpiredDialog
import it.roadies.android_app.ui.bookingFlow.components.BookingStepBottomBar
import it.roadies.android_app.ui.bookingFlow.components.BookingStepHeader
import it.roadies.android_app.ui.bookingFlow.components.BookingStepProgressBar
import it.roadies.android_app.ui.bookingFlow.components.BookingTimerBar
import it.roadies.android_app.ui.bookingFlow.components.LoadingOverlay
import it.roadies.android_app.viewmodel.bookingFlow.BookingFlowViewModel
import it.roadies.android_app.viewmodel.bookingFlow.BookingMembersViewModel
import it.roadies.android_app.viewmodel.bookingFlow.DocumentUploadItem
import it.roadies.android_app.viewmodel.bookingFlow.MemberForm
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import it.roadies.android_app.R

@Composable
fun BookingStepMembersScreen(onBack: () -> Unit, onMembersInserted: (List<DocumentUploadItem>) -> Unit, onExpired: () -> Unit, flowViewModel: BookingFlowViewModel, viewModel: BookingMembersViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val flowState by flowViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { flowViewModel.startTimer() }

    LaunchedEffect(state.uploadItems) {
        state.uploadItems?.let {
            onMembersInserted(it)
            viewModel.onMembersNavigated()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            BookingStepProgressBar(stepLabel = stringResource(R.string.booking_step_2_of_4), progress = 0.5f)

            Spacer(modifier = Modifier.height(16.dp))
            BookingTimerBar(remainingSeconds = flowState.remainingSeconds)

            Spacer(modifier = Modifier.height(24.dp))
            BookingStepHeader(
                title = stringResource(R.string.participant_details),
                subtitle = stringResource(R.string.insert_participant_details),
            )

            Spacer(modifier = Modifier.height(24.dp))

            state.members.forEachIndexed { index, member ->
                MemberFormCard(
                    index = index,
                    member = member,
                    enabled = !state.isLoading,
                    onChange = { transform -> viewModel.updateMember(index, transform) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            BookingErrorText(
                error = if (state.error == "invalid_fields") stringResource(R.string.booking_members_invalid_fields) else state.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            BookingStepBottomBar(
                primaryLabel = stringResource(R.string.booking_continue),
                onPrimary = { viewModel.submit() },
                primaryEnabled = !state.isLoading,
                secondaryLabel =  stringResource(R.string.booking_back),
                onSecondary = onBack,
                secondaryEnabled = !state.isLoading
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (state.isLoading) {
            LoadingOverlay(message = stringResource(R.string.saving_partecipants),)
        }

        if (flowState.isExpired) {
            BookingExpiredDialog(onConfirm = onExpired)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberFormCard(
    index: Int,
    member: MemberForm,
    enabled: Boolean,
    onChange: ((MemberForm) -> MemberForm) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.partecipants, index + 1),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = member.firstName,
                onValueChange = { value -> onChange { it.copy(firstName = value) } },
                label = { Text(stringResource(R.string.first_name)) },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = member.lastName,
                onValueChange = { value -> onChange { it.copy(lastName = value) } },
                label = { Text(stringResource(R.string.last_name)) },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = member.phoneNumber,
                onValueChange = { value -> onChange { it.copy(phoneNumber = value) } },
                label = { Text(stringResource(R.string.tel)) },
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // data di nascita: campo di sola lettura che apre il date picker
            OutlinedTextField(
                value = member.birthDate.format(dateFormatter),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(stringResource(R.string.date)) },
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = colorScheme.onSurface,
                    disabledBorderColor = colorScheme.outline,
                    disabledLabelColor = colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (enabled) Modifier.clickableNoRipple { showDatePicker = true } else Modifier
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))

            DocumentTypeDropdown(
                selected = member.documentType,
                enabled = enabled,
                onSelected = { type -> onChange { it.copy(documentType = type) } }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = member.birthDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onChange { it.copy(birthDate = date) }
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.booking_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.booking_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentTypeDropdown(
    selected: MemberDocumentRequest.Type,
    enabled: Boolean,
    onSelected: (MemberDocumentRequest.Type) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selected.documentLabel(),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.type_documents)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MemberDocumentRequest.Type.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.documentLabel()) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MemberDocumentRequest.Type.documentLabel(): String = when (this) {
    MemberDocumentRequest.Type.ID_CARD -> stringResource(R.string.id_card)
    MemberDocumentRequest.Type.DRIVER_LICENSE -> stringResource(R.string.dr_license)
    MemberDocumentRequest.Type.PASSPORT -> stringResource(R.string.passport)
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.composed {
        clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    }
