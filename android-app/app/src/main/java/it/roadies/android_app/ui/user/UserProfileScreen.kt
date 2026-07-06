package it.roadies.android_app.ui.user

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import it.roadies.android_app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import it.roadies.android_app.BadgeStatItem
import it.roadies.android_app.StatItem
import it.roadies.android_app.viewmodel.user.UserProfileViewModel

private val DarkBlueBg = Color(0xFF1B3B5A)
private val OrangeAvatarColor = Color(0xFFE26D38)
private val BackgroundGrayColor = Color(0xFFF5F5F5)
private val BorderGrayColor = Color(0xFFE0E0E0)

@Composable
fun UserProfileScreen(
    username: String,
    viewModel: UserProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToFavoriteLists: () -> Unit = {},
    onNavigateToOrganizedTrips: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(username) {
        viewModel.loadProfile(username)
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToChatEvent.collect { conversationId ->
            onNavigateToChat(conversationId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrayColor)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    color = OrangeAvatarColor,
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

            state.user != null -> {
                val user = state.user!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBlueBg)
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            it.roadies.android_app.ui.components.UserAvatar(
                                username = user.username,
                                avatarUrl = user.avatarUrl?.replace("localhost", "10.0.2.2"),
                                modifier = Modifier.size(100.dp),
                                fontSize = 36.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${user.firstName} ${user.lastName}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "@${user.username}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatItem(value = user.points.toString(), label = "Punti")

                            Divider(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(1.dp),
                                color = BorderGrayColor
                            )

                            BadgeStatItem(badgeName = user.badge?.toString() ?: "NONE")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToFavoriteLists() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAvatarColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.fav_lists), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        if (state.isOrganizer) {
                            Log.d("È UN ORGANIZZATORE", "organizer_check")
                            Button(
                                onClick = {
                                    state.user?.username?.let { username ->
                                        onNavigateToOrganizedTrips(username)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBlueBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.view_organized_travels), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = { viewModel.contactOrganizer() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlueBg)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.contact_organizer), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Log.d("NON E UN ORGANIZZATORE", "organizer_check")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}