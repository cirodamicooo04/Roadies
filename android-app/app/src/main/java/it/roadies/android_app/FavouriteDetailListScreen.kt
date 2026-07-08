package it.roadies.android_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.roadies.android_app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.viewmodel.FavouriteListDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteListDetailScreen(
    onBack: () -> Unit,
    onNavigateToTravel: (String) -> Unit,
    viewModel: FavouriteListDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            state.isLoading && state.list == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            state.list != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.list?.name ?: stringResource(R.string.fav_detail_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (state.isOwner) {
                        Row {
                            if (state.list?.visibility == FavouriteListResponse.Visibility.SHARED_SPECIFIC) {
                                IconButton(onClick = {
                                    viewModel.loadFriends()
                                    showShareSheet = true
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.fav_share_with_friends), tint = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.fav_edit_list), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.fav_delete_list), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val listItems = state.list!!.items ?: emptyList()

                if (listItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.fav_list_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listItems) { item ->
                            item.travel?.let { travel ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToTravel(travel.id.toString()) },
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = travel.title ?: stringResource(R.string.fav_travel_placeholder),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text = travel.destination ?: stringResource(R.string.fav_unknown_destination),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Icona per rimuovere singolo viaggio
                                        if (state.isOwner) {
                                            IconButton(
                                                onClick = { travel.id?.let { viewModel.removeTravel(it) } }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.fav_remove_item),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogo per eliminare l'intera lista
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.fav_delete_confirm_title)) },
            text = { Text(stringResource(R.string.fav_delete_confirm_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteList()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.fav_delete_btn)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.fav_cancel_btn)) }
            }
        )
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(state.list?.name ?: "") }
        var editVisibility by remember { mutableStateOf(state.list?.visibility ?: FavouriteListResponse.Visibility.PRIVATE) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.fav_edit_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.fav_name_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.fav_visibility_label), fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = editVisibility == FavouriteListResponse.Visibility.PRIVATE,
                            onClick = { editVisibility = FavouriteListResponse.Visibility.PRIVATE }
                        )
                        Text(stringResource(R.string.fav_visibility_private))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = editVisibility == FavouriteListResponse.Visibility.PUBLIC,
                            onClick = { editVisibility = FavouriteListResponse.Visibility.PUBLIC }
                        )
                        Text(stringResource(R.string.fav_visibility_public))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = editVisibility == FavouriteListResponse.Visibility.SHARED_SPECIFIC,
                            onClick = { editVisibility = FavouriteListResponse.Visibility.SHARED_SPECIFIC }
                        )
                        Text(stringResource(R.string.fav_visibility_shared))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateList(editName, editVisibility)
                        showEditDialog = false
                    }
                ) { Text(stringResource(R.string.fav_save_btn)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(stringResource(R.string.fav_cancel_btn)) }
            }
        )
    }

    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.fav_share_with_friends),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (state.isFriendsLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.friendsList.isEmpty()) {
                    Text(stringResource(R.string.fav_no_friends_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.friendsList) { friend ->
                            val isShared = friend.username?.let { state.sharedWithUsernames.contains(it) } == true
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = friend.username ?: stringResource(R.string.fav_unknown_friend), fontWeight = FontWeight.Bold)
                                    Text(text = "${friend.firstName} ${friend.lastName}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isShared,
                                    onCheckedChange = { checked ->
                                        friend.username?.let { viewModel.toggleFriendShare(it, checked) }
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}