package it.roadies.android_app

import android.R.attr.onClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import it.roadies.android_app.viewmodel.user.ProfileViewModel
import it.roadies.android_app.R
import it.roadies.android_app.viewmodel.AuthViewModel

val DarkBlueBg = Color(0xFF1B3B5A)
val OrangeAvatar = Color(0xFFE26D38)
val BackgroundGray = Color(0xFFF5F5F5)
val TextDark = Color(0xFF1A2B4C)
val BorderGray = Color(0xFFE0E0E0)

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(),
                  authViewModel: AuthViewModel = hiltViewModel(),
                  onNavigateTo:(String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.profile != null) {
            val user = state.profile!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(OrangeAvatar),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = user.avatarUrl
                        ?.replace("localhost", "10.0.2.2")
                        ?.takeIf { it.isNotBlank() }
                        ?: ""

                    if (avatarUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
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
                    text = "@"+ user.username,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

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
                            color = BorderGray
                        )

                        BadgeStatItem(badgeName = user.badge?.toString() ?: "NONE")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    MenuItem(text = stringResource(R.string.edit_profile),
                        onClick = {onNavigateTo("edit_profile")})

                    MenuItem(text = stringResource(R.string.friends),
                        onClick = {onNavigateTo("friend")})

                    MenuItem(text = stringResource(R.string.logout),
                        textColor = Color(0xFFD32F2F),
                        onClick = {
                            authViewModel.logout()

                            onNavigateTo("home")
                        })

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun BadgeStatItem(badgeName: String) {
    val imageRes = when (badgeName.uppercase()) {
        "BRONZE" -> R.drawable.badge_bronze
        "SILVER" -> R.drawable.badge_silver
        "GOLD" -> R.drawable.badge_gold
        "PLATINUM" -> R.drawable.badge_platinum
        "DIAMOND" -> R.drawable.badge_diamond
        "EMERALD" -> R.drawable.badge_emerald
        else -> R.drawable.badge_bronze
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Badge $badgeName",
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
    )
}

@Composable
fun MenuItem(text: String, textColor: Color = TextDark, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        )
    }
}