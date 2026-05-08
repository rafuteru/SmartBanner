package lab.smartbanner

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import lab.smartbanner.navigation.Screen
import lab.smartbanner.ui.home.HomeScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        
        NavHost(
            navController = navController,
            startDestination = Screen.Home
        ) {
            composable<Screen.Home> {
                HomeScreen()
            }
        }
    }
}
