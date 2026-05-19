package lab.smartbanner.ui.preview

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.PosterTheme
import lab.smartbanner.renderer.PosterRenderer
import lab.smartbanner.ui.theme.SimpleColorPicker
import lab.smartbanner.ui.components.AdBanner
import lab.smartbanner.utils.AdConstants
import lab.smartbanner.getPlatform
import kotlinx.coroutines.launch

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
        AdBanner(
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
                        Text("Watch Ad to Use Once")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Delete Confirmation Dialog
    themeToDelete?.let { theme ->
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = { Text("Delete Theme") },
            text = { Text("Are you sure you want to delete \u0027${theme.name}\u0027?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToDelete = theme.id
                        themeToDelete = null
                        viewModel.deleteUserTheme(idToDelete)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
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
fun ThemeItem(
    name: String,
    swatchColor: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(parseColor(swatchColor)))
                .border(
                    width = if (isSelected) 3.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            } else if (isSelected) {
                Box(
                    modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            name, 
            fontSize = 11.sp, 
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        // Show edit/delete options for user themes
        if (onEdit != null || onDelete != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                onEdit?.let {
                    IconButton(onClick = it, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                onDelete?.let {
                    IconButton(onClick = it, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

fun parseColor(hex: String): Int {
    return try {
        if (hex.startsWith("#")) {
            val h = hex.substring(1)
            if (h.length == 6) {
                (0xFF000000 or h.toLong(16)).toInt()
            } else if (h.length == 8) {
                h.toLong(16).toInt()
            } else {
                0xFF000000.toInt()
            }
        } else {
            0xFF000000.toInt()
        }
    } catch (e: Exception) {
        0xFF000000.toInt()
    }
}
