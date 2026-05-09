package lab.smartbanner.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterContent
import lab.smartbanner.model.PosterTemplate

sealed class PreviewUiState {
    object Loading : PreviewUiState()
    data class Success(
        val template: PosterTemplate,
        val content: PosterContent
    ) : PreviewUiState()
    data class Error(val message: String) : PreviewUiState()
}

class TemplatePreviewViewModel(
    private val repository: TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading)
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    fun loadTemplate(id: String) {
        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading
            val template = repository.getTemplateById(id)
            if (template != null) {
                // Initialize content map with default values from template if needed
                _uiState.value = PreviewUiState.Success(
                    template = template,
                    content = PosterContent()
                )
            } else {
                _uiState.value = PreviewUiState.Error("Template not found")
            }
        }
    }

    /**
     * Updates content based on a key (e.g., "shop_name") instead of a specific element ID.
     */
    fun updateTextContent(key: String, value: String) {
        _uiState.update { state ->
            if (state is PreviewUiState.Success) {
                state.copy(
                    content = state.content.copy(
                        textMap = state.content.textMap + (key to value)
                    )
                )
            } else state
        }
    }

    /**
     * Updates color based on a key (e.g., "primary_color").
     */
    fun updateColorContent(key: String, hexColor: String) {
        _uiState.update { state ->
            if (state is PreviewUiState.Success) {
                state.copy(
                    content = state.content.copy(
                        colorMap = state.content.colorMap + (key to hexColor)
                    )
                )
            } else state
        }
    }
}
