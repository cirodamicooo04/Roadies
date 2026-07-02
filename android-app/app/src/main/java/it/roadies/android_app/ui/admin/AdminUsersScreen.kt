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

    var selectedFilter by remember { mutableStateOf("ACTIVE") }
    var expanded by remember { mutableStateOf(false) }

    var selectedUser by remember { mutableStateOf<UserResponseDTO?>(null) }
    val filters = listOf("ACTIVE", "BANNED", "ORGANIZER")

    LaunchedEffect(selectedFilter) {
        viewModel.fetchUsers(selectedFilter)
    }

    Column(modifier = Modifier.padding(16.dp)) {
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