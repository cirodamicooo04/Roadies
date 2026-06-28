package it.roadies.android_app.ui.admin

import UserItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import it.roadies.android_app.viewmodel.AdminViewModel

@Composable
fun AdminUsersScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers("ACTIVE")
    }

    LazyColumn {
        items(users) { user ->
            UserItem(
                user = user,
                onBlockClick = { id -> viewModel.blockUser(id) },
                onUnblockClick = { id -> viewModel.unblockUser(id) }
            )
        }
    }
}