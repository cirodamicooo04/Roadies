package it.roadies.android_app.viewmodel.bookingFlow

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.booking.MemberDocumentRequest
import it.roadies.android_app.repository.BookingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val BOOKING_TIMER_SECONDS = 15 * 60

data class DocumentUploadItem(
    val documentId: UUID,
    val memberName: String,
    val documentType: MemberDocumentRequest.Type,
    val localUri: String? = null,
    val uploaded: Boolean = false
)

data class DocumentsUploadState(
    val items: List<DocumentUploadItem> = emptyList(),
    val isUploading: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false,
    val remainingSeconds: Int = BOOKING_TIMER_SECONDS,
    val isExpired: Boolean = false
)

@HiltViewModel
class BookingFlowViewModel @Inject constructor(private val repository: BookingRepository) : ViewModel() {

    private val _state = MutableStateFlow(DocumentsUploadState())
    val state: StateFlow<DocumentsUploadState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var deadlineElapsed: Long = 0L

    fun startTimer() {
        if (timerJob != null) return
        deadlineElapsed = SystemClock.elapsedRealtime() + BOOKING_TIMER_SECONDS * 1000L
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = ((deadlineElapsed - SystemClock.elapsedRealtime()) / 1000L).toInt().coerceAtLeast(0)
                _state.update { current -> current.copy(remainingSeconds = remaining) }
                if (remaining <= 0) {
                    _state.update { current-> current.copy(isExpired = true) }
                    break
                }
                delay(1000L.milliseconds)
            }
        }
    }

    // segnala che la navigazione verso lo step pagamento è stata gestita
    // così tornando indietro a questo step il LaunchedEffect non ri-naviga in avanti
    fun onCompletedNavigated() {
        _state.update { current -> current.copy(completed = false) }
    }

    fun setItems(items: List<DocumentUploadItem>) {
        _state.update { current -> current.copy(items = items, completed = false) }
    }

    fun onPhotoPicked(documentId: UUID, uri: String) {
        _state.update { current -> current.copy(
                items = current.items.map { if (it.documentId == documentId) it.copy(localUri = uri) else it },
                error = null
            )
        }
    }

    fun uploadAll() {
        val items = _state.value.items
        if (items.any { !it.uploaded && it.localUri == null }) {
            _state.update { current -> current.copy(error = "Upload a photo for each document") }
            return
        }

        viewModelScope.launch {
            _state.update { current -> current.copy(isUploading = true, error = null) }

            for (item in items) {
                if (item.uploaded || item.localUri == null) continue

                val response = repository.uploadDocumentPhoto(item.documentId, item.localUri)
                if (response.success) {
                    _state.update { current -> current.copy(items = current.items.map { if (it.documentId == item.documentId) it.copy(uploaded = true) else it }) }
                } else {
                    _state.update { current -> current.copy(isUploading = false, error = response.errorMessage ?: "Error with document of: ${item.memberName}") }
                    return@launch
                }
            }

            _state.update { current -> current.copy(isUploading = false, completed = true) }
        }
    }
}
