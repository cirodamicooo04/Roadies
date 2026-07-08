package it.roadies.android_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun UserAvatar(
    username: String?,
    avatarUrl: String?,
    modifier: Modifier = Modifier.size(48.dp),
    fontSize: TextUnit = 18.sp
) {
    val initial = username?.trim()?.take(1)?.uppercase() ?: "?"

    val fallbackContent: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (!avatarUrl.isNullOrBlank() && avatarUrl != "null") {
        coil3.compose.SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            modifier = modifier.clip(CircleShape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            loading = { fallbackContent() },
            error = { fallbackContent() }
        )
    } else {
        fallbackContent()
    }
}