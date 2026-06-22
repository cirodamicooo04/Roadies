package it.roadies.android_app.viewmodel.bookingFlow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.booking.BookingCreateRequest
import it.roadies.android_app.client.models.booking.BookingStatusResponse
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

enum class BookingStatus { DRAFT, PENDING, RESERVE_CONFIRMED, RESERVE_REJECTED, READY_FOR_PAYMENT, CONFIRMED, CANCELLED, EXPIRED }
data class BookingState(
    val isLoading: Boolean = false,
    val status: BookingStatus = BookingStatus.DRAFT,
    val error: String? = null,
    val peopleCount: Int = 1
)

@HiltViewModel
class BookingViewModel @Inject constructor (private val repository: BookingRepository, savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()
    private val bookingId: UUID = UUID.fromString(savedStateHandle["bookingId"])
    private val travelId: UUID? = savedStateHandle.get<String>("travelId")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) }
    private val activityId: UUID? = savedStateHandle.get<String>("activityId")?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) }

    fun increasePeopleCount() {
        _state.value = _state.value.copy(peopleCount = _state.value.peopleCount + 1, error = null)
    }

    fun decreasePeopleCount() {
        if (_state.value.peopleCount <= 1) return
        _state.value = _state.value.copy(peopleCount = _state.value.peopleCount - 1, error = null)
    }

    fun onNext() {
        viewModelScope.launch {
            try {
                val request = BookingCreateRequest(bookingId, travelId, activityId, _state.value.peopleCount)
                _state.update { current -> current.copy(isLoading = true, error = null) }

                val reserveResponse = repository.createPendingAndReserveSeats(request)
                if (!reserveResponse.success) {
                    _state.update { current -> current.copy(isLoading = false, error = reserveResponse.errorMessage ?: "Errore nella prenotazione")
                    }
                } else {
                    withTimeout(30_000L.milliseconds) {
                        while (true) {
                            delay(2_000L.milliseconds)
                            val statusResponse = repository.getStatus(bookingId)
                            if (statusResponse.success && statusResponse.data != null) {
                                val newStatus = statusResponse.data.status.toBookingStatus()
                                _state.update { current -> current.copy(status = newStatus) }
                                if (newStatus == BookingStatus.RESERVE_CONFIRMED || newStatus == BookingStatus.RESERVE_REJECTED) break
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _state.update { current -> current.copy(error = "Timeout: nessuna risposta dal server. Riprova.") }
            } catch (e: Exception) {
                _state.update { current -> current.copy(error = e.message) }
            } finally {
                _state.update { current -> current.copy(isLoading = false) }
            }
        }
    }

    fun onBack() {
        viewModelScope.launch {
            //al momento chiamo il delete ma in realtà lo devo rimettere in draft e non in stato cancelled
            repository.deleteBooking(bookingId)
        }
    }
}

private fun BookingStatusResponse.Status.toBookingStatus(): BookingStatus = when (this) {
    BookingStatusResponse.Status.DRAFT -> BookingStatus.DRAFT
    BookingStatusResponse.Status.PENDING -> BookingStatus.PENDING
    BookingStatusResponse.Status.RESERVE_CONFIRMED -> BookingStatus.RESERVE_CONFIRMED
    BookingStatusResponse.Status.RESERVE_REJECTED -> BookingStatus.RESERVE_REJECTED
    BookingStatusResponse.Status.READY_FOR_PAYMENT -> BookingStatus.READY_FOR_PAYMENT
    BookingStatusResponse.Status.CONFIRMED -> BookingStatus.CONFIRMED
    BookingStatusResponse.Status.CANCELLED -> BookingStatus.CANCELLED
    BookingStatusResponse.Status.EXPIRED -> BookingStatus.EXPIRED
}