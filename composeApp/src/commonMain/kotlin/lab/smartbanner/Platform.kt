package lab.smartbanner

import coil3.ImageLoader
import coil3.PlatformContext

interface Platform {
    val name: String
    val isDebug: Boolean
    fun openEmail(recipient: String, subject: String, body: String)
    fun createImageLoader(context: PlatformContext): ImageLoader
}

expect fun getPlatform(): Platform
