package lab.smartbanner.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.ImageElement
import lab.smartbanner.model.PosterContent
import lab.smartbanner.model.TextElement
import lab.smartbanner.ui.components.AdBanner
import lab.smartbanner.ui.preview.PreviewUiState
import lab.smartbanner.ui.preview.TemplatePreviewViewModel
import lab.smartbanner.utils.AdConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldsScreen(
    viewModel: TemplatePreviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Local state to hold changes before saving
    var localContent by remember(uiState) {
        mutableStateOf((uiState as? PreviewUiState.Success)?.content ?: PosterContent())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.updateContent(localContent)
                            onBack()
                        }
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        // Tap outside to dismiss keyboard
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            when (val state = uiState) {
                is PreviewUiState.Success -> {
                    val template = state.template

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                focusManager.clearFocus()
                            }
                            .padding(16.dp)
                    ) {
                        // High-visibility Ad placement at the very top
                        AdBanner(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            adUnitId = AdConstants.PREVIEW_BOTTOM_BANNER_ID
                        )

                        // 1. Image Elements Section
                        val imageElements = template.elements
                            .filterIsInstance<ImageElement>()
                            .filter { it.contentKey != null }
                            .distinctBy { it.contentKey }

                        if (imageElements.isNotEmpty()) {
                            Text(
                                "Banner Images",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                            )
                            imageElements.forEach { element ->
                                val key = element.contentKey ?: return@forEach
                                val currentValue = localContent.imageMap[key] ?: element.imageUrl
                                
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { newValue ->
                                        localContent = localContent.copy(
                                            imageMap = localContent.imageMap + (key to newValue)
                                        )
                                    },
                                    label = {
                                        Text(key.replace("_", " ").capitalizeWords())
                                    },
                                    placeholder = { Text("Paste image URL here") },
                                    supportingText = {
                                        Text("Recommended size: ${element.width.toInt()}x${element.height.toInt()}px")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = {
                                        Icon(Icons.Default.Image, contentDescription = null)
                                    },
                                    singleLine = true
                                )
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        }

                        // 2. Text Elements
                        val textElements = template.elements
                            .filterIsInstance<TextElement>()
                            .filter { it.contentKey != null }
                            .distinctBy { it.contentKey }
                            .sortedWith(
                                compareByDescending<TextElement> { localContent.usageCount[it.contentKey] ?: 0 }
                                    .thenByDescending { it.priority }
                                    .thenBy { it.y }
                            )

                        Text(
                            "Text Details", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                        
                        textElements.forEachIndexed { index, element ->
                            val key = element.contentKey ?: return@forEachIndexed
                            val value = localContent.textMap[key] ?: element.text
                            
                            EditTextFieldItem(
                                key = key,
                                currentValue = value,
                                onValueChange = { newValue ->
                                    localContent = localContent.copy(
                                        textMap = localContent.textMap + (key to newValue)
                                    )
                                }
                            )

                            // Inject another ad after 3 items for high engagement
                            if (index == 2 && textElements.size > 4) {
                                AdBanner(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    adUnitId = AdConstants.PREVIEW_BOTTOM_BANNER_ID
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun EditTextFieldItem(
    key: String,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    val isMultilinePreference = key.contains("address", true) ||
            key.contains("footer", true) ||
            key.contains("discount", true) ||
            key.contains("offer", true) ||
            key.contains("exchange", true) ||
            key.contains("message", true) ||
            key.contains("establishment", true)

    OutlinedTextField(
        value = currentValue,
        onValueChange = onValueChange,
        label = {
            Text(key.replace("_", " ").capitalizeWords())
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
            Icon(icon, contentDescription = null)
        },
        minLines = if (isMultilinePreference) 3 else 1,
        maxLines = 10,
        singleLine = false
    )
}

fun String.capitalizeWords(): String = 
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
