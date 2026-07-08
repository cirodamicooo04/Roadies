package it.roadies.android_app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.roadies.android_app.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.roadies.android_app.client.models.chat.MessageResponseDTO
import it.roadies.android_app.ui.theme.extendedColors
import it.roadies.android_app.viewmodel.chat.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    organizerName: String = "Organizzatore",
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }

    // Rilancia l'inizializzazione se cambia la conversazione
    LaunchedEffect(conversationId) {
        viewModel.initChat(conversationId)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        it.roadies.android_app.ui.components.UserAvatar(
                            username = state.otherUserName ?: organizerName,
                            avatarUrl = state.otherUserAvatarUrl,
                            modifier = Modifier.size(36.dp),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(state.otherUserName ?: organizerName, color = MaterialTheme.extendedColors.onHeader)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.extendedColors.header)
            )
        },
        bottomBar = {
            ChatInputBar(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                },
                isSending = state.isSending
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.neutralBackground)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.tertiary
                )
            } else if (state.error != null && state.messages.isEmpty()) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages) { message ->
                        val isMe = message.senderId == state.currentUserId
                        ChatBubble(message = message, isMe = isMe)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageResponseDTO, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isMe) MaterialTheme.extendedColors.chatBubbleMine else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val time = message.timestamp.substringAfter("T").take(5)
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.type_message)) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.extendedColors.header,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            maxLines = 4
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank() && !isSending,
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (text.isNotBlank()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(24.dp)
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.send),
                tint = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}