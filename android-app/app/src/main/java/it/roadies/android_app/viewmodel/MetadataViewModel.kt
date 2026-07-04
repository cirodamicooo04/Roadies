package it.roadies.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.roadies.android_app.client.models.travel.TagResponse
import it.roadies.android_app.repository.MetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetadataDataUiState (
    val isLoading: Boolean = false,
    val tags: List<TagResponse>? = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class MetadataViewModel @Inject constructor(private val metadataRepository: MetadataRepository): ViewModel(){
    private val _uiState = MutableStateFlow(MetadataDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags(){
        viewModelScope.launch {
            _uiState.value = MetadataDataUiState(isLoading = true)

            val response = metadataRepository.getTags()
            if (response.success && response.data != null){
                _uiState.value =  MetadataDataUiState(isLoading = false, tags = response.data)
            } else {
                _uiState.value =  MetadataDataUiState(isLoading = false, errorMessage = response.errorMessage?: "Error while fetching tags")
            }
        }
    }

    fun addNewTag(tagName: String){
        viewModelScope.launch {
            val response = metadataRepository.addTag(tagName)
            if (response.success) {
                loadTags()
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = response.errorMessage ?: "Errore durante l'aggiunta del tag"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}