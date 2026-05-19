package lab.smartbanner.ui.preview

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.PosterDraft
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterContent
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.model.PosterTheme
import lab.smartbanner.utils.PosterExporter
import lab.smartbanner.utils.contactSupport

sealed class PreviewUiState {
    object Loading : PreviewUiState()
    data class Success(
        val template: PosterTemplate,
        val content: PosterContent,
        val selectedThemeId: String? = null,
        val isLocked: Boolean = false,
        val isTemporarilyUnlocked: Boolean = false
    ) : PreviewUiState()
    data class Error(val message: String) : PreviewUiState()
}

class TemplatePreviewViewModel(
    private val repository: TemplateRepository,
    private val draftRepository: DraftRepository,
    private val posterExporter: PosterExporter,
    private val authRepository: AccessCodeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading)
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()
    
    private val _exportResult = MutableSharedFlow<Result<Unit>>()
    val exportResult = _exportResult.asSharedFlow()

    private var originalContent: PosterContent? = null

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
                    val isLocked = template.config.isLocked
                    
                    val content = initialContent ?: run {
                        val draft = draftRepository.getLatestDraft().first()
                        if (draft?.templateId == id) {
                            draft.content
                        } else {
                            draftRepository.getSavedContent(id).first() ?: PosterContent()
                        }
                    }
                    this@TemplatePreviewViewModel.originalContent = content
                    
                    val allThemes = template.themes + content.userThemes
                    val selectedThemeId = allThemes.find { theme ->
                        theme.colors == content.colorMap
                    }?.id
                    
                    _uiState.value = PreviewUiState.Success(
                        template = template,
                        content = content,
                        selectedThemeId = selectedThemeId,
                        isLocked = isLocked,
                        isTemporarilyUnlocked = false
                    )
                } else {
                    _uiState.value = PreviewUiState.Error("Template not found")
                }
            } catch (e: Exception) {
                _uiState.value = PreviewUiState.Error(e.message ?: "Failed to load template")
            }
        }
    }

    fun unlockTemporarily() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            _uiState.value = currentState.copy(isTemporarilyUnlocked = true)
        }
    }

    private fun isEffectivelyLocked(state: PreviewUiState.Success): Boolean {
        return state.isLocked && !state.isTemporarilyUnlocked
    }

    fun applyTheme(theme: PosterTheme) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val updatedContent = currentState.content.copy(
                colorMap = theme.colors
            )
            _uiState.value = currentState.copy(content = updatedContent, selectedThemeId = theme.id)
            saveContent(updatedContent, currentState.template.id)
        }
    }

    fun updateContent(content: PosterContent) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && !isEffectivelyLocked(currentState)) {
            _uiState.value = currentState.copy(content = content)
            saveContent(content, currentState.template.id)
        }
    }

    private fun saveContent(content: PosterContent, templateId: String) {
        viewModelScope.launch {
            val draft = PosterDraft(templateId, content)
            draftRepository.saveDraft(draft)
            draftRepository.saveTemplateContent(templateId, content)
        }
    }

    fun resetToDefault() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && !isEffectivelyLocked(currentState)) {
            val emptyContent = PosterContent()
            _uiState.value = currentState.copy(content = emptyContent, selectedThemeId = null)
            viewModelScope.launch {
                draftRepository.clearActiveDraft()
                draftRepository.saveTemplateContent(currentState.template.id, emptyContent)
            }
        }
    }

    fun addUserTheme(theme: PosterTheme) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && !isEffectivelyLocked(currentState)) {
            val updatedUserThemes = currentState.content.userThemes.toMutableList()
            val existingIndex = updatedUserThemes.indexOfFirst { it.id == theme.id }
            if (existingIndex != -1) {
                updatedUserThemes[existingIndex] = theme
            } else {
                updatedUserThemes.add(theme)
            }

            val updatedContent = currentState.content.copy(
                userThemes = updatedUserThemes,
                colorMap = theme.colors
            )
            _uiState.value = currentState.copy(content = updatedContent, selectedThemeId = theme.id)
            saveContent(updatedContent, currentState.template.id)
        }
    }

    fun deleteUserTheme(themeId: String) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && !isEffectivelyLocked(currentState)) {
            val updatedUserThemes = currentState.content.userThemes.filter { it.id != themeId }
            val updatedContent = currentState.content.copy(userThemes = updatedUserThemes)
            
            val isSelectedThemeDeleted = currentState.selectedThemeId == themeId
            val newSelectedThemeId = if (isSelectedThemeDeleted) null else currentState.selectedThemeId
            
            _uiState.value = currentState.copy(content = updatedContent, selectedThemeId = newSelectedThemeId)
            saveContent(updatedContent, currentState.template.id)
        }
    }

    suspend fun saveCurrentDraft() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && !isEffectivelyLocked(currentState)) {
            if (currentState.content != originalContent) {
                val draft = PosterDraft(currentState.template.id, currentState.content)
                draftRepository.saveDraft(draft)
                draftRepository.saveTemplateContent(currentState.template.id, currentState.content)
            }
        }
    }

    suspend fun completeEditing() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && !isEffectivelyLocked(currentState)) {
            draftRepository.saveTemplateContent(currentState.template.id, currentState.content)
            draftRepository.clearActiveDraft()
        }
    }

    fun exportPoster(bitmap: ImageBitmap) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success && !isEffectivelyLocked(currentState)) {
            viewModelScope.launch {
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val fileName = "SmartBanner_${currentState.template.id}_$timestamp"
                val result = posterExporter.saveToGallery(bitmap, fileName)
                _exportResult.emit(result)
            }
        }
    }

    fun contactSupportForLockedTemplate(templateName: String) {
        viewModelScope.launch {
            val accessCode = authRepository.getAccessCode()
            contactSupport(
                subject = "Access Request: $templateName",
                body = "Hi, I would like to get access to the '$templateName' template. Please let me know the process.",
                accessCode = accessCode
            )
        }
    }
}
