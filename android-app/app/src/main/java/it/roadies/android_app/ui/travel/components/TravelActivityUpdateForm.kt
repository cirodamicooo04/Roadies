package it.roadies.android_app.ui.travel.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.roadies.android_app.R
import it.roadies.android_app.client.models.travel.SearchSuggestion
import it.roadies.android_app.client.models.travel.TravelUpdateRequest
import it.roadies.android_app.viewmodel.TravelActivityUpdateState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelActivityUpdateForm(
    initialActivity: TravelActivityUpdateState,
    suggestions: List<SearchSuggestion>,
    onSearchAddress: (String) -> Unit,
    onClearSuggestions: () -> Unit,
    alreadySelectedDays: List<Int>,
    onSaveClick: (TravelActivityUpdateState) -> Unit,
    onCancelClick: () -> Unit,
    travelDurationDays: Int,
    travelContinent: TravelUpdateRequest.Continent,
    travelCountry: String,
    travelDestination: String
) {
    var name by remember(initialActivity.id) { mutableStateOf(initialActivity.name) }
    var description by remember(initialActivity.id) { mutableStateOf(initialActivity.description) }
    var dayNumber by remember(initialActivity.id) { mutableIntStateOf(initialActivity.dayNumber) }
    var addressQuery by remember(initialActivity.id) { mutableStateOf(initialActivity.address) }
    var selectedLatitude by remember(initialActivity.id) { mutableStateOf<Double?>(initialActivity.latitude) }
    var selectedLongitude by remember(initialActivity.id) { mutableStateOf<Double?>(initialActivity.longitude) }
    var images by remember(initialActivity.id) { mutableStateOf(initialActivity.images) }

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
            text = if (initialActivity.isNew) stringResource(R.string.create_new_activity) else stringResource(R.string.edit_activity),
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
                    TrailingIcon(expanded = expandedDayDropdown)
                }
            )
            DropdownMenu(
                expanded = expandedDayDropdown,
                onDismissRequest = { expandedDayDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                for (i in 1..travelDurationDays) {
                    if (i !in alreadySelectedDays) {
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
                        val updatedActivity = initialActivity.copy(
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
                        onSaveClick(updatedActivity)
                    }
                },
                enabled = dayNumber > 0
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
