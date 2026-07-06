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
import androidx.compose.ui.graphics.Color
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
                    Text(text = state.errorMessage!!, color = Color.Red)
                }
            }
            state.list != null -> {
                // INTESTAZIONE DELLA LISTA (Sostituisce la TopAppBar doppia)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Nome della lista in grande
                    Text(
                        text = state.list?.name ?: "Dettaglio Lista",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Icone per modificare/eliminare LA LISTA INTERA (solo per il proprietario)
                    if (state.isOwner) {
                        Row {
                            if (state.list?.visibility == FavouriteListResponse.Visibility.SHARED_SPECIFIC) {
                                IconButton(onClick = { 
                                    viewModel.loadFriends()
                                    showShareSheet = true 
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Condividi con amici", tint = Color(0xFFE26D38))
                                }
                            }
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifica lista", tint = Color.Gray)
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina lista", tint = Color.Red)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ELEMENTI DELLA LISTA
                val listItems = state.list!!.items ?: emptyList()

                if (listItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("La lista è vuota.", color = Color.Gray)
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
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = travel.title ?: "Viaggio",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text = travel.destination ?: "Destinazione ignota",
                                                color = Color.Gray
                                            )
                                        }

                                        // Icona per RIMUOVERE IL SINGOLO VIAGGIO
                                        if (state.isOwner) {
                                            IconButton(
                                                onClick = { travel.id?.let { viewModel.removeTravel(it) } }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Rimuovi elemento",
                                                    tint = Color.Red
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

    // Dialogo per ELIMINARE l'intera lista
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text("Eliminare la lista?") },
            text = { Text("Questa azione cancellerà la lista e tutti gli elementi al suo interno in modo irreversibile.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteList()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annulla") }
            }
        )
    }

    // Dialogo per MODIFICARE l'intera lista
    if (showEditDialog) {
        var editName by remember { mutableStateOf(state.list?.name ?: "") }
        var editVisibility by remember { mutableStateOf(state.list?.visibility ?: FavouriteListResponse.Visibility.PRIVATE) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modifica Lista") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome lista") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Visibilità:", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = editVisibility == FavouriteListResponse.Visibility.PRIVATE,
                            onClick = { editVisibility = FavouriteListResponse.Visibility.PRIVATE }
                        )
                        Text("Privata")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = editVisibility == FavouriteListResponse.Visibility.PUBLIC,
                            onClick = { editVisibility = FavouriteListResponse.Visibility.PUBLIC }
                        )
                        Text("Pubblica")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = editVisibility == FavouriteListResponse.Visibility.SHARED_SPECIFIC,
                            onClick = { editVisibility = FavouriteListResponse.Visibility.SHARED_SPECIFIC }
                        )
                        Text("Condivisa")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateList(editName, editVisibility)
                        showEditDialog = false
                    }
                ) { Text("Salva") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Annulla") }
            }
        )
    }

    // BottomSheet per CONDIVISIONE con amici
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
                    text = "Condividi con amici",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (state.isFriendsLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.friendsList.isEmpty()) {
                    Text("Nessun amico trovato.", color = Color.Gray)
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
                                    Text(text = friend.username ?: "Sconosciuto", fontWeight = FontWeight.Bold)
                                    Text(text = "${friend.firstName} ${friend.lastName}", fontSize = 14.sp, color = Color.Gray)
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