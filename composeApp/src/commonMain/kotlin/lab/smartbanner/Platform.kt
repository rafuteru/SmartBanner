package lab.smartbanner

import coil3.ImageLoader
import coil3.PlatformContext

interface Platform {
    val name: String
    val isDebug: Boolean
    val deviceId: String
    fun openEmail(recipient: String, subject: String, body: String)
    fun createImageLoader(context: PlatformContext): ImageLoader
    fun showRewardedAd(adUnitId: String, onRewardEarned: () -> Unit)
}

expect fun getPlatform(): Platform
