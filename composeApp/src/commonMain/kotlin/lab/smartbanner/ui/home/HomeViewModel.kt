package lab.smartbanner.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.PosterDraft
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterTemplate

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val templates: List<PosterTemplate>,
        val categories: List<String>,
        val selectedCategory: String = "All",
        val latestDraft: PosterDraft? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val repository: TemplateRepository,
    private val draftRepository: DraftRepository
) : ViewModel() {

    var uiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    val latestDraft: StateFlow<PosterDraft?> = draftRepository.getLatestDraft()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            uiState = HomeUiState.Loading
            try {
                val templates = repository.getTemplates()
                val categories = listOf("All") + templates.map { it.category }.distinct()
                
                // We'll update the Success state with the draft when it's collected in the UI
                // or we can observe the flow here.
                uiState = HomeUiState.Success(
                    templates = templates,
                    categories = categories
                )
            } catch (e: Exception) {
                uiState = HomeUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun selectCategory(category: String) {
        val currentState = uiState
        if (currentState is HomeUiState.Success) {
            uiState = currentState.copy(selectedCategory = category)
        }
    }

    fun clearDraft() {
        viewModelScope.launch {
            draftRepository.clearDraft()
        }
    }
}
