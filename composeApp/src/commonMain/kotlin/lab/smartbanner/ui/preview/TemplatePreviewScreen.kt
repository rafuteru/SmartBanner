package lab.smartbanner.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import lab.smartbanner.getPlatform
import lab.smartbanner.model.PosterTheme
import lab.smartbanner.renderer.PosterRenderer
import lab.smartbanner.ui.components.StyledAdBanner
import lab.smartbanner.utils.AdConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePreviewScreen(
    templateId: String,
    viewModel: TemplatePreviewViewModel,
    onBack: () -> Unit,
    onEditFields: (String) -> Unit,
    onCreateTheme: (String) -> Unit,
    onEditTheme: (String, String) -> Unit // themeId, templateId
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.exportResult.collect { result ->
            if (result.isSuccess) {
                snackbarHostState.showSnackbar("Poster saved to gallery!")
            } else {
                snackbarHostState.showSnackbar("Failed to export: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    val state = uiState as? PreviewUiState.Success
    val isLocked = state?.isLocked ?: false
    val isTemporarilyUnlocked = state?.isTemporarilyUnlocked ?: false
    val effectivelyLocked = isLocked && !isTemporarilyUnlocked

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val title = state?.template?.name ?: "Preview"
                    Text(title, fontWeight = FontWeight.Bold)
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
                    if (!effectivelyLocked) {
                        IconButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset to Default")
                        }
                        IconButton(
                            onClick = {
                                // Trigger Interstitial Ad before Exporting
                                getPlatform().showInterstitialAd(AdConstants.INTERSTITIAL_AD_ID) {
                                    scope.launch {
                                        try {
                                            val bitmap = graphicsLayer.toImageBitmap()
                                            viewModel.exportPoster(bitmap)
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Export failed: ${e.message}")
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Export")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state != null && !effectivelyLocked) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { onEditFields(templateId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Edit Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val s = uiState) {
                is PreviewUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PreviewUiState.Error -> {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                }
                is PreviewUiState.Success -> {
                    PreviewContent(
                        templateId = templateId,
                        state = s,
                        viewModel = viewModel,
                        graphicsLayer = graphicsLayer,
                        onCreateTheme = onCreateTheme,
                        onEditTheme = onEditTheme
                    )
                }
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Template") },
                text = { Text("Are you sure you want to discard all changes and reset to the original template?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                            viewModel.resetToDefault()
                        }
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewContent(
    templateId: String,
    state: PreviewUiState.Success,
    viewModel: TemplatePreviewViewModel,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    onCreateTheme: (String) -> Unit,
    onEditTheme: (String, String) -> Unit
) {
    val template = state.template
    val content = state.content
    val isLocked = state.isLocked
    val isTemporarilyUnlocked = state.isTemporarilyUnlocked
    val effectivelyLocked = isLocked && !isTemporarilyUnlocked
    
    val scrollState = rememberScrollState()

    // Use intrinsicHeight for calculating aspect ratio to support dynamic content
    val posterAspectRatio = remember(template.width, template.intrinsicHeight) {
        template.width / template.intrinsicHeight
    }

    var themeToDelete by remember { mutableStateOf<PosterTheme?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(posterAspectRatio)
                .shadow(8.dp)
                .background(Color.White)
                .drawWithContent {
                    if (!effectivelyLocked) {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                    }
                    drawContent()
                },
            contentAlignment = Alignment.Center
        ) {
            PosterRenderer(
                template = template,
                content = content,
                modifier = Modifier.fillMaxSize()
            )

            if (effectivelyLocked) {
                // Watermark Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        repeat(5) {
                            Text(
                                text = "PREMIUM TEMPLATE",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.Gray.copy(alpha = 0.2f),
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .rotate(-45f)
                                    .padding(vertical = 40.dp)
                            )
                        }
                    }
                    
                    // Locked Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        // Use the SECOND Ad Unit ID here
        StyledAdBanner(
            modifier = Modifier.fillMaxWidth(),
            adUnitId = AdConstants.PREVIEW_BOTTOM_BANNER_ID
        )
        
        // Theme Selection
        if (template.themes.isNotEmpty() || (!effectivelyLocked && content.userThemes.isNotEmpty())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Themes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (!effectivelyLocked) {
                    TextButton(onClick = { onCreateTheme(templateId) }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Theme")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                // Predefined Themes (Not editable)
                items(template.themes) { theme ->
                    val isSelected = state.selectedThemeId == theme.id
                    ThemeItem(
                        name = theme.name,
                        swatchColor = theme.colors["background"] ?: theme.colors["primary"] ?: "#000000",
                        isSelected = isSelected,
                        onClick = { viewModel.applyTheme(theme) }
                    )
                }
                
                // User Custom Themes (Editable & Deletable) - Only if not locked
                if (!effectivelyLocked) {
                    items(content.userThemes) { theme ->
                        val isSelected = state.selectedThemeId == theme.id
                        ThemeItem(
                            name = theme.name,
                            swatchColor = theme.colors["background"] ?: theme.colors["primary"] ?: "#000000",
                            isSelected = isSelected,
                            onClick = { viewModel.applyTheme(theme) },
                            onEdit = { onEditTheme(theme.id, templateId) },
                            onDelete = { themeToDelete = theme }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (effectivelyLocked) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Premium Template",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This template is exclusive to our registered customers. Contact support to unlock this and many other premium designs.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    // Unlock Button (Purchase Flow / Contact Support)
                    Button(
                        onClick = { viewModel.contactSupportForLockedTemplate(template.name) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Contact Support to Unlock")
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Watch Ad Option
                    OutlinedButton(
                        onClick = {
                            getPlatform().showRewardedAd(AdConstants.REWARDED_AD_ID) {
                                viewModel.unlockTemporarily()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Watch Ad to Unlock for 1 Hour")
                    }
                }
            }
        }
    }

    if (themeToDelete != null) {
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = { Text("Delete Theme") },
            text = { Text("Are you sure you want to delete the theme \"${themeToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        themeToDelete?.let { viewModel.deleteUserTheme(it.id) }
                        themeToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeItem(
    name: String,
    swatchColor: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(parseColor(swatchColor)))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isColorDark(swatchColor)) Color.White else Color.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        
        if (onEdit != null || onDelete != null) {
            Row {
                if (onEdit != null) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

fun parseColor(colorString: String): Long {
    if (colorString.startsWith("#")) {
        val hex = colorString.substring(1)
        return when (hex.length) {
            6 -> 0xFF000000 or hex.toLong(16)
            8 -> hex.toLong(16)
            else -> 0xFF000000
        }
    }
    return 0xFF000000
}

fun isColorDark(colorString: String): Boolean {
    val color = parseColor(colorString)
    val r = (color shr 16 and 0xFF).toInt()
    val g = (color shr 8 and 0xFF).toInt()
    val b = (color and 0xFF).toInt()
    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
    return luminance < 0.5
}
