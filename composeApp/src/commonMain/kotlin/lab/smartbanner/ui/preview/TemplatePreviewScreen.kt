package lab.smartbanner.ui.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.TextElement
import lab.smartbanner.renderer.PosterRenderer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePreviewScreen(
    templateId: String,
    viewModel: TemplatePreviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()

    // Handle export results
    LaunchedEffect(Unit) {
        viewModel.exportResult.collect { result ->
            if (result.isSuccess) {
                snackbarHostState.showSnackbar("Poster saved to gallery!")
            } else {
                snackbarHostState.showSnackbar("Failed to export: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val title =
                        (uiState as? PreviewUiState.Success)?.template?.name ?: "Edit Poster"
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.saveCurrentDraft()
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Export Button
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val bitmap = graphicsLayer.toImageBitmap()
                                    viewModel.exportPoster(bitmap)
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Export failed: ${e.message}")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.completeEditing()
                                onBack()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Done")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PreviewUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }

                is PreviewUiState.Error -> {
                    PreviewErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadTemplate(templateId) })
                }

                is PreviewUiState.Success -> {
                    PreviewEditorContent(
                        state = state,
                        viewModel = viewModel,
                        graphicsLayer = graphicsLayer
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewEditorContent(
    state: PreviewUiState.Success,
    viewModel: TemplatePreviewViewModel,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer
) {
    val template = state.template
    val content = state.content
    val scrollState = rememberScrollState()

    val posterAspectRatio = remember(template.width, template.height) {
        template.width / template.height
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            )
    ) {

        // Poster Preview Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Using a Box with shadow and explicit border for clear boundaries
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(posterAspectRatio)
                    .shadow(8.dp, RoundedCornerShape(0.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(0.dp))
                    .drawWithContent {
                        // Capture logic
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawContent()
                    }
            ) {
                PosterRenderer(
                    template = template,
                    content = content,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }


        // Editor Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 32.dp,
                topEnd = 32.dp
            ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .fillMaxWidth()
            ) {

                // Drag Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader(
                    title = "Poster Content",
                    icon = Icons.Default.TextFields
                )

                Spacer(modifier = Modifier.height(20.dp))

                template.elements
                    .filterIsInstance<TextElement>()
                    .filter { it.contentKey != null }
                    .distinctBy { it.contentKey }
                    .forEach { element ->

                        val key = element.contentKey!!
                        val currentValue =
                            content.textMap[key] ?: element.text

                        // Identify fields that typically contain multiline/long content
                        val isMultilinePreference = key.contains("address", true) || 
                                                  key.contains("footer", true) || 
                                                  key.contains("discount", true) || 
                                                  key.contains("offer", true) ||
                                                  key.contains("exchange", true) ||
                                                  key.contains("message", true)

                        OutlinedTextField(
                            value = currentValue,
                            onValueChange = {
                                viewModel.updateTextContent(
                                    key,
                                    it
                                )
                            },
                            label = {
                                Text(
                                    key
                                        .replace("_", " ")
                                        .replaceFirstChar { c ->
                                            c.uppercase()
                                        }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),

                            leadingIcon = {
                                val icon = when {
                                    key.contains("offer", true) || 
                                    key.contains("rate", true) || 
                                    key.contains("discount", true) -> Icons.Default.LocalOffer
                                    
                                    key.contains("shop", true) -> Icons.Default.Store
                                    key.contains("address", true) || key.contains("footer", true) -> Icons.Default.LocationOn
                                    key.contains("owner", true) || key.contains("phone", true) -> Icons.Default.Person
                                    else -> Icons.Default.Edit
                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },

                            // Ensure multiline is supported for all fields, with extra space for long ones
                            minLines = if (isMultilinePreference) 3 else 1,
                            maxLines = 10,
                            singleLine = false
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
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.5.sp
        )
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
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
