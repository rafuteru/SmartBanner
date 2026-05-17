package lab.smartbanner

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.compose.setSingletonImageLoaderFactory
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.AuthState
import lab.smartbanner.navigation.Screen
import lab.smartbanner.ui.auth.AuthScreen
import lab.smartbanner.ui.auth.AuthViewModel
import lab.smartbanner.ui.edit.EditFieldsScreen
import lab.smartbanner.ui.home.HomeScreen
import lab.smartbanner.ui.home.HomeViewModel
import lab.smartbanner.ui.preview.TemplatePreviewScreen
import lab.smartbanner.ui.preview.TemplatePreviewViewModel
import lab.smartbanner.ui.theme.CreateThemeScreen
import lab.smartbanner.ui.theme.PosterWalaTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        getPlatform().createImageLoader(context)
    }

    PosterWalaTheme {
        val navController = rememberNavController()
        val accessCodeRepository: AccessCodeRepository = koinInject()
        val authState by accessCodeRepository.authState.collectAsState()

        val startDestination = if (authState is AuthState.Authenticated) Screen.Home else Screen.Login

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable<Screen.Login> {
                val authViewModel: AuthViewModel = koinViewModel()
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthenticated = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Login) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.Home> {
                val homeViewModel: HomeViewModel = koinViewModel()
                val authViewModel: AuthViewModel = koinViewModel()
                
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToPreview = { id ->
                        navController.navigate(Screen.TemplatePreview(templateId = id))
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                )
            }
            
            composable<Screen.TemplatePreview> { backStackEntry ->
                val route: Screen.TemplatePreview = backStackEntry.toRoute()
                val previewViewModel: TemplatePreviewViewModel = koinViewModel()
                
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
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Screen.TemplatePreview>()
                }
                val previewViewModel: TemplatePreviewViewModel = koinViewModel(
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
                val previewViewModel: TemplatePreviewViewModel = koinViewModel(
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
