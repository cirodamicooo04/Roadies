package it.roadies.android_app.viewmodel.bookingHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.booking.BookingHomeResponse
import it.roadies.android_app.repository.AuthRepository
import it.roadies.android_app.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class BookingFilterType {
    ALL, TRAVEL, ACTIVITY
}

data class BookingPagingState(
    val items: List<BookingHomeResponse> = emptyList(),
    val nextPage: Int = 0,
    val isLast: Boolean = false,
    val isLoading: Boolean = false
)

data class BookingHomeState(
    val isLoggedIn: Boolean = false,
    val active: BookingPagingState = BookingPagingState(),
    val past: BookingPagingState = BookingPagingState(),
    val filterType: BookingFilterType = BookingFilterType.ALL,
    val errorMessage: String? = null
)

const val PAGE_SIZE = 10

@HiltViewModel
class BookingHomeViewModel @Inject constructor(private val repository: BookingRepository, private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(BookingHomeState())
    val state: StateFlow<BookingHomeState> = _state.asStateFlow()

    init {
        observeAuthState()
    }
    
    fun setFilterType(type: BookingFilterType) {
        _state.update { it.copy(filterType = type) }
    }
    
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { auth ->
                if (auth.isLoading) return@collectLatest

                _state.value = _state.value.copy(isLoggedIn = auth.isLogged)

                if (auth.isLogged) {
                    loadMoreActive()
                    loadMorePast()
                }
            }
        }
    }

    fun loadMoreActive() {
        val cur = _state.value.active
        if (cur.isLoading || cur.isLast) return
        viewModelScope.launch {
            _state.update { it.copy(active = it.active.copy(isLoading = true)) }
            val res = repository.getActiveBookings(cur.nextPage, PAGE_SIZE)
            val page = res.data
            _state.update {
                it.copy(
                    active = it.active.copy(
                        items = it.active.items + (page?.content ?: emptyList()),
                        nextPage = it.active.nextPage + 1,
                        isLast = page?.last ?: true,
                        isLoading = false
                    ),
                    errorMessage = res.errorMessage
                )
            }
        }
    }

    fun loadMorePast() {
        val cur = _state.value.past
        if (cur.isLoading || cur.isLast) return
        viewModelScope.launch {
            _state.update { it.copy(past = it.past.copy(isLoading = true)) }
            val res = repository.getPastBookings(cur.nextPage, PAGE_SIZE)
            val page = res.data
            _state.update {
                it.copy(
                    past = it.past.copy(
                        items = it.past.items + (page?.content ?: emptyList()),
                        nextPage = it.past.nextPage + 1,
                        isLast = page?.last ?: true,
                        isLoading = false
                    ),
                    errorMessage = res.errorMessage
                )
            }
        }
    }
}