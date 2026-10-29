package com.teleprompter.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ThumbnailUtils {

    suspend fun generateAndSaveThumbnail(
        context: Context,
        videoUri: Uri,
        recordingId: String
    ): String? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            if (videoUri.scheme == "file") {
                retriever.setDataSource(videoUri.path)
            } else {
                retriever.setDataSource(context, videoUri)
            }

            // Retrieve frame at 1,000,000 microseconds (1.0 second)
            val bitmap = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime

            if (bitmap != null) {
                val thumbnailsDir = File(context.filesDir, "thumbnails").apply {
                    if (!exists()) mkdirs()
                }
                val thumbnailFile = File(thumbnailsDir, "thumb_$recordingId.jpg")

                // Scale down bitmap to max 480px width preserving aspect ratio
                val maxDim = 480
                val width = bitmap.width
                val height = bitmap.height
                val scale = if (width > height) {
                    maxDim.toFloat() / width
                } else {
                    maxDim.toFloat() / height
                }
                val scaledBitmap = if (scale < 1.0f) {
                    Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
                } else {
                    bitmap
                }

                FileOutputStream(thumbnailFile).use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }

                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle()
                }
                bitmap.recycle()

                return@withContext thumbnailFile.absolutePath
            }
        } catch (_: Exception) {
            // Extraction failure fallback
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
        null
    }

    suspend fun deleteRecordingFiles(
        videoUriString: String?,
        thumbnailPath: String?
    ) = withContext(Dispatchers.IO) {
        if (!thumbnailPath.isNullOrBlank()) {
            try {
                File(thumbnailPath).delete()
            } catch (_: Exception) {}
        }

        if (!videoUriString.isNullOrBlank()) {
            try {
                val uri = Uri.parse(videoUriString)
                if (uri.scheme == "file") {
                    File(uri.path ?: "").delete()
                }
            } catch (_: Exception) {}
        }
    }

    fun getAvailableStorageBytes(): Long {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (_: Exception) {
            Long.MAX_VALUE
        }
    }

    fun isStorageCriticallyLow(): Boolean {
        // Less than 100 MB available
        return getAvailableStorageBytes() < 100L * 1024L * 1024L
    }
}
