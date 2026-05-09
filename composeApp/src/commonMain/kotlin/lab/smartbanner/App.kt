package lab.smartbanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import lab.smartbanner.data.LocalTemplateRepository
import lab.smartbanner.navigation.Screen
import lab.smartbanner.ui.home.HomeScreen
import lab.smartbanner.ui.home.HomeViewModel
import lab.smartbanner.ui.preview.TemplatePreviewScreen
import lab.smartbanner.ui.preview.TemplatePreviewViewModel
import lab.smartbanner.ui.theme.PosterWalaTheme

@Composable
@Preview
fun App() {
    PosterWalaTheme {
        val navController = rememberNavController()
        
        NavHost(
            navController = navController,
            startDestination = Screen.Home
        ) {
            composable<Screen.Home> {
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(LocalTemplateRepository())
                }
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToPreview = { templateId ->
                        navController.navigate(Screen.TemplatePreview(templateId))
                    }
                )
            }
            
            composable<Screen.TemplatePreview> { backStackEntry ->
                val route: Screen.TemplatePreview = backStackEntry.toRoute()
                val previewViewModel: TemplatePreviewViewModel = viewModel {
                    TemplatePreviewViewModel(LocalTemplateRepository())
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
