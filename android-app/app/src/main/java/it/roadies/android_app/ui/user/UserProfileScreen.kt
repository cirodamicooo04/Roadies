package it.roadies.android_app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
private val TextDarkColor = Color(0xFF1A2B4C)
private val BorderGrayColor = Color(0xFFE0E0E0)

@Composable
fun UserProfileScreen(
    username: String,
    viewModel: UserProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(username) {
        viewModel.loadProfile(username)
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
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(OrangeAvatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarUrl = user.avatarUrl
                                    ?.replace("localhost", "10.0.2.2")
                                    ?.takeIf { it.isNotBlank() }
                                    ?: ""

                                if (avatarUrl.isNotBlank()) {
                                    SubcomposeAsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar di ${user.username}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    val initialNome = user.firstName?.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: ""
                                    val initialCognome = user.lastName?.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: ""
                                    val initials = if (initialNome.isBlank() && initialCognome.isBlank()) "?" else "$initialNome$initialCognome"

                                    Text(
                                        text = initials,
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

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

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}