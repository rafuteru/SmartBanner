package lab.smartbanner

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.navigation.Screen
import lab.smartbanner.ui.edit.EditFieldsScreen
import lab.smartbanner.ui.home.HomeScreen
import lab.smartbanner.ui.home.HomeViewModel
import lab.smartbanner.ui.preview.TemplatePreviewScreen
import lab.smartbanner.ui.preview.TemplatePreviewViewModel
import lab.smartbanner.ui.theme.CreateThemeScreen
import lab.smartbanner.ui.theme.PosterWalaTheme
import lab.smartbanner.utils.PosterExporter

@Composable
fun App(
    templateRepository: TemplateRepository,
    draftRepository: DraftRepository,
    posterExporter: PosterExporter
) {
    PosterWalaTheme {
        val navController = rememberNavController()

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
                    onNavigateToPreview = { id ->
                        navController.navigate(Screen.TemplatePreview(templateId = id))
                    }
                )
            }
            
            composable<Screen.TemplatePreview> { backStackEntry ->
                val route: Screen.TemplatePreview = backStackEntry.toRoute()
                val previewViewModel: TemplatePreviewViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry
                ) {
                    TemplatePreviewViewModel(templateRepository, draftRepository, posterExporter)
                }
                
                LaunchedEffect(route.templateId) {
                    previewViewModel.loadTemplate(route.templateId)
                }

                TemplatePreviewScreen(
                    templateId = route.templateId,
                    viewModel = previewViewModel,
                    onBack = { navController.popBackStack() },
                    onEditFields = { id ->
                        navController.navigate(Screen.EditFields(templateId = id))
                    },
                    onCreateTheme = { id ->
                        navController.navigate(Screen.CreateTheme(templateId = id))
                    },
                    onEditTheme = { themeId, templateId ->
                        navController.navigate(Screen.CreateTheme(templateId = templateId, themeId = themeId))
                    }
                )
            }

            composable<Screen.EditFields> { backStackEntry ->
                val route: Screen.EditFields = backStackEntry.toRoute()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Screen.TemplatePreview>()
                }
                val previewViewModel: TemplatePreviewViewModel = viewModel(
                    viewModelStoreOwner = parentEntry
                )

                EditFieldsScreen(
                    viewModel = previewViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.CreateTheme> { backStackEntry ->
                val route: Screen.CreateTheme = backStackEntry.toRoute()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Screen.TemplatePreview>()
                }
                val previewViewModel: TemplatePreviewViewModel = viewModel(
                    viewModelStoreOwner = parentEntry
                )

                CreateThemeScreen(
                    viewModel = previewViewModel,
                    onBack = { navController.popBackStack() },
                    editThemeId = route.themeId
                )
            }
        }
    }
}
