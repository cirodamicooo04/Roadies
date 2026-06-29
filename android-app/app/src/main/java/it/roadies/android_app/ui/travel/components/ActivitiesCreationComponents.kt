package it.roadies.android_app.ui.travel.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import it.roadies.android_app.R
import it.roadies.android_app.client.models.travel.SearchSuggestion

@Composable
fun ActivityTitleDescriptionInput(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    nameError: String? = null,
    descriptionError: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.activity_name_label)) },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.activity_description_label)) },
            isError = descriptionError != null,
            supportingText = { descriptionError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityAddressPicker(
    addressQuery: String,
    onAddressQueryChange: (String) -> Unit,
    suggestions: List<SearchSuggestion>,
    onSuggestionSelected: (SearchSuggestion) -> Unit,
    addressError: String? = null
) {
    ExposedDropdownMenuBox(
        expanded = suggestions.isNotEmpty(),
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = addressQuery,
            onValueChange = onAddressQueryChange,
            label = { Text(stringResource(R.string.insert_address)) },
            isError = addressError != null,
            supportingText = { addressError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true,
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) }
        )

        DropdownMenu(
            expanded = suggestions.isNotEmpty(),
            onDismissRequest = { /* usually handled by parent clear logic */ },
            modifier = Modifier.fillMaxWidth(0.9f),
            properties = PopupProperties(focusable = false)
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text("${suggestion.name}, ${suggestion.country ?: ""}") },
                    onClick = { onSuggestionSelected(suggestion) }
                )
            }
        }
    }
}

