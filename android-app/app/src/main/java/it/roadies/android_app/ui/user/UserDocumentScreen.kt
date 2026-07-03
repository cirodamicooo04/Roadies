package it.roadies.android_app.ui.user

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import it.roadies.android_app.client.models.user.UserDocumentRequestDTO
import it.roadies.android_app.viewmodel.user.UserDocumentsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDocumentScreen(
    viewModel: UserDocumentsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showUploadDialog by remember { mutableStateOf(false) }
    var viewingImageUrl by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let { errorMsg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorMsg)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("I miei documenti") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Documento")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.documents.isEmpty()) {
                Text(
                    "Nessun documento caricato.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.documents) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Info del documento
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tipo: ${doc.documentType?.value ?: "Sconosciuto"}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Numero: ${doc.documentNumber ?: "-"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Stato: ${doc.status?.value ?: "Sconosciuto"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when(doc.status?.value) {
                                            "VERIFIED" -> Color(0xFF4CAF50)
                                            "REJECTED" -> Color(0xFFF44336)
                                            else -> Color(0xFFFF9800)
                                        }
                                    )
                                }

                                // Pulsante per visualizzare il file
                                if (!doc.fileUrl.isNullOrBlank()) {
                                    IconButton(
                                        onClick = {

                                            val finalUrl = doc.fileUrl!!.replace("localhost", "10.0.2.2")
                                            try {
                                                val uri = Uri.parse(finalUrl)
                                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                }
                                                context.startActivity(intent)
                                            } catch (e: ActivityNotFoundException) {
                                                Toast.makeText(context, "Nessuna app trovata per aprire il link", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Errore generico in apertura", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Visualizza documento",
                                            tint = MaterialTheme.colorScheme.primary
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

    if (showUploadDialog) {
        UploadDocumentDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { type, number, uri ->
                viewModel.uploadDocument(type, number, uri)
                showUploadDialog = false
            },
            isUploading = state.isUploading
        )
    }

    // Mostra l'immagine in-app se ce n'è una selezionata
    viewingImageUrl?.let { url ->
        DocumentImageViewerDialog(
            imageUrl = url,
            onDismiss = { viewingImageUrl = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadDocumentDialog(
    onDismiss: () -> Unit,
    onUpload: (UserDocumentRequestDTO.DocumentType, String, Uri) -> Unit,
    isUploading: Boolean
) {
    var documentNumber by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(UserDocumentRequestDTO.DocumentType.ID_CARD) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Carica nuovo documento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = documentNumber,
                    onValueChange = { documentNumber = it },
                    label = { Text("Numero Documento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo di documento:", style = MaterialTheme.typography.bodyMedium)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedType == UserDocumentRequestDTO.DocumentType.ID_CARD,
                        onClick = { selectedType = UserDocumentRequestDTO.DocumentType.ID_CARD },
                        label = { Text("Carta d'Identità") }
                    )
                    FilterChip(
                        selected = selectedType == UserDocumentRequestDTO.DocumentType.PASSPORT,
                        onClick = { selectedType = UserDocumentRequestDTO.DocumentType.PASSPORT },
                        label = { Text("Passaporto") }
                    )
                    FilterChip(
                        selected = selectedType == UserDocumentRequestDTO.DocumentType.DRIVER_LICENSE,
                        onClick = { selectedType = UserDocumentRequestDTO.DocumentType.DRIVER_LICENSE },
                        label = { Text("Patente") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { filePickerLauncher.launch("*/*") }) {
                    Text(if (selectedUri == null) "Seleziona File" else "File selezionato: pronto")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedUri?.let { onUpload(selectedType, documentNumber, it) } },
                enabled = selectedUri != null && documentNumber.isNotBlank() && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Carica")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun DocumentImageViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Documento ingrandito",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center).size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    error = {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Impossibile caricare l'immagine", color = Color.White)
                        }
                    }
                )

                // Pulsante di chiusura (X) in alto a destra
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(32.dp)
                ) {
                    Text("X", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}