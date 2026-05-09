package lab.smartbanner.utils

import androidx.compose.ui.graphics.ImageBitmap

interface PosterExporter {
    suspend fun saveToGallery(bitmap: ImageBitmap, fileName: String): Result<Unit>
    suspend fun sharePoster(bitmap: ImageBitmap, fileName: String, text: String): Result<Unit>
}

/**
 * Platform-specific factory to create a [PosterExporter].
 */
expect fun createPosterExporter(context: Any?): PosterExporter
