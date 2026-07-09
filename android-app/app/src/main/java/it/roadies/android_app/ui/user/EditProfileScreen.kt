package it.roadies.android_app.ui.user

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.roadies.android_app.R
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.roadies.android_app.ui.theme.extendedColors
import it.roadies.android_app.viewmodel.user.EditProfileViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onAvatarPicked(uri)
    }

    val initialSelectedDateMillis = remember(state.birthDate) {
        state.birthDate
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis?.let { millis ->
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        viewModel.onBirthDateChange(selectedDate)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        state.successMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.extendedColors.successText
            )
        }

        OutlinedTextField(
            value = state.firstName,
            onValueChange = viewModel::onFirstNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.first_name)) },
            singleLine = true
        )

        OutlinedTextField(
            value = state.lastName,
            onValueChange = viewModel::onLastNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.last_name)) },
            singleLine = true
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (state.avatarUrl.isBlank()) {
                    stringResource(R.string.no_image_selected)
                } else {
                    state.avatarUrl
                },
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.avatar)) },
                readOnly = true,
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { photoPicker.launch("image/*") }
            )
        }

        Button(
            onClick = { photoPicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_avatar))
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.birthDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).orEmpty(),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.date_of_birth)) },
                readOnly = true,
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDatePicker = true }
            )
        }

        Button(
            onClick = viewModel::saveProfile,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.save_changes))
            }
        }
    }
}