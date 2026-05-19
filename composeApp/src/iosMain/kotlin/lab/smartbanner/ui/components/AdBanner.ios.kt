package lab.smartbanner.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import lab.smartbanner.iosAdViewFactory
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdBanner(
    modifier: Modifier,
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: () -> Unit
) {
    val factory = iosAdViewFactory
    if (factory != null) {
        UIKitView(
            factory = { 
                // Note: ideally we would hook into GADBannerViewDelegate here
                // to call onAdLoaded and onAdFailedToLoad
                factory(adUnitId) 
            },
            modifier = modifier.height(50.dp)
        )
    }
}
