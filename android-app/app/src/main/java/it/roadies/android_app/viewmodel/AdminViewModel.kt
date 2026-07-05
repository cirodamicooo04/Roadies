package it.roadies.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.user.PendingOrganizerRequestResponseDTO
import it.roadies.android_app.client.models.user.UserProfileResponseDTO
import it.roadies.android_app.client.models.user.UserResponseDTO
import it.roadies.android_app.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val admRepository: AdminRepository
) : ViewModel() {

    // States for data
    private val _users = MutableStateFlow<List<UserResponseDTO>>(emptyList())
    val users: StateFlow<List<UserResponseDTO>> = _users

    private val _pendingRequests = MutableStateFlow<List<PendingOrganizerRequestResponseDTO>>(emptyList())
    val pendingRequests: StateFlow<List<PendingOrganizerRequestResponseDTO>> = _pendingRequests

    // UI States for handling errors and loading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _topUsers = MutableStateFlow<List<UserProfileResponseDTO>>(emptyList())
    val topUsers: StateFlow<List<UserProfileResponseDTO>> = _topUsers.asStateFlow()
    private val _isStatsLoading = MutableStateFlow(false)
    val isStatsLoading: StateFlow<Boolean> = _isStatsLoading.asStateFlow()

    // Fetch users by filter
    fun fetchUsers(filter: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = admRepository.getUsersByFilter(filter)
            if (response.success && response.data != null) {
                _users.value = response.data
                _errorMessage.value = null
            } else {
                _users.value = emptyList()
                _errorMessage.value = response.errorMessage ?: "Failed to load users"
            }
            _isLoading.value = false
        }
    }

    // Fetch top 20 users by points
    fun fetchTopUsers() {
        viewModelScope.launch {
            _isStatsLoading.value = true
            val response = admRepository.getTop20Travelers()
            if (response.success && response.data != null) {
                _topUsers.value = response.data
                _errorMessage.value = null
            } else {
                _topUsers.value = emptyList()
                _errorMessage.value = response.errorMessage ?: "Failed to load leaderboard statistics"
            }
            _isStatsLoading.value = false
        }
    }

    // Block user
    fun blockUser(keycloakId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = admRepository.blockUser(keycloakId)
            if (response.success) {
                fetchUsers("ACTIVE") // Refresh list
            } else {
                _errorMessage.value = response.errorMessage ?: "Failed to block user"
            }
            _isLoading.value = false
        }
    }

    // Unblock user
    fun unblockUser(keycloakId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = admRepository.unblockUser(keycloakId)
            if (response.success) {
                fetchUsers("BANNED") // Refresh list
            } else {
                _errorMessage.value = response.errorMessage ?: "Failed to unblock user"
            }
            _isLoading.value = false
        }
    }

    // Accept/Reject organizer request
    fun reviewOrganizerRequest(keycloakId: String, approved: Boolean, reason: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = admRepository.reviewOrganizerRequest(keycloakId, approved, reason)
            if (response.success) {
                loadPendingRequests() // Refresh list
            } else {
                _errorMessage.value = response.errorMessage ?: "Failed to process request"
            }
            _isLoading.value = false
        }
    }

    // Get pending requests
    fun loadPendingRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            val response = admRepository.getPendingOrganizerRequests()
            if (response.success && response.data != null) {
                _pendingRequests.value = response.data
                _errorMessage.value = null
            } else {
                _pendingRequests.value = emptyList()
                _errorMessage.value = response.errorMessage ?: "Failed to load requests"
            }
            _isLoading.value = false
        }
    }

    // Clear error message after displaying it
    fun clearError() {
        _errorMessage.value = null
    }


}