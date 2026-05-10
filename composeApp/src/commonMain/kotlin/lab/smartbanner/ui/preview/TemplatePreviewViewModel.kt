package lab.smartbanner.ui.preview

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.PosterDraft
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterContent
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.utils.PosterExporter

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
    private val draftRepository: DraftRepository,
    private val posterExporter: PosterExporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading)
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()
    
    private val _exportResult = MutableSharedFlow<Result<Unit>>()
    val exportResult = _exportResult.asSharedFlow()

    private var saveJob: Job? = null

    fun loadTemplate(id: String, initialContent: PosterContent? = null) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && currentState.template.id == id && initialContent == null) {
            return
        }

        viewModelScope.launch {
            if (_uiState.value !is PreviewUiState.Success) {
                _uiState.value = PreviewUiState.Loading
            }
            try {
                val template = repository.getTemplateById(id)
                if (template != null) {
                    val content = initialContent ?: run {
                        // Priority: 1. Active Draft (if same template), 2. Saved Template Content, 3. Default
                        val draft = draftRepository.getLatestDraft().first()
                        if (draft?.templateId == id) {
                            draft.content
                        } else {
                            draftRepository.getSavedContent(id).first() ?: PosterContent()
                        }
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
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val updatedContent = currentState.content.copy(
                textMap = currentState.content.textMap + (key to value)
            )
            
            // Update UI state immediately
            _uiState.value = currentState.copy(content = updatedContent)
            
            // Debounce active draft saving
            saveJob?.cancel()
            saveJob = viewModelScope.launch {
                delay(300)
                draftRepository.saveDraft(PosterDraft(currentState.template.id, updatedContent))
            }
        }
    }

    /**
     * Saves the current content specifically for this template AND as an active draft.
     */
    suspend fun saveCurrentDraft() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            saveJob?.cancel()
            val draft = PosterDraft(currentState.template.id, currentState.content)
            draftRepository.saveDraft(draft)
            draftRepository.saveTemplateContent(currentState.template.id, currentState.content)
        }
    }

    /**
     * Called when the user is "Done". Persists content for the template and clears the resume prompt.
     */
    suspend fun completeEditing() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            saveJob?.cancel()
            draftRepository.saveTemplateContent(currentState.template.id, currentState.content)
            draftRepository.clearActiveDraft()
        }
    }

    fun exportPoster(bitmap: ImageBitmap) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            viewModelScope.launch {
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val fileName = "PosterWala_${currentState.template.id}_$timestamp"
                val result = posterExporter.saveToGallery(bitmap, fileName)
                _exportResult.emit(result)
            }
        }
    }
}
