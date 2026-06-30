package it.roadies.android_app.viewmodel.bookingFlow

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.R
import it.roadies.android_app.client.models.booking.BookingMemberDTO
import it.roadies.android_app.client.models.booking.BookingMemberRequest
import it.roadies.android_app.client.models.booking.MemberDocumentRequest
import it.roadies.android_app.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class MemberForm(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val birthDate: LocalDate = LocalDate.now(),
    val notes : String = "",
    val documentType: MemberDocumentRequest.Type = MemberDocumentRequest.Type.ID_CARD
)
//validazioni identiche a quelle del backend
private fun isValidFirstName( name: String): Boolean {
    return name.length < 100 && name.isNotBlank()
}
private fun isValidLastName( name: String): Boolean {
    return name.length < 100 && name.isNotBlank()
}
private fun isValidDate( date: LocalDate): Boolean {
    return date.isBefore(LocalDate.now())
}
private fun isValidPhone( phone: String): Boolean {
    return phone.isNotBlank()
}

data class BookingMembersState(
    val members: List<MemberForm> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadItems: List<DocumentUploadItem>? = null
)

@HiltViewModel
class BookingMembersViewModel @Inject constructor(private val repository: BookingRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    private val bookingId: UUID = UUID.fromString(savedStateHandle["bookingId"])
    private val peopleCount: Int = savedStateHandle["peopleCount"] ?: 1
    private val _state = MutableStateFlow(BookingMembersState(members = List(peopleCount) { MemberForm() }))
    val state: StateFlow<BookingMembersState> = _state.asStateFlow()

    // segnala che la navigazione verso lo step documenti è stata gestita,
    // così tornando indietro a questo step il LaunchedEffect non ri-naviga in avanti
    fun onMembersNavigated() {
        _state.update { current -> current.copy(uploadItems = null) }
    }

    fun updateMember(index: Int, transform: (MemberForm) -> MemberForm) {
        _state.update { current ->
            val updated = current.members.toMutableList()
            updated[index] = transform(updated[index])
            current.copy(members = updated, error = null)
        }
    }

    fun submit() {
        val members = _state.value.members

        val hasInvalid = members.any {!isValidFirstName(it.firstName) || !isValidLastName(it.lastName) || !isValidPhone(it.phoneNumber) || !isValidDate(it.birthDate) }
        if (hasInvalid) {
            _state.update { current -> current.copy(error = "invalid_fields") }
            return
        }

        viewModelScope.launch {
            _state.update { current -> current.copy(isLoading = true, error = null) }

            val request = BookingMemberRequest(
                bookingId = bookingId,
                members = members.map { member ->
                    BookingMemberDTO(
                        firstName = member.firstName.trim(),
                        lastName = member.lastName.trim(),
                        phoneNumber = member.phoneNumber.trim(),
                        birthDate = member.birthDate,
                        notes = member.notes.trim().ifBlank{null},
                        documents = listOf(MemberDocumentRequest(member.documentType))
                    )
                }
            )

            val response = repository.insertMembers(request)
            val responseMembers = response.data?.members
            if (response.success && responseMembers != null) {
                val items = responseMembers.mapIndexedNotNull { index, m ->
                    val documentId = m.documentIds?.firstOrNull() ?: return@mapIndexedNotNull null
                    val form = members.getOrNull(index)
                    DocumentUploadItem(documentId, (m.firstName + " " + m.lastName), form?.documentType?:MemberDocumentRequest.Type.ID_CARD)
                }
                _state.update { current->current.copy(isLoading = false, uploadItems = items) }
            } else {
                _state.update { current->current.copy(isLoading = false, error = response.errorMessage ?: "Error")
                }
            }
        }
    }
}
