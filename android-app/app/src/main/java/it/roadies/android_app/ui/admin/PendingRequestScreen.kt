package it.roadies.android_app.ui.admin


import UserDetailDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.viewmodel.AdminViewModel

@Composable
fun PendingRequestsScreen(
    viewModel: AdminViewModel
) {
    val requests by viewModel.pendingRequests.collectAsState()

    var selectedUserRequest by remember {
        mutableStateOf<PendingOrganizerRequestResponseDTO?>(null)
    }

    LaunchedEffect(requests) {
        viewModel.loadPendingRequests()
    }

    Column {
        if (requests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No pending requests at the moment",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn {
                items(requests) { request ->
                    RequestItem(
                        request = request,
                        onAccept = {
                            request.keycloakId?.let {
                                viewModel.reviewOrganizerRequest(
                                    it,
                                    true,
                                    null
                                )
                            }
                        },
                        onReject = { reason ->
                            request.keycloakId?.let {
                                viewModel.reviewOrganizerRequest(
                                    it,
                                    false,
                                    reason
                                )
                            }
                        },
                        onUserClick = { selectedUserRequest = request }
                    )
                }

            }
            selectedUserRequest?.let {
                UserDetailDialog(
                    user = UserDetailsUi(
                        id = it.keycloakId,
                        username = it.username,
                        firstname = it.firstName,
                        lastname = it.lastName,
                        email = it.email
                    ),
                    onDismiss = {
                        selectedUserRequest = null
                    }
                )
            }
        }
    }
}