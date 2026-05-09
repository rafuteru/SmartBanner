package lab.smartbanner.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
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
        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading
            try {
                val template = repository.getTemplateById(id)
                if (template != null) {
                    val content = initialContent ?: run {
                        val draft = draftRepository.getLatestDraft().first()
                        if (draft?.templateId == id) draft.content else PosterContent()
                    }
                    _uiState.value = PreviewUiState.Success(template, content)
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
                // Persist draft in background
                viewModelScope.launch {
                    draftRepository.saveDraft(PosterDraft(state.template.id, newContent))
                }
                state.copy(content = newContent)
            } else state
        }
    }
}
