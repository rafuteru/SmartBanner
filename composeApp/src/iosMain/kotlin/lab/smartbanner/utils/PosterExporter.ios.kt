package lab.smartbanner.utils

import androidx.compose.ui.graphics.ImageBitmap
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.EncodedImageFormat
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes

class IosPosterExporter : PosterExporter {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveToGallery(bitmap: ImageBitmap, fileName: String): Result<Unit> {
        return try {
            val uiImage = bitmap.toUIImage()
            if (uiImage != null) {
                UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to convert bitmap to UIImage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun sharePoster(bitmap: ImageBitmap, fileName: String, text: String): Result<Unit> {
        return try {
            val uiImage = bitmap.toUIImage()
            if (uiImage != null) {
                val window = UIApplication.sharedApplication.keyWindow
                val rootViewController = window?.rootViewController
                
                val activityViewController = UIActivityViewController(
                    activityItems = listOf(uiImage, text),
                    applicationActivities = null
                )
                
                rootViewController?.presentViewController(
                    viewControllerToPresent = activityViewController,
                    animated = true,
                    completion = null
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to convert bitmap to UIImage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ImageBitmap.toUIImage(): UIImage? {
    val skiaBitmap = this.asSkiaBitmap()
    val image = Image.makeFromBitmap(skiaBitmap)
    val data = image.encodeToData(EncodedImageFormat.PNG) ?: return null
    val bytes = data.bytes
    val nsData = bytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }
    return UIImage.imageWithData(nsData)
}

actual fun createPosterExporter(context: Any?): PosterExporter = IosPosterExporter()
