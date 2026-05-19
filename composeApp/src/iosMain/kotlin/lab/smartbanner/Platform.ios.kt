package lab.smartbanner

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    
    // Will be injected from Swift
    var adManager: AdManager? = null

    @OptIn(ExperimentalNativeApi::class)
    override val isDebug: Boolean = kotlin.native.Platform.isDebugBinary

    override val deviceId: String
        get() = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_ios"

    @OptIn(ExperimentalForeignApi::class)
    override fun openEmail(recipient: String, subject: String, body: String) {
        // ... (existing implementation)
    }

    override fun createImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    override fun showRewardedAd(adUnitId: String, onRewardEarned: () -> Unit) {
        adManager?.showRewardedAd(adUnitId, onRewardEarned)
    }

    override fun showInterstitialAd(adUnitId: String, onAdClosed: () -> Unit) {
        adManager?.showInterstitialAd(adUnitId, onAdClosed)
    }

    override fun showAppOpenAd(adUnitId: String) {
        adManager?.showAppOpenAd(adUnitId)
    }
}

private val iosPlatform = IOSPlatform()

actual fun getPlatform(): Platform = iosPlatform

// Helper for Swift to provide the AdManager implementation
fun setAdManager(manager: AdManager) {
    iosPlatform.adManager = manager
}
