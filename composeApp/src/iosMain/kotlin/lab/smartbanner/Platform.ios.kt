package lab.smartbanner

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import platform.UIKit.UIDevice
import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    
    @OptIn(ExperimentalNativeApi::class)
    override val isDebug: Boolean = kotlin.native.Platform.isDebugBinary

    override fun openEmail(recipient: String, subject: String, body: String) {
        val urlString = "mailto:$recipient?subject=${subject.replace(" ", "%20")}&body=${body.replace(" ", "%20")}"
        val url = NSURL.URLWithString(urlString)
        if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }

    override fun createImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
}

actual fun getPlatform(): Platform = IOSPlatform()
