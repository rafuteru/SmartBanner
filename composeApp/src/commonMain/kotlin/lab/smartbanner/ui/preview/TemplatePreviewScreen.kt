package lab.smartbanner.ui.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.smartbanner.model.BannerElement
import lab.smartbanner.model.ImageElement
import lab.smartbanner.model.TextElement
import lab.smartbanner.ui.components.DynamicBanner
import lab.smartbanner.ui.components.DynamicImage
import lab.smartbanner.ui.components.DynamicText
import lab.smartbanner.ui.components.PosterCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePreviewScreen(
    templateId: String,
    viewModel: TemplatePreviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Template Preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is PreviewUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is PreviewUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
                is PreviewUiState.Success -> {
                    val template = state.template
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PosterCanvas(
                            template = template,
                            modifier = Modifier.weight(1f)
                        ) { scale ->
                            // Sort elements by zIndex to ensure correct layering
                            template.elements.sortedBy { it.zIndex }.forEach { element ->
                                when (element) {
                                    is TextElement -> {
                                        DynamicText(
                                            element = element,
                                            scale = scale
                                        )
                                    }
                                    is ImageElement -> {
                                        DynamicImage(
                                            element = element,
                                            scale = scale
                                        )
                                    }
                                    is BannerElement -> {
                                        DynamicBanner(
                                            element = element,
                                            scale = scale
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Previewing: ${template.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
