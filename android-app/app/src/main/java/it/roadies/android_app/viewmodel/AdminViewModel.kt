package it.roadies.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.client.models.user.UserResponseDTO
import it.roadies.android_app.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(private val admRepository: AdminRepository) : ViewModel() {

    // User state
    private val _users = MutableStateFlow<List<UserResponseDTO>>(emptyList())
    val users: StateFlow<List<UserResponseDTO>> = _users

    private val _pendingRequests = MutableStateFlow<List<PendingOrganizerRequestResponseDTO>>(emptyList())
    val pendingRequests: StateFlow<List<PendingOrganizerRequestResponseDTO>> = _pendingRequests

    // Get users by filter
    fun fetchUsers(filter: String) {
        viewModelScope.launch {
            try {
                _users.value = admRepository.getUsersByFilter(filter)
            } catch (e: Exception) {
                // Error processing logic
            }
        }
        // for testing only
//        _users.value = listOf(
//            UserResponseDTO(keycloakId = "1", username = "beast",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = true ),
//            UserResponseDTO(keycloakId = "2", username = "monster",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = true ),
//            UserResponseDTO(keycloakId = "3", username = "user",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = true ),
//            UserResponseDTO(keycloakId = "4", username = "beast",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = true ),
//            UserResponseDTO(keycloakId = "5", username = "beast",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = true ),
//            UserResponseDTO(keycloakId = "6", username = "beast",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = false ),
//            UserResponseDTO(keycloakId = "7", username = "beast",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = true ),
//            UserResponseDTO(keycloakId = "8", username = "beast",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv", avatarUrl = "...", points = 1, badge = "..", enabled = false )
//        )
    }

    // block user
    fun blockUser(keycloakId: String) {
        viewModelScope.launch {
            try {
                admRepository.blockUser(keycloakId)
                // Aggiorna la lista
                fetchUsers("ACTIVE")
            } catch (e: Exception) { /* handle error */ }
        }
    }

    // unblock user
    fun unblockUser(keycloakId: String) {
        viewModelScope.launch {
            try {
                admRepository.unblockUser(keycloakId)
                fetchUsers("BANNED")
            } catch (e: Exception) { /* handle error */ }
        }
    }

    // accept/reject organizer request
    fun reviewOrganizerRequest(keycloakId: String, approved: Boolean, reason: String?) {
        viewModelScope.launch {
            try {
                admRepository.reviewOrganizerRequest(keycloakId, approved, reason)
                loadPendingRequests() // update list
            } catch (e: Exception) { /* handle error */ }
        }
    }

    // get requests
    fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                _pendingRequests.value = admRepository.getPendingOrganizerRequests()
            } catch (e: Exception) { /* handle error */ }
        }

//        _pendingRequests.value = listOf(
//            PendingOrganizerRequestResponseDTO(keycloakId = "1", username = "beast",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv"),
//            PendingOrganizerRequestResponseDTO(keycloakId = "1", username = "monster",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv"),
//            PendingOrganizerRequestResponseDTO(keycloakId = "1", username = "legendary",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv"),
//            PendingOrganizerRequestResponseDTO(keycloakId = "1", username = "Huge",firstName = "Ahmad", lastName = "Alradi", email = "sn;lkmfv")
//        )
    }
}