package lab.smartbanner

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import lab.smartbanner.data.DataStoreDraftRepository
import lab.smartbanner.data.LocalTemplateRepository
import lab.smartbanner.domain.PosterDraft
import lab.smartbanner.navigation.Screen
import lab.smartbanner.ui.home.HomeScreen
import lab.smartbanner.ui.home.HomeViewModel
import lab.smartbanner.ui.preview.TemplatePreviewScreen
import lab.smartbanner.ui.preview.TemplatePreviewViewModel
import lab.smartbanner.ui.theme.PosterWalaTheme
import lab.smartbanner.utils.createDataStore
import lab.smartbanner.utils.createPosterExporter

@Composable
@Preview
fun App() {
    PosterWalaTheme {
        val navController = rememberNavController()
        
        // Dependency Injection (Manual for now, keeping it KMP-ready)
        val context = LocalContext.current
        val dataStore = remember { createDataStore(context) }
        val templateRepository = remember { LocalTemplateRepository() }
        val draftRepository = remember { DataStoreDraftRepository(dataStore) }
        val posterExporter = remember { createPosterExporter(context) }

        NavHost(
            navController = navController,
            startDestination = Screen.Home
        ) {
            composable<Screen.Home> {
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(templateRepository, draftRepository)
                }
                
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToPreview = { templateId ->
                        navController.navigate(Screen.TemplatePreview(templateId))
                    },
                    onResumeDraft = { draft: PosterDraft ->
                        navController.navigate(Screen.TemplatePreview(draft.templateId))
                    }
                )
            }
            
            composable<Screen.TemplatePreview> { backStackEntry ->
                val route: Screen.TemplatePreview = backStackEntry.toRoute()
                val previewViewModel: TemplatePreviewViewModel = viewModel {
                    TemplatePreviewViewModel(templateRepository, draftRepository, posterExporter)
                }
                
                // Only trigger load once when entering the screen
                LaunchedEffect(route.templateId) {
                    previewViewModel.loadTemplate(route.templateId)
                }

                TemplatePreviewScreen(
                    templateId = route.templateId,
                    viewModel = previewViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
