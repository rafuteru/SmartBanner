package lab.smartbanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import lab.smartbanner.navigation.Screen
import lab.smartbanner.ui.home.HomeScreen
import lab.smartbanner.ui.preview.TemplatePreviewScreen
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
                HomeScreen(
                    onNavigateToPreview = { templateId ->
                        navController.navigate(Screen.TemplatePreview(templateId))
                    }
                )
            }
            
            composable<Screen.TemplatePreview> { backStackEntry ->
                val route: Screen.TemplatePreview = backStackEntry.toRoute()
                TemplatePreviewScreen(
                    templateId = route.templateId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
