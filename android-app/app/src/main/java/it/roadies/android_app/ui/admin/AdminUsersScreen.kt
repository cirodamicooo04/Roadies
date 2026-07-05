package it.roadies.android_app.ui.admin

import UserDetailDialog
import UserItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.roadies.android_app.client.models.user.UserResponseDTO
import it.roadies.android_app.viewmodel.AdminViewModel

@Composable
fun AdminUsersScreen(viewModel: AdminViewModel, navController: NavHostController) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedFilter by remember { mutableStateOf("ACTIVE") }
    var expanded by remember { mutableStateOf(false) }

    var selectedUser by remember { mutableStateOf<UserResponseDTO?>(null) }
    val filters = listOf("ACTIVE", "BANNED", "ORGANIZER")

    LaunchedEffect(selectedFilter) {
        viewModel.fetchUsers(selectedFilter)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            // Filter dropdown and navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(text = "Filter: $selectedFilter")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        filters.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter = filter
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        navController.navigate("pending_requests")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Organizer Requests")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check if user list is empty to display the contextual placeholder
            if (users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Capitalize first letter and make others lower case for better visual output (e.g., Active, Banned, Organizer)
                    val formattedFilter = selectedFilter.lowercase().replaceFirstChar { it.uppercase() }
                    Text(
                        text = "There are no $formattedFilter users",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Display the list of filtered users
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(users) { user ->
                        Box(modifier = Modifier.clickable { selectedUser = user }) {
                            UserItem(
                                user = user,
                                onBlockClick = { viewModel.blockUser(it) },
                                onUnblockClick = { viewModel.unblockUser(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Overlay dialog for user detailed views
    selectedUser?.let { user ->
        UserDetailDialog(
            user = UserDetailsUi(
                id = user.keycloakId,
                username = user.username,
                firstname = user.firstName,
                lastname = user.lastName,
                email = user.email
            ),
            onDismiss = {
                selectedUser = null
            }
        )
    }
}