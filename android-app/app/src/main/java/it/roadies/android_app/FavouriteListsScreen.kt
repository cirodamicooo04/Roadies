package it.roadies.android_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import it.roadies.android_app.R
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.roadies.android_app.client.models.travel.FavouriteListResponse
import it.roadies.android_app.ui.theme.extendedColors
import it.roadies.android_app.viewmodel.FavouriteListsViewModel

@Composable
fun FavouriteListsScreen(
    onBack: () -> Unit,
    onListClick: (String) -> Unit,
    viewModel: FavouriteListsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (state.isMyProfile) stringResource(R.string.fav_my_lists) else stringResource(R.string.fav_lists),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            state.isLoading && state.cachedLists.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Se abbiamo la cache, la mostriamo SEMPRE (anche se c'è un errore network)
            state.lists.isNotEmpty() || state.cachedLists.isNotEmpty() -> {
                val displayLists = if (state.lists.isNotEmpty()) state.lists else state.cachedLists.map { cached ->
                    FavouriteListResponse(
                        id = cached.id,
                        ownerId = cached.ownerId,
                        name = cached.name,
                        visibility = FavouriteListResponse.Visibility.valueOf(cached.visibility)
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayLists) { list ->
                        ListCard(
                            list = list,
                            isMyProfile = state.isMyProfile,
                            onClick = { list.id?.let { onListClick(it.toString()) } }
                        )
                    }
                }
            }
            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.fav_no_lists), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


@Composable
fun ListCard(list: FavouriteListResponse, isMyProfile: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = list.name ?: stringResource(R.string.fav_no_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                val visibilityName = when (list.visibility?.name) {
                    "PRIVATE" -> stringResource(R.string.fav_visibility_private)
                    "PUBLIC" -> stringResource(R.string.fav_visibility_public)
                    "SHARED_SPECIFIC" -> stringResource(R.string.fav_visibility_shared)
                    else -> list.visibility?.name ?: "N/A"
                }
                
                val visibilityText = if (isMyProfile) stringResource(R.string.fav_visibility, visibilityName) else ""
                Text(
                    text = stringResource(R.string.fav_items_count, list.items?.size ?: 0) + visibilityText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}
