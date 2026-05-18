package lab.smartbanner.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView
import platform.UIKit.UILabel
import platform.UIKit.UIColor
import platform.UIKit.NSTextAlignmentCenter
import platform.CoreGraphics.CGRectMake
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdBanner(modifier: Modifier, adUnitId: String) {
    // Placeholder for iOS AdBanner. 
    // Real AdMob integration on iOS usually requires CocoaPods/SPM setup in the Xcode project.
    UIKitView(
        factory = {
            val view = UIView()
            view.backgroundColor = UIColor.lightGrayColor
            val label = UILabel(frame = CGRectMake(0.0, 0.0, 320.0, 50.0))
            label.text = "Ad Banner Placeholder\nID: ${adUnitId ?: "Test"}"
            label.numberOfLines = 0
            label.textAlignment = NSTextAlignmentCenter
            view.addSubview(label)
            view
        },
        modifier = modifier.fillMaxWidth().height(50.dp)
    )
}
