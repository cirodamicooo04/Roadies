package it.roadies.android_app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import it.roadies.android_app.R
import it.roadies.android_app.client.models.user.FriendshipResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.ui.theme.extendedColors
import it.roadies.android_app.viewmodel.user.FriendViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    viewModel: FriendViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isShowingSearchResults = state.searchQuery.isNotBlank()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var friendToRemove by remember { mutableStateOf<UserProfileResponseDTO?>(null) }

    if (friendToRemove != null) {
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = { Text(stringResource(R.string.remove_friend)) },
            text = {
                Text(stringResource(R.string.remove_friend_confirm, friendToRemove?.username ?: "utente"))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        friendToRemove?.username?.let { username ->
                            viewModel.removeFriend(username)
                        }
                        friendToRemove = null
                    }
                ) {
                    Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { friendToRemove = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PendingRequestsSheet(
                requests = state.pendingRequests,
                onAccept = { id -> viewModel.respondToRequest(id, accept = true) },
                onReject = { id -> viewModel.respondToRequest(id, accept = false) },
                onUserClick = { username ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showBottomSheet = false
                        navController.navigate("user_profile/$username")
                    }
                },
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showBottomSheet = false
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.extendedColors.neutralBackground)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_friend)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchUser() }),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.pendingRequests.isNotEmpty()) {
            Button(
                onClick = { showBottomSheet = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.pending_request) + "(${state.pendingRequests.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading || state.isSearching -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                isShowingSearchResults -> {
                    if (state.searchResults.isEmpty() && !state.isSearching) {
                        Text(
                            text = stringResource(R.string.no_user),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        UsersList(
                            title = stringResource(R.string.search_results),
                            users = state.searchResults,
                            isSearchMode = true,
                            friendsList = state.friends,
                            requestSentTo = state.requestSentTo,
                            pendingRequests = state.pendingRequests,
                            onAddFriendClick = { username ->
                                viewModel.sendFriendRequest(username)
                            },
                            onRemoveFriendClick = {},
                            onUserClick = { username ->
                                navController.navigate("user_profile/$username")
                            }
                        )
                    }
                }

                else -> {
                    if (state.friends.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_friends),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        UsersList(
                            title = stringResource(R.string.your_friends),
                            users = state.friends,
                            isSearchMode = false,
                            friendsList = state.friends,
                            requestSentTo = state.requestSentTo,
                            pendingRequests = state.pendingRequests,
                            onAddFriendClick = {},
                            onRemoveFriendClick = { user ->
                                friendToRemove = user
                            },
                            onUserClick = { username ->
                                navController.navigate("user_profile/$username")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingRequestsSheet(
    requests: List<FriendshipResponseDTO>,
    onAccept: (UUID) -> Unit,
    onReject: (UUID) -> Unit,
    onUserClick: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.pending_request),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(requests, key = { it.id.toString() }) { request ->
                PendingRequestCard(
                    request = request,
                    onAccept = { request.id?.let { onAccept(it) } },
                    onReject = { request.id?.let { onReject(it) } },
                    onUserClick = { onUserClick(request.friendProfile?.username ?: "") }
                )
            }
        }
    }
}

@Composable
fun PendingRequestCard(
    request: FriendshipResponseDTO,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onUserClick: () -> Unit = {}
) {
    val user = request.friendProfile

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            it.roadies.android_app.ui.components.UserAvatar(
                username = user?.username,
                avatarUrl = user?.avatarUrl,
                modifier = Modifier.size(52.dp),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user?.firstName} ${user?.lastName}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${user?.username ?: "utente"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            IconButton(
                onClick = onReject,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.reject),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onAccept,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.extendedColors.success)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.accept),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun UsersList(
    title: String,
    users: List<UserProfileResponseDTO>,
    isSearchMode: Boolean,
    friendsList: List<UserProfileResponseDTO>,
    requestSentTo: Set<String>,
    pendingRequests: List<FriendshipResponseDTO> = emptyList(),
    onAddFriendClick: (String) -> Unit,
    onRemoveFriendClick: (UserProfileResponseDTO) -> Unit,
    onUserClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(users) { user ->
            val isAlreadyFriend = friendsList.any { it.username == user.username }
            val requestAlreadySent = requestSentTo.contains(user.username)
            val hasPendingRequestFromThem = pendingRequests.any { it.friendProfile?.username == user.username }

            FriendCard(
                user = user,
                isSearchMode = isSearchMode,
                showAddButton = !isAlreadyFriend && !requestAlreadySent && !hasPendingRequestFromThem,
                requestAlreadySent = requestAlreadySent,
                hasPendingRequestFromThem = hasPendingRequestFromThem,
                onAddFriendClick = { onAddFriendClick(user.username ?: "") },
                onRemoveFriendClick = { onRemoveFriendClick(user) },
                onUserClick = { onUserClick(user.username ?: "") }
            )
        }
    }
}

@Composable
fun FriendCard(
    user: UserProfileResponseDTO,
    isSearchMode: Boolean = false,
    showAddButton: Boolean = true,
    requestAlreadySent: Boolean = false,
    hasPendingRequestFromThem: Boolean = false,
    onAddFriendClick: () -> Unit = {},
    onRemoveFriendClick: () -> Unit = {},
    onUserClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            it.roadies.android_app.ui.components.UserAvatar(
                username = user.username,
                avatarUrl = user.avatarUrl,
                modifier = Modifier.size(60.dp),
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${user.username ?: "utente"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            if (isSearchMode) {
                when {
                    hasPendingRequestFromThem -> {
                        Text(
                            text = stringResource(R.string.request_received),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    showAddButton -> {
                        IconButton(
                            onClick = onAddFriendClick,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = stringResource(R.string.add_friend),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    requestAlreadySent -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.request_sent),
                            tint = MaterialTheme.extendedColors.success,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(horizontal = 12.dp)
                        )
                    }
                }
            } else {
                val badgeName = user.badge?.toString() ?: "NONE"
                val imageRes = when (badgeName.uppercase()) {
                    "BRONZE" -> R.drawable.badge_bronze
                    "SILVER" -> R.drawable.badge_silver
                    "GOLD" -> R.drawable.badge_gold
                    "PLATINUM" -> R.drawable.badge_platinum
                    "DIAMOND" -> R.drawable.badge_diamond
                    "EMERALD" -> R.drawable.badge_emerald
                    else -> R.drawable.badge_bronze
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Badge $badgeName",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        tint = Color.Unspecified
                    )

                    IconButton(
                        onClick = onRemoveFriendClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.remove_friend),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}