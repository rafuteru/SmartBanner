package lab.smartbanner.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import lab.smartbanner.model.BannerElement
import lab.smartbanner.model.TextElement
import lab.smartbanner.renderer.PosterRenderer
import lab.smartbanner.utils.toColor

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
                        PosterRenderer(
                            template = template,
                            content = content,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 1. Color Customization Section
                        Text(
                            text = "Colors",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val presetColors = listOf("#FFFFFF", "#FFF8E1", "#EFEBE9", "#8D6E63", "#5D4037", "#000000", "#FFD54F", "#F44336", "#2196F3")

                        // Background Color
                        template.background.contentKey?.let { key ->
                            ColorPickerRow(
                                label = "Background",
                                selectedColor = content.colorMap[key] ?: template.background.color,
                                colors = presetColors,
                                onColorSelected = { viewModel.updateColorContent(key, it) }
                            )
                        }

                        // Banner Colors (Unique Keys)
                        template.elements.filterIsInstance<BannerElement>()
                            .filter { it.colorKey != null }
                            .distinctBy { it.colorKey }
                            .forEach { element ->
                                val key = element.colorKey!!
                                ColorPickerRow(
                                    label = "Banner (${key.replace("_", " ")})",
                                    selectedColor = content.colorMap[key] ?: element.color,
                                    colors = presetColors,
                                    onColorSelected = { viewModel.updateColorContent(key, it) }
                                )
                            }

                        // Text Colors (Unique Keys)
                        template.elements.filterIsInstance<TextElement>()
                            .filter { it.colorKey != null }
                            .distinctBy { it.colorKey }
                            .forEach { element ->
                                val key = element.colorKey!!
                                ColorPickerRow(
                                    label = "Text (${key.replace("_", " ")})",
                                    selectedColor = content.colorMap[key] ?: element.color,
                                    colors = presetColors,
                                    onColorSelected = { viewModel.updateColorContent(key, it) }
                                )
                            }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 2. Text Content Section
                        Text(
                            text = "Content",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

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
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerRow(
    label: String,
    selectedColor: String,
    colors: List<String>,
    onColorSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colors) { hex ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(hex.toColor())
                        .clickable { onColorSelected(hex) }
                        .then(
                            if (hex.lowercase() == selectedColor.lowercase()) {
                                Modifier.background(Color.Black.copy(alpha = 0.2f))
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hex.lowercase() == selectedColor.lowercase()) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
