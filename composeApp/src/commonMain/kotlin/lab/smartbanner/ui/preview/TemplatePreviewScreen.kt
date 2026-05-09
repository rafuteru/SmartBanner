package lab.smartbanner.ui.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lab.smartbanner.model.TextElement
import lab.smartbanner.renderer.PosterRenderer

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
                title = {
                    val title = (uiState as? PreviewUiState.Success)?.template?.name ?: "Edit Poster"
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is PreviewUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PreviewUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is PreviewUiState.Success -> {
                    val template = state.template
                    val content = state.content
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Live Preview Section
                        PosterRenderer(
                            template = template,
                            content = content,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 2. Editor Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Edit Content",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Generate fields based on template content keys
                                template.elements
                                    .filterIsInstance<TextElement>()
                                    .filter { it.contentKey != null }
                                    .distinctBy { it.contentKey }
                                    .forEach { element ->
                                        val key = element.contentKey!!
                                        val currentValue = content.textMap[key] ?: element.text
                                        
                                        OutlinedTextField(
                                            value = currentValue,
                                            onValueChange = { viewModel.updateTextContent(key, it) },
                                            label = { Text(key.replace("_", " ").uppercase()) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            leadingIcon = {
                                                val icon = when {
                                                    key.contains("rate") -> Icons.Default.CurrencyRupee
                                                    key.contains("shop") -> Icons.Default.Store
                                                    else -> Icons.Default.Edit
                                                }
                                                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                                            },
                                            singleLine = !key.contains("address")
                                        )
                                    }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
