package lab.smartbanner

import androidx.compose.ui.window.ComposeUIViewController
import lab.smartbanner.di.initKoin

fun MainViewController() = ComposeUIViewController {
    App()
}

fun initializeKoin() {
    initKoin()
}
