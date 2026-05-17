package lab.smartbanner

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

class AndroidPlatform(private val context: Context?) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    
    override val isDebug: Boolean
        get() = context?.let {
            (it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } ?: false

    override fun openEmail(recipient: String, subject: String, body: String) {
        context?.let { ctx ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$recipient")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                ctx.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    ctx.startActivity(Intent.createChooser(fallbackIntent, "Send Email").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (ex: Exception) {
                }
            }
        }
    }

    override fun createImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
}

private var platformInstance: Platform? = null

fun initializePlatform(context: Context) {
    platformInstance = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = platformInstance ?: AndroidPlatform(null)
