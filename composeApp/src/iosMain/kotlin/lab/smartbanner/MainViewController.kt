package lab.smartbanner

import androidx.compose.ui.window.ComposeUIViewController
import lab.smartbanner.di.initKoin
import platform.UIKit.UIView

var iosAdViewFactory: ((String) -> UIView)? = null

fun MainViewController(
    createAdView: (String) -> UIView,
    adManager: AdManager
) = ComposeUIViewController {
    iosAdViewFactory = createAdView
    setAdManager(adManager) // Set the bridge implementation
    App()
}

fun initializeKoin() {
    initKoin()
}
