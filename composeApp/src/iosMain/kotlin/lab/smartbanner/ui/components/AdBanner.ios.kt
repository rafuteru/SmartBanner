package lab.smartbanner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import lab.smartbanner.iosAdViewFactory
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdBanner(modifier: Modifier, adUnitId: String) {
    val factory = iosAdViewFactory
    if (factory != null) {
        UIKitView(
            factory = { factory(adUnitId) },
            modifier = modifier
        )
    }
}
