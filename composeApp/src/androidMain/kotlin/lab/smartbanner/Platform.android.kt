package lab.smartbanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

class AndroidPlatform(private val context: Context?) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    
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
                // If mailto fails, try a generic chooser
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
                    // Log or show error
                }
            }
        }
    }
}

private var platformInstance: Platform? = null

fun initializePlatform(context: Context) {
    platformInstance = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = platformInstance ?: AndroidPlatform(null)
