package lab.smartbanner.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.PosterDraft
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
    private val repository: TemplateRepository,
    private val draftRepository: DraftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading)
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    fun loadTemplate(id: String, initialContent: PosterContent? = null) {
        val currentState = _uiState.value
        // If already loaded and content is provided, we might want to update it.
        // But for now, let's keep the check for same template.
        if (currentState is PreviewUiState.Success && currentState.template.id == id) {
             if (initialContent != null && currentState.content != initialContent) {
                 _uiState.value = currentState.copy(content = initialContent)
             }
             return
        }

        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading
            try {
                val template = repository.getTemplateById(id)
                if (template != null) {
                    val content = initialContent ?: run {
                        val draft = draftRepository.getLatestDraft().first()
                        if (draft?.templateId == id) draft.content else PosterContent()
                    }
                    
                    _uiState.value = PreviewUiState.Success(
                        template = template,
                        content = content
                    )
                } else {
                    _uiState.value = PreviewUiState.Error("Template not found")
                }
            } catch (e: Exception) {
                _uiState.value = PreviewUiState.Error(e.message ?: "Failed to load template")
            }
        }
    }

    fun updateTextContent(key: String, value: String) {
        _uiState.update { state ->
            if (state is PreviewUiState.Success) {
                val newContent = state.content.copy(
                    textMap = state.content.textMap + (key to value)
                )
                saveDraftInternal(state.template.id, newContent)
                state.copy(content = newContent)
            } else state
        }
    }

    fun updateColorContent(key: String, hexColor: String) {
        _uiState.update { state ->
            if (state is PreviewUiState.Success) {
                val newContent = state.content.copy(
                    colorMap = state.content.colorMap + (key to hexColor)
                )
                saveDraftInternal(state.template.id, newContent)
                state.copy(content = newContent)
            } else state
        }
    }

    private fun saveDraftInternal(templateId: String, content: PosterContent) {
        viewModelScope.launch {
            draftRepository.saveDraft(PosterDraft(templateId, content))
        }
    }
    
    fun clearDraft() {
        viewModelScope.launch {
            draftRepository.clearDraft()
        }
    }
}
