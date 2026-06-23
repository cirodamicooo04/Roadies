package it.roadies.android_app.ui.bookingFlow

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.roadies.android_app.ui.bookingFlow.components.BookingErrorText
import it.roadies.android_app.ui.bookingFlow.components.BookingStepBottomBar
import it.roadies.android_app.ui.bookingFlow.components.BookingStepHeader
import it.roadies.android_app.ui.bookingFlow.components.BookingStepProgressBar
import it.roadies.android_app.ui.bookingFlow.components.LoadingOverlay
import it.roadies.android_app.viewmodel.bookingFlow.BookingStatus
import it.roadies.android_app.viewmodel.bookingFlow.BookingViewModel
import it.roadies.android_app.R

@Composable
fun BookingStepPeopleScreen(onConfirmed: (peopleCount: Int) -> Unit, viewModel: BookingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status == BookingStatus.RESERVE_CONFIRMED) onConfirmed(state.peopleCount)
    }

    val errorMessage = when {
        state.status == BookingStatus.RESERVE_REJECTED -> stringResource(R.string.booking_without_seats)
        else -> state.error
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BookingStepPeopleContent(
            peopleCount = state.peopleCount,
            isLoading = state.isLoading,
            error = errorMessage,
            onIncrease = { viewModel.increasePeopleCount() },
            onDecrease = { viewModel.decreasePeopleCount() },
            onNext = { viewModel.onNext() },
            onBack = { viewModel.onBack() }
        )

        if (state.isLoading) {
            LoadingOverlay(message = stringResource(R.string.booking_searching_seats))
        }
    }
}

@Composable
fun BookingStepPeopleContent(
    peopleCount: Int,
    isLoading: Boolean = false,
    error: String? = null,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {

        // progress bar
        Spacer(modifier = Modifier.height(40.dp))
        BookingStepProgressBar(stepLabel = stringResource(R.string.booking_step_1_of_4), progress = 0.25f)

        Spacer(modifier = Modifier.weight(1f))

        // icona + titolo
        BookingStepHeader(
            title = stringResource(R.string.booking_people_title),
            subtitle = stringResource(R.string.booking_people_subtitle),
            icon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(40.dp))

        // stepper
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // pulsante "diminuisci"
                FilledIconButton(
                    onClick = onDecrease,
                    enabled = peopleCount > 1 && !isLoading,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colorScheme.surface,
                        contentColor = colorScheme.primary,
                        disabledContainerColor = colorScheme.surface.copy(alpha = 0.4f),
                        disabledContentColor = colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(R.string.booking_decrease),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // contatore
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = peopleCount,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { -it } + fadeIn() togetherWith
                                        slideOutVertically { it } + fadeOut()
                            } else {
                                slideInVertically { it } + fadeIn() togetherWith
                                        slideOutVertically { -it } + fadeOut()
                            }
                        },
                        label = "counter_animation"
                    ) { count ->
                        Text(
                            text = count.toString(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            lineHeight = 64.sp
                        )
                    }
                    Text(
                        text = if (peopleCount == 1) stringResource(R.string.booking_person) else stringResource(R.string.booking_people),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                // pulsante "aumenta"
                FilledIconButton(
                    onClick = onIncrease,
                    enabled = !isLoading,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.booking_increase),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // persone visualizzate
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val maxIconsVisible = 8
            val iconsToShow = minOf(peopleCount, maxIconsVisible)
            repeat(iconsToShow) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = colorScheme.primary.copy(alpha = 0.45f),
                    modifier = Modifier.size(22.dp)
                )
            }
            if (peopleCount > maxIconsVisible) {
                Text(
                    text = "+${peopleCount - maxIconsVisible}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // errore
        BookingErrorText(
            error = error,
            centered = true,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // navigazione
        BookingStepBottomBar(
            primaryLabel = stringResource(R.string.booking_continue),
            onPrimary = onNext,
            primaryEnabled = !isLoading,
            secondaryLabel = stringResource(R.string.booking_cancel),
            onSecondary = onBack,
            secondaryEnabled = !isLoading,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}