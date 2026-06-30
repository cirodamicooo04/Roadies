package it.roadies.android_app.ui.bookingFlow

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.roadies.android_app.client.models.booking.MemberDocumentRequest
import it.roadies.android_app.ui.bookingFlow.components.BookingErrorText
import it.roadies.android_app.ui.bookingFlow.components.BookingExpiredDialog
import it.roadies.android_app.ui.bookingFlow.components.BookingStepBottomBar
import it.roadies.android_app.ui.bookingFlow.components.BookingStepHeader
import it.roadies.android_app.ui.bookingFlow.components.BookingStepProgressBar
import it.roadies.android_app.ui.bookingFlow.components.BookingTimerBar
import it.roadies.android_app.ui.bookingFlow.components.LoadingOverlay
import it.roadies.android_app.viewmodel.bookingFlow.BookingFlowViewModel
import it.roadies.android_app.viewmodel.bookingFlow.DocumentUploadItem
import java.util.UUID
import it.roadies.android_app.R

@Composable
fun BookingDocumentsScreen(flowViewModel: BookingFlowViewModel, onCompleted: () -> Unit, onExpired: () -> Unit) {
    val state by flowViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.completed) {
        if (state.completed) {
            onCompleted()
            flowViewModel.onCompletedNavigated()
        }
    }

    // tiene traccia di quale documento sta scegliendo la foto
    var pendingDocumentId by remember { mutableStateOf<UUID?>(null) }
    val photoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        uri -> val docId = pendingDocumentId
        if (uri != null && docId != null) {
            flowViewModel.onPhotoPicked(docId, uri.toString())
        }
        pendingDocumentId = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            BookingStepProgressBar(stepLabel = stringResource(R.string.booking_step_3_of_4), progress = 0.75f)

            Spacer(modifier = Modifier.height(16.dp))
            BookingTimerBar(remainingSeconds = state.remainingSeconds)

            Spacer(modifier = Modifier.height(24.dp))
            BookingStepHeader(
                title = stringResource(R.string.upload_documents),
                subtitle = stringResource(R.string.add_ph)
            )

            Spacer(modifier = Modifier.height(24.dp))

            state.items.forEach { item ->
                DocumentUploadCard(
                    item = item,
                    onPickPhoto = {
                        pendingDocumentId = item.documentId
                        photoPicker.launch("image/*")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            BookingErrorText(
                error = state.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            val allPicked = state.items.isNotEmpty() && state.items.all { it.localUri != null || it.uploaded }
            BookingStepBottomBar(
                primaryLabel = stringResource(R.string.upload_and_complete),
                onPrimary = { flowViewModel.uploadAll() },
                primaryEnabled = allPicked && !state.isUploading
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (state.isUploading) {
            LoadingOverlay(message = stringResource(R.string.uploading_documents))
        }

        if (state.isExpired) {
            BookingExpiredDialog(onConfirm = onExpired)
        }
    }
}

@Composable
private fun DocumentUploadCard(
    item: DocumentUploadItem,
    onPickPhoto: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.memberName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.documentType.documentLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                if (item.uploaded) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.uploaded),
                        tint = colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item.localUri != null) {
                AsyncImage(
                    model = item.localUri,
                    contentDescription = stringResource(R.string.document_preview),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = onPickPhoto,
                enabled = !item.uploaded,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (item.localUri == null) stringResource(R.string.choose_photo) else stringResource(R.string.change_photo))
            }
        }
    }
}

@Composable
private fun MemberDocumentRequest.Type.documentLabel(): String = when (this) {
    MemberDocumentRequest.Type.ID_CARD -> stringResource(R.string.id_card)
    MemberDocumentRequest.Type.DRIVER_LICENSE ->  stringResource(R.string.dr_license)
    MemberDocumentRequest.Type.PASSPORT ->  stringResource(R.string.passport)
}
