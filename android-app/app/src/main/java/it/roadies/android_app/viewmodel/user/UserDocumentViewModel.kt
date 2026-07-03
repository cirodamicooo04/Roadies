package it.roadies.android_app.viewmodel.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.user.UserDocumentRequestDTO
import it.roadies.android_app.client.models.user.UserDocumentResponseDTO
import it.roadies.android_app.repository.UserDocumentRepository
import it.roadies.android_app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserDocumentsState(
    val isLoading: Boolean = false,
    val documents: List<UserDocumentResponseDTO> = emptyList(),
    val error: String? = null,
    val isUploading: Boolean = false
)

@HiltViewModel
class UserDocumentsViewModel @Inject constructor(
    private val documentRepository: UserDocumentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserDocumentsState())
    val state: StateFlow<UserDocumentsState> = _state.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentUser = userRepository.getCurrentUser()

            if (currentUser == null) {
                _state.update { it.copy(isLoading = false, error = "Utente non loggato") }
                return@launch
            }

            val response = documentRepository.getMyDocuments(currentUser.id)
            if (response.success && response.data != null) {
                _state.update { it.copy(isLoading = false, documents = response.data) }
            } else {
                _state.update { it.copy(isLoading = false, error = response.errorMessage) }
            }
        }
    }

    fun uploadDocument(type: UserDocumentRequestDTO.DocumentType, number: String, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            val currentUser = userRepository.getCurrentUser()

            if (currentUser == null) {
                _state.update { it.copy(isUploading = false, error = "Utente non loggato") }
                return@launch
            }

            val response = documentRepository.uploadDocument(currentUser.id, type, number, uri)

            _state.update { it.copy(isUploading = false) }

            if (response.success) {
                loadDocuments()
            } else {
                _state.update { it.copy(error = response.errorMessage) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}