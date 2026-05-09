package lab.smartbanner.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.model.TextElement

sealed class PreviewUiState {
    object Loading : PreviewUiState()
    data class Success(val template: PosterTemplate) : PreviewUiState()
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
                _uiState.value = PreviewUiState.Success(template)
            } else {
                _uiState.value = PreviewUiState.Error("Template not found")
            }
        }
    }

    fun updateElementText(elementId: String, newText: String) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val updatedElements = currentState.template.elements.map { element ->
                if (element is TextElement && element.id == elementId) {
                    element.copy(text = newText)
                } else {
                    element
                }
            }
            _uiState.value = currentState.copy(
                template = currentState.template.copy(elements = updatedElements)
            )
        }
    }
}
