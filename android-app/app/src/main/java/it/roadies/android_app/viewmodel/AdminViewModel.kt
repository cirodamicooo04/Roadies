package it.roadies.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.roadies.android_app.client.apis.user.AdminManagementApi
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val adminApi: AdminManagementApi) : ViewModel() {

    // User state
    private val _users = MutableStateFlow<List<UserProfileResponseDTO>>(emptyList())
    val users: StateFlow<List<UserProfileResponseDTO>> = _users

    // حالة طلبات المنسقين المعلقة
    private val _pendingRequests = MutableStateFlow<List<PendingOrganizerRequestResponseDTO>>(emptyList())
    val pendingRequests: StateFlow<List<PendingOrganizerRequestResponseDTO>> = _pendingRequests

    // Get users by filter
    fun fetchUsers(filter: String) {
        viewModelScope.launch {
            try {
                _users.value = adminApi.getFilteredUsers(filter)
            } catch (e: Exception) {
                // Error processing logic
            }
        }
    }

    // block user
    fun blockUser(keycloakId: String) {
        viewModelScope.launch {
            try {
                adminApi.blockUser(keycloakId)
                // Aggiorna la lista
                fetchUsers("ACTIVE")
            } catch (e: Exception) { /* handle error */ }
        }
    }

    // unblock user
    fun unblockUser(keycloakId: String) {
        viewModelScope.launch {
            try {
                adminApi.unblockUser(keycloakId)
                fetchUsers("BANNED")
            } catch (e: Exception) { /* handle error */ }
        }
    }

    // accept/reject organizer request
    fun reviewOrganizerRequest(keycloakId: String, approved: Boolean, reason: String?) {
        viewModelScope.launch {
            try {
                adminApi.reviewOrganizerRequest(keycloakId, approved, reason)
                loadPendingRequests() // update list
            } catch (e: Exception) { /* handle error */ }
        }
    }

    // get requests
    fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                _pendingRequests.value = adminApi.getPendingOrganizerRequests()
            } catch (e: Exception) { /* handle error */ }
        }
    }
}