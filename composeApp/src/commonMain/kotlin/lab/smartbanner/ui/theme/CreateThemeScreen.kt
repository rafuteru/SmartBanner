package lab.smartbanner.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.PosterTheme
import lab.smartbanner.renderer.PosterRenderer
import lab.smartbanner.ui.components.AdBanner
import lab.smartbanner.ui.preview.PreviewUiState
import lab.smartbanner.ui.preview.TemplatePreviewViewModel
import lab.smartbanner.ui.preview.parseColor
import lab.smartbanner.utils.AdConstants
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateThemeScreen(
    viewModel: TemplatePreviewViewModel,
    onBack: () -> Unit,
    editThemeId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var themeName by remember { mutableStateOf("") }
    val selectedColors = remember { mutableStateMapOf<String, String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Initialize data for editing or creation
    LaunchedEffect(uiState, editThemeId) {
        val state = uiState
        if (state is PreviewUiState.Success && selectedColors.isEmpty()) {
            val existingTheme = if (editThemeId != null) {
                state.content.userThemes.find { it.id == editThemeId }
            } else {
                null
            }

            if (existingTheme != null) {
                themeName = existingTheme.name
                selectedColors.putAll(existingTheme.colors)
            } else {
                // Default to first template theme or collect keys
                state.template.themes.firstOrNull()?.colors?.let {
                    selectedColors.putAll(it)
                } ?: run {
                    val keys = mutableSetOf<String>()
                    state.template.background.colorKey?.let { keys.add(it) }
                    state.template.elements.forEach {
                        when (it) {
                            is lab.smartbanner.model.TextElement -> {
                                it.colorKey?.let { keys.add(it) }
                                it.strokeColorKey?.let { keys.add(it) }
                            }
                            is lab.smartbanner.model.BannerElement -> {
                                it.colorKey?.let { keys.add(it) }
                                it.borderColorKey?.let { keys.add(it) }
                            }
                            is lab.smartbanner.model.ImageElement -> {
                                it.borderColorKey?.let { keys.add(it) }
                            }
                        }
                    }
                    keys.forEach { selectedColors[it] = "#000000" }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editThemeId != null) "Edit Theme" else "New Theme", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editThemeId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Button(
                        onClick = {
                            if (themeName.isNotBlank()) {
                                val newTheme = PosterTheme(
                                    id = editThemeId ?: "user_${Clock.System.now().toEpochMilliseconds()}",
                                    name = themeName,
                                    colors = selectedColors.toMap()
                                )
                                viewModel.addUserTheme(newTheme)
                                onBack()
                            }
                        },
                        enabled = themeName.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        val state = uiState
        if (state is PreviewUiState.Success) {
            val template = state.template
            // Create a temporary content to show live preview of theme changes
            val previewContent = state.content.copy(colorMap = selectedColors.toMap())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Poster Preview
                val posterAspectRatio = remember(template.width, template.height) {
                    template.width.toFloat() / template.height.toFloat()
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(posterAspectRatio),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    PosterRenderer(
                        template = template,
                        content = previewContent,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Theme Name
                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text("Theme Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // High-visibility Ad placement
                AdBanner(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    adUnitId = AdConstants.PREVIEW_BOTTOM_BANNER_ID
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Theme Colors", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    "Tap on a color circle to customize", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 3. Color Selection List
                val colorKeys = remember(template) {
                    val keys = mutableSetOf<String>()
                    template.background.colorKey?.let { keys.add(it) }
                    template.elements.forEach {
                        when (it) {
                            is lab.smartbanner.model.TextElement -> {
                                it.colorKey?.let { keys.add(it) }
                                it.strokeColorKey?.let { keys.add(it) }
                            }
                            is lab.smartbanner.model.BannerElement -> {
                                it.colorKey?.let { keys.add(it) }
                                it.borderColorKey?.let { keys.add(it) }
                            }
                            is lab.smartbanner.model.ImageElement -> {
                                it.borderColorKey?.let { keys.add(it) }
                            }
                        }
                    }
                    keys.toList().sorted()
                }

                colorKeys.forEach { key ->
                    ColorRow(
                        label = key.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        currentColor = selectedColors[key] ?: "#000000",
                        onColorSelected = { selectedColors[key] = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }

            if (showDeleteDialog && editThemeId != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Theme") },
                    text = { Text("Are you sure you want to delete this custom theme?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            viewModel.deleteUserTheme(editThemeId)
                            onBack()
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ColorRow(
    label: String,
    currentColor: String,
    onColorSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(parseColor(currentColor)))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }

    if (showDialog) {
        SimpleColorPicker(
            initialColor = currentColor,
            onDismiss = { showDialog = false },
            onColorPicked = {
                onColorSelected(it)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleColorPicker(
    initialColor: String,
    onDismiss: () -> Unit,
    onColorPicked: (String) -> Unit
) {
    val basicColors = listOf(
        "#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF",
        "#FFFF00", "#00FFFF", "#FF00FF", "#808080", "#2437A6",
        "#D62828", "#FFE600", "#7A1F1F", "#A52A2A", "#4CAF50",
        "#FF9800", "#9C27B0", "#E91E63", "#00BCD4", "#607D8B"
    )

    var customHex by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick a Color") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    basicColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(parseColor(hex)))
                                .border(
                                    width = if (hex.lowercase() == initialColor.lowercase()) 3.dp else 1.dp,
                                    color = if (hex.lowercase() == initialColor.lowercase()) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { onColorPicked(hex) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = customHex,
                    onValueChange = { customHex = it },
                    label = { Text("Hex Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                
                Text(
                    "Format: #RRGGBB", 
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onColorPicked(customHex) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
