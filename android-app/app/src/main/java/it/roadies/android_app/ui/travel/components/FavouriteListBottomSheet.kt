package it.roadies.android_app.ui.travel.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.roadies.android_app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.roadies.android_app.client.models.travel.FavouriteListCreateRequest
import it.roadies.android_app.client.models.travel.FavouriteListResponse
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteListBottomSheet(
    isOpen: Boolean,
    lists: List<FavouriteListResponse>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onListSelected: (FavouriteListResponse) -> Unit,
    onCreateList: (name: String, visibility: FavouriteListCreateRequest.Visibility) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Stato per il form di creazione
    var showCreateForm by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var selectedVisibility by remember { mutableStateOf(FavouriteListCreateRequest.Visibility.PRIVATE) }
    var expandedDropdown by remember { mutableStateOf(false) }
    if (isOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showCreateForm = false
                newListName = ""
                onDismiss()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.fav_add_to_favorites),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (!showCreateForm) {
                    // Mostra le liste esistenti
                    if (lists.isEmpty()) {
                        // Non ci sono liste, mostra solo il pulsante crea
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.fav_no_lists_yet),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(lists) { list ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onListSelected(list) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = list.name ?: stringResource(R.string.fav_no_name),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    text = when (list.visibility) {
                                                        FavouriteListResponse.Visibility.PUBLIC -> stringResource(R.string.fav_visibility_public)
                                                        FavouriteListResponse.Visibility.PRIVATE -> stringResource(R.string.fav_visibility_private)
                                                        FavouriteListResponse.Visibility.SHARED_SPECIFIC -> stringResource(R.string.fav_visibility_shared)
                                                        else -> ""
                                                    },
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = stringResource(R.string.fav_elements_count, list.items?.size ?: 0),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Pulsante "Crea nuova lista"
                    OutlinedButton(
                        onClick = { showCreateForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.fav_create_new_list))
                    }
                } else {
                    // Form di creazione nuova lista
                    Text(
                        text = stringResource(R.string.fav_new_list),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        label = { Text(stringResource(R.string.fav_list_name_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Dropdown visibilità
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = when (selectedVisibility) {
                                FavouriteListCreateRequest.Visibility.PUBLIC -> stringResource(R.string.fav_visibility_public)
                                FavouriteListCreateRequest.Visibility.PRIVATE -> stringResource(R.string.fav_visibility_private)
                                FavouriteListCreateRequest.Visibility.SHARED_SPECIFIC -> stringResource(R.string.fav_visibility_shared_specific)
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.fav_visibility_label_only)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.fav_visibility_private)) },
                                onClick = {
                                    selectedVisibility = FavouriteListCreateRequest.Visibility.PRIVATE
                                    expandedDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.fav_visibility_public)) },
                                onClick = {
                                    selectedVisibility = FavouriteListCreateRequest.Visibility.PUBLIC
                                    expandedDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.fav_visibility_shared_specific)) },
                                onClick = {
                                    selectedVisibility = FavouriteListCreateRequest.Visibility.SHARED_SPECIFIC
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showCreateForm = false
                                newListName = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.fav_cancel_btn))
                        }
                        Button(
                            onClick = {
                                onCreateList(newListName.trim(), selectedVisibility)
                                showCreateForm = false
                                newListName = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = newListName.trim().isNotEmpty()
                        ) {
                            Text(stringResource(R.string.fav_create_btn))
                        }
                    }
                }
            }
        }
    }
}