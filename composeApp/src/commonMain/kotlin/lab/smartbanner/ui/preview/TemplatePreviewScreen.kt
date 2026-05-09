package lab.smartbanner.ui.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.BannerElement
import lab.smartbanner.model.TextElement
import lab.smartbanner.renderer.PosterRenderer
import lab.smartbanner.utils.toColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Placeholder for Save/Export */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "PreviewContentTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { state ->
            when (state) {
                is PreviewUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }
                is PreviewUiState.Error -> {
                    PreviewErrorState(message = state.message, onRetry = { viewModel.loadTemplate(templateId) })
                }
                is PreviewUiState.Success -> {
                    PreviewEditorContent(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewEditorContent(
    state: PreviewUiState.Success,
    viewModel: TemplatePreviewViewModel
) {
    val template = state.template
    val content = state.content
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Poster Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                PosterRenderer(
                    template = template,
                    content = content,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Editor Controls
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Colors Section
                SectionHeader("Visual Style", Icons.Default.Palette)
                Spacer(modifier = Modifier.height(16.dp))

                val presetColors = listOf("#FFFFFF", "#FFF8E1", "#EFEBE9", "#8D6E63", "#5D4037", "#000000", "#FFD54F", "#F44336", "#2196F3", "#4CAF50")

                // Background Color
                template.background.contentKey?.let { key ->
                    ColorPickerRow(
                        label = "Background",
                        selectedColor = content.colorMap[key] ?: template.background.color,
                        colors = presetColors,
                        onColorSelected = { viewModel.updateColorContent(key, it) }
                    )
                }

                // Banner Colors
                template.elements.filterIsInstance<BannerElement>()
                    .filter { it.colorKey != null }
                    .distinctBy { it.colorKey }
                    .forEach { element ->
                        val key = element.colorKey!!
                        ColorPickerRow(
                            label = key.replace("_", " ").capitalizeWords(),
                            selectedColor = content.colorMap[key] ?: element.color,
                            colors = presetColors,
                            onColorSelected = { viewModel.updateColorContent(key, it) }
                        )
                    }

                // Text Colors
                template.elements.filterIsInstance<TextElement>()
                    .filter { it.colorKey != null }
                    .distinctBy { it.colorKey }
                    .forEach { element ->
                        val key = element.colorKey!!
                        ColorPickerRow(
                            label = "${key.replace("_", " ").capitalizeWords()} Color",
                            selectedColor = content.colorMap[key] ?: element.color,
                            colors = presetColors,
                            onColorSelected = { viewModel.updateColorContent(key, it) }
                        )
                    }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Content Section
                SectionHeader("Text Content", Icons.Default.TextFields)
                Spacer(modifier = Modifier.height(16.dp))

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
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            leadingIcon = {
                                val icon = when {
                                    key.contains("rate") -> Icons.Default.CurrencyRupee
                                    key.contains("shop") || key.contains("brand") -> Icons.Default.Store
                                    key.contains("address") -> Icons.Default.LocationOn
                                    else -> Icons.Default.Edit
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = !key.contains("address")
                        )
                    }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ColorPickerRow(
    label: String,
    selectedColor: String,
    colors: List<String>,
    onColorSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(colors) { hex ->
                val isSelected = hex.lowercase() == selectedColor.lowercase()
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(hex.toColor())
                        .clickable { onColorSelected(hex) }
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check, 
                            contentDescription = null, 
                            tint = if (hex.lowercase() == "#ffffff") Color.Black else Color.White, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
