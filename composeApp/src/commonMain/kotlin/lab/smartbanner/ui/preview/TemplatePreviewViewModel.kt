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
import lab.smartbanner.model.PosterTheme
import lab.smartbanner.utils.PosterExporter

sealed class PreviewUiState {
    object Loading : PreviewUiState()
    data class Success(
        val template: PosterTemplate,
        val content: PosterContent,
        val selectedThemeId: String? = null
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
                    
                    _uiState.value = PreviewUiState.Success(template, content, selectedThemeId)
                } else {
                    _uiState.value = PreviewUiState.Error("Template not found")
                }
            } catch (e: Exception) {
                _uiState.value = PreviewUiState.Error(e.message ?: "Failed to load template")
            }
        }
    }

    fun applyTheme(theme: PosterTheme) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val updatedContent = currentState.content.copy(
                colorMap = theme.colors
            )
            _uiState.value = currentState.copy(content = updatedContent, selectedThemeId = theme.id)
            autoSave(updatedContent, currentState.template.id)
        }
    }

    fun addUserTheme(theme: PosterTheme) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
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
            autoSave(updatedContent, currentState.template.id)
        }
    }

    fun deleteUserTheme(themeId: String) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val updatedUserThemes = currentState.content.userThemes.filter { it.id != themeId }
            val updatedContent = currentState.content.copy(userThemes = updatedUserThemes)
            
            val isSelectedThemeDeleted = currentState.selectedThemeId == themeId
            val newSelectedThemeId = if (isSelectedThemeDeleted) null else currentState.selectedThemeId
            
            _uiState.value = currentState.copy(content = updatedContent, selectedThemeId = newSelectedThemeId)
            autoSave(updatedContent, currentState.template.id)
        }
    }

    fun updateColor(key: String, hexColor: String) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val updatedColors = currentState.content.colorMap.toMutableMap()
            updatedColors[key] = hexColor
            val updatedContent = currentState.content.copy(colorMap = updatedColors)
            
            val allThemes = currentState.template.themes + currentState.content.userThemes
            val matchingThemeId = allThemes.find { it.colors == updatedColors }?.id
            
            _uiState.value = currentState.copy(content = updatedContent, selectedThemeId = matchingThemeId)
            autoSave(updatedContent, currentState.template.id)
        }
    }

    fun updateTextContent(key: String, value: String) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val currentUsage = currentState.content.usageCount[key] ?: 0
            val updatedUsage = currentState.content.usageCount + (key to currentUsage + 1)
            
            val updatedContent = currentState.content.copy(
                textMap = currentState.content.textMap + (key to value),
                usageCount = updatedUsage
            )
            _uiState.value = currentState.copy(content = updatedContent)
            autoSave(updatedContent, currentState.template.id)
        }
    }

    fun updateImageContent(key: String, url: String) {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            val currentUsage = currentState.content.usageCount[key] ?: 0
            val updatedUsage = currentState.content.usageCount + (key to currentUsage + 1)
            
            val updatedContent = currentState.content.copy(
                imageMap = currentState.content.imageMap + (key to url),
                usageCount = updatedUsage
            )
            _uiState.value = currentState.copy(content = updatedContent)
            autoSave(updatedContent, currentState.template.id)
        }
    }

    private fun autoSave(content: PosterContent, templateId: String) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(300)
            draftRepository.saveDraft(PosterDraft(templateId, content))
        }
    }

    suspend fun saveCurrentDraft() {
        val currentState = _uiState.value
        if (currentState is PreviewUiState.Success) {
            if (currentState.content != originalContent) {
                saveJob?.cancel()
                val draft = PosterDraft(currentState.template.id, currentState.content)
                draftRepository.saveDraft(draft)
                draftRepository.saveTemplateContent(currentState.template.id, currentState.content)
            }
        }
    }

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
                val fileName = "SmartBanner_${currentState.template.id}_$timestamp"
                val result = posterExporter.saveToGallery(bitmap, fileName)
                _exportResult.emit(result)
            }
        }
    }
}
