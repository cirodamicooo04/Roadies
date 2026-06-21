package it.roadies.android_app.ui.booking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

//componenti condivisi e riutilizzabili tra i vari step del booking
@Composable
fun BookingStepProgressBar(
    stepLabel: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stepLabel,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun BookingStepHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    centered: Boolean = icon != null
) {
    val colorScheme = MaterialTheme.colorScheme
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        if (icon != null) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(20.dp),
                color = colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = textAlign,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}


@Composable
fun BookingErrorText(
    error: String?,
    modifier: Modifier = Modifier,
    centered: Boolean = false
) {
    if (error == null) return
    Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = modifier.fillMaxWidth()
    )
}


@Composable
fun BookingStepBottomBar(
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
    secondaryEnabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (secondaryLabel != null) {
            OutlinedButton(
                onClick = onSecondary,
                enabled = secondaryEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(secondaryLabel)
            }
        }
        Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(primaryLabel)
        }
    }
}

@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
