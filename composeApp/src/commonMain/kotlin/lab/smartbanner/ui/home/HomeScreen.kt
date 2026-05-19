package lab.smartbanner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.PosterContent
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.renderer.PosterRenderer
import lab.smartbanner.ui.components.AdBanner
import lab.smartbanner.utils.AdConstants
import lab.smartbanner.utils.contactSupport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPreview: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState = viewModel.uiState
    var showIdDialog by remember { mutableStateOf(false) }
    var isCodeVisible by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val maskedCode = remember(viewModel.currentIdentifier) {
        if (viewModel.currentIdentifier.length > 4) {
            "*".repeat(viewModel.currentIdentifier.length - 4) + viewModel.currentIdentifier.takeLast(4)
        } else {
            "*".repeat(viewModel.currentIdentifier.length)
        }
    }

    if (viewModel.showInitialAccessCodeDialog) {
        InitialAccessCodeDialog(
            accessCode = viewModel.currentIdentifier,
            onDismiss = { viewModel.dismissInitialDialog() },
            onCopy = {
                clipboardManager.setText(AnnotatedString(viewModel.currentIdentifier))
                viewModel.dismissInitialDialog()
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showIdDialog = true }
                    ) {
                        Text(
                            "SmartBanner",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        if (viewModel.currentIdentifier.isNotBlank()) {
                            Text(
                                text = "Access Code: $maskedCode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    contactSupport(
                        subject = "Custom Design Request",
                        body = "Hi, I would like to request a custom theme for SmartBanner. [Send your design we will add a theme for you.]",
                        accessCode = viewModel.currentIdentifier
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Email, contentDescription = null) },
                text = { Text("Request Custom Design") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }
                is HomeUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadTemplates(forceRefresh = true) }
                    )
                }
                is HomeUiState.Success -> {
                    HomeContent(
                        state = state,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onTemplateClick = { template -> onNavigateToPreview(template.id) },
                        onRetry = { viewModel.loadTemplates(forceRefresh = true) }
                    )
                }
            }
        }
    }

    if (showIdDialog) {
        AlertDialog(
            onDismissRequest = { showIdDialog = false },
            title = { Text("Access Code") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Please save your ID for future use. Tap below to copy your Access Code. Share it with our team for custom design requests.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (viewModel.currentIdentifier.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable { isCodeVisible = !isCodeVisible }
                        ) {
                            Text(
                                text = "Code: " + if (isCodeVisible) viewModel.currentIdentifier else maskedCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isCodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isCodeVisible) "Hide" else "Show",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        Text(
                            text = "Code: NA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        val code = viewModel.currentIdentifier
                        showIdDialog = false
                        clipboardManager.setText(AnnotatedString(code))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Access Code")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIdDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun InitialAccessCodeDialog(
    accessCode: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Welcome to SmartBanner", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Your unique Access Code is generated. Please save it for future use.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = accessCode,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "If you lose this code, your previous templates and customizations could be lost.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCopy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy & Save Code")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I've saved it")
            }
        }
    )
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onCategorySelect: (String) -> Unit,
    onTemplateClick: (PosterTemplate) -> Unit,
    onRetry: () -> Unit
) {
    val filteredTemplates = remember(state.selectedCategory, state.templates) {
        if (state.selectedCategory == "All") {
            state.templates
        } else {
            state.templates.filter { it.category == state.selectedCategory }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.categories) { category ->
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        if (filteredTemplates.isEmpty()) {
            EmptyState(category = state.selectedCategory, onRetry = onRetry)
        } else {
            val grouped = remember(filteredTemplates) {
                filteredTemplates.groupBy { it.category }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                grouped.forEach { (category, templates) ->
                    if (state.selectedCategory == "All") {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    itemsIndexed(templates) { index, template ->
                        if (index > 0 && index % 4 == 0) {
                            // Inject Ad occasionally
                            // Using a key or item to avoid issues in grid
                        }
                        
                        TemplateItem(
                            template = template,
                            onClick = { onTemplateClick(template) }
                        )
                    }
                }
                
                // Add an ad at the very end or fixed position if needed
                item(span = { GridItemSpan(2) }) {
                    AdItemContent()
                }
            }
        }
    }
}

@Composable
private fun AdItemContent() {
    AdBanner(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        adUnitId = AdConstants.HOME_GRID_BANNER_ID
    )
}

@Composable
private fun EmptyState(category: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No templates found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (category == "All") "We couldn\u0027t load any templates. Check your connection." 
                   else "We couldn\u0027t find any templates in \u0027$category\u0027 category.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
fun TemplateItem(
    template: PosterTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .padding(8.dp)
                        .shadow(4.dp, RoundedCornerShape(2.dp))
                        .clip(RoundedCornerShape(2.dp))
                ) {
                    PosterRenderer(
                        template = template,
                        content = PosterContent(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = template.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!template.config.isFree) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Info, // Placeholder for "locked" or similar
                            contentDescription = "Premium",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
