package it.roadies.android_app.ui.admin

import RejectRequestDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO

@Composable
fun RequestItem(
    request: PendingOrganizerRequestResponseDTO,
    onAccept: () -> Unit,
    onReject: (String) -> Unit, // This will trigger the rejection dialog
    onUserClick: () -> Unit
) {
    var showRejectDialog by remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onUserClick() } // Open user details on click
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display username from the request
            Text(text = "User: ${request.username}", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Reject button
                IconButton(onClick = {
                    showRejectDialog = true
                }) {
                    Text("❌", color = MaterialTheme.colorScheme.error)
                }
                // Accept button
                IconButton(onClick = onAccept) {
                    Text("✅")
                }
            }
            if (showRejectDialog){
                RejectRequestDialog(
                    onConfirm = {
                        showRejectDialog = false
                        onReject(it)
                    },
                    onDismiss = {
                        showRejectDialog = false
                    }
                )
            }
        }
    }
}