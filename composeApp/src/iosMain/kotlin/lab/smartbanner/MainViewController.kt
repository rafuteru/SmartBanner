package lab.smartbanner

import androidx.compose.ui.window.ComposeUIViewController
import lab.smartbanner.di.initKoin
import platform.UIKit.UIView

var iosAdViewFactory: ((String) -> UIView)? = null

fun MainViewController(createAdView: (String) -> UIView) = ComposeUIViewController {
    iosAdViewFactory = createAdView
    App()
}

fun initializeKoin() {
    initKoin()
}
