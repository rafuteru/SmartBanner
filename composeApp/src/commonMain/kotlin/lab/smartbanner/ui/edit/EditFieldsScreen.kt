package lab.smartbanner.ui.edit

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.ImageElement
import lab.smartbanner.model.TextElement
import lab.smartbanner.ui.preview.PreviewUiState
import lab.smartbanner.ui.preview.TemplatePreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldsScreen(
    viewModel: TemplatePreviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PreviewUiState.Success -> {
                    val template = state.template
                    val content = state.content

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
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
                                val key = element.contentKey!!
                                val currentValue = content.imageMap[key] ?: element.imageUrl
                                
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { viewModel.updateImageContent(key, it) },
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

                        // 2. Text Elements Logic (Most used first)
                        val textElements = template.elements
                            .filterIsInstance<TextElement>()
                            .filter { it.contentKey != null }
                            .distinctBy { it.contentKey }
                            .sortedWith(
                                compareByDescending<TextElement> { content.usageCount[it.contentKey] ?: 0 }
                                    .thenByDescending { it.priority }
                                    .thenBy { it.y }
                            )

                        val frequentlyUsed = textElements.takeWhile { (content.usageCount[it.contentKey] ?: 0) > 0 }.take(5)
                        val remainingText = textElements.filterNot { frequentlyUsed.contains(it) }

                        if (frequentlyUsed.isNotEmpty()) {
                            Text(
                                "Frequently Used", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                            )
                            frequentlyUsed.forEach { element ->
                                EditTextFieldItem(element, content.textMap[element.contentKey!!] ?: element.text, viewModel)
                            }
                            
                            if (remainingText.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                            }
                        }

                        if (remainingText.isNotEmpty()) {
                            Text(
                                if (frequentlyUsed.isEmpty()) "Text Details" else "Other Fields", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                            )
                            remainingText.forEach { element ->
                                EditTextFieldItem(element, content.textMap[element.contentKey!!] ?: element.text, viewModel)
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
    element: TextElement,
    currentValue: String,
    viewModel: TemplatePreviewViewModel
) {
    val key = element.contentKey!!
    val isMultilinePreference = key.contains("address", true) ||
            key.contains("footer", true) ||
            key.contains("discount", true) ||
            key.contains("offer", true) ||
            key.contains("exchange", true) ||
            key.contains("message", true)

    OutlinedTextField(
        value = currentValue,
        onValueChange = { viewModel.updateTextContent(key, it) },
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
