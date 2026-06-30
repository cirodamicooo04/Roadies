package it.roadies.android_app.ui.bookingFlow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Builder
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import it.roadies.android_app.BuildConfig
import it.roadies.android_app.R
import it.roadies.android_app.ui.bookingFlow.components.BookingErrorText
import it.roadies.android_app.ui.bookingFlow.components.BookingStepBottomBar
import it.roadies.android_app.ui.bookingFlow.components.BookingStepHeader
import it.roadies.android_app.ui.bookingFlow.components.BookingStepProgressBar
import it.roadies.android_app.ui.bookingFlow.components.LoadingOverlay
import it.roadies.android_app.ui.bookingFlow.components.BookingExpiredDialog
import it.roadies.android_app.ui.bookingFlow.components.BookingTimerBar
import it.roadies.android_app.viewmodel.bookingFlow.BookingFlowViewModel
import it.roadies.android_app.viewmodel.bookingFlow.PaymentViewModel

@Composable
fun BookingPaymentScreen(onCompleted: () -> Unit, onExpired: () -> Unit, flowViewModel: BookingFlowViewModel, viewModel: PaymentViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val flowState by flowViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        if (BuildConfig.STRIPE_PUBLISHABLE_KEY.isNotBlank()) {
            PaymentConfiguration.init(context, BuildConfig.STRIPE_PUBLISHABLE_KEY)
        }
    }

    val paymentResultCallback = { result: PaymentSheetResult ->
        when (result) {
            is PaymentSheetResult.Completed -> viewModel.onPaymentCompleted()
            is PaymentSheetResult.Canceled -> {}
            is PaymentSheetResult.Failed -> viewModel.onPaymentFailed("fail payment")
        }
    }
    val paymentSheet = remember(paymentResultCallback) { Builder(paymentResultCallback) }.build()


    LaunchedEffect(state.confirmed) {
        if (state.confirmed) onCompleted()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            BookingStepProgressBar(stepLabel = stringResource(R.string.booking_payment_step_label), progress = 1f)

            Spacer(modifier = Modifier.height(16.dp))
            BookingTimerBar(remainingSeconds = flowState.remainingSeconds)

            Spacer(modifier = Modifier.weight(1f))

            BookingStepHeader(
                title = stringResource(R.string.booking_payment_title),
                subtitle = stringResource(R.string.booking_payment_subtitle),
                icon = Icons.Default.Lock
            )

            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                BookingErrorText(error = state.error, centered = true)
            }

            Spacer(modifier = Modifier.weight(1f))

            val clientSecret = state.clientSecret
            BookingStepBottomBar(
                primaryLabel = stringResource(R.string.booking_payment_pay_now),
                onPrimary = {
                    if (clientSecret != null) {
                        paymentSheet.presentWithPaymentIntent(
                            paymentIntentClientSecret = clientSecret,
                            configuration = PaymentSheet.Configuration(merchantDisplayName = "Roadies")
                        )
                    }
                },
                primaryEnabled = clientSecret != null &&
                    !state.isLoading &&
                    !state.isConfirming &&
                    BuildConfig.STRIPE_PUBLISHABLE_KEY.isNotBlank()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (state.isLoading || state.isConfirming) {
            LoadingOverlay(
                message = if (state.isConfirming) stringResource(R.string.booking_payment_confirming) else stringResource(R.string.booking_payment_preparing)
            )
        }


        if (flowState.isExpired) {
            BookingExpiredDialog(onConfirm = onExpired)
        }

        if (state.confirmed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.booking_payment_confirmed),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
