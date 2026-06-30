package it.roadies.android_app.viewmodel.bookingFlow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.booking.BookingStatusResponse
import it.roadies.android_app.client.models.booking.PaymentRequest
import it.roadies.android_app.repository.BookingRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class PaymentState(
    val isLoading: Boolean = false,
    val clientSecret: String? = null,
    val isConfirming: Boolean = false,
    val confirmed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(private val repository: BookingRepository, savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookingId: UUID = UUID.fromString(savedStateHandle["bookingId"])
    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    init {
        createPaymentIntent()
    }

    fun createPaymentIntent() {
        viewModelScope.launch {
            _state.update { current->current.copy(isLoading = true, error = null) }
            val response = repository.createPaymentIntent(PaymentRequest(bookingId))
            val clientSecret = response.data?.clientSecret
            if (response.success && clientSecret != null) {
                _state.update { current->current.copy(isLoading = false, clientSecret = clientSecret) }
            } else {
                _state.update { current->current.copy(isLoading = false, error = response.errorMessage ?: "We have problem with payments. Try again later")
                }
            }
        }
    }

    fun onPaymentCompleted() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isConfirming = true, error = null) }
                withTimeout(30_000L.milliseconds) {
                    while (true) {
                        val statusResponse = repository.getStatus(bookingId)
                        if (statusResponse.success && statusResponse.data?.status == BookingStatusResponse.Status.CONFIRMED) {
                            _state.update { current->current.copy(isConfirming = false, confirmed = true) }
                            break
                        }
                        delay(2_000L.milliseconds)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _state.update {
                    it.copy(
                        isConfirming = false,
                        error = "Payment received, confirmation in progress. Check your bookings shortly"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isConfirming = false, error = e.message) }
            }
        }
    }

    fun onPaymentFailed(message: String?) {
        _state.update { current->current.copy(error = message ?: "Payment failed") }
    }
}
