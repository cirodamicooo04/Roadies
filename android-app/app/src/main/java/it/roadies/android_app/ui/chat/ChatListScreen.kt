package it.roadies.android_app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.roadies.android_app.client.models.chat.ConversationResponseDTO
import it.roadies.android_app.ui.components.UserAvatar
import it.roadies.android_app.viewmodel.chat.ChatListState
import it.roadies.android_app.viewmodel.chat.ChatListViewModel

@Composable
fun ChatListScreen(
    onNavigateToChat: (String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(state.conversations) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    myUserId = state.myUserId,
                    state = state,
                    onClick = { onNavigateToChat(conversation.id.toString()) }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationResponseDTO,
    myUserId: String,
    state: ChatListState,
    onClick: () -> Unit
) {
    val otherUserId = if (conversation.travelerId == myUserId) conversation.organizerId else conversation.travelerId
    val userInfo = otherUserId?.let { state.userInfos[it] }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(), 
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    username = userInfo?.username,
                    avatarUrl = userInfo?.avatarUrl,
                    modifier = Modifier.size(48.dp),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userInfo?.username ?: "Utente ${otherUserId?.take(6)}",
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold
                    )
                    Text(text = "Chat aperta", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary, 
                            RoundedCornerShape(percent = 50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}