package es.ulpgc.kippo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

object ImageUtils {
    private const val DEFAULT_IMAGE_QUALITY = 80
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = DEFAULT_IMAGE_QUALITY): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
    fun uriToBase64(context: Context, uri: Uri, maxSize: Int = 1024, quality: Int = DEFAULT_IMAGE_QUALITY): String? {
        return try {
            val sourceBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            val scaledBitmap = scaleBitmapPreservingRatio(sourceBitmap, maxSize)
            bitmapToBase64(scaledBitmap, quality)
        } catch (_: Exception) {
            null
        }
    }

    private fun scaleBitmapPreservingRatio(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return bitmap.scale(newWidth, newHeight)
    }

    fun decodeToImageBitmapOrNull(data: String?): ImageBitmap? {
        if (data.isNullOrBlank()) return null

        return try {
            val lower = data.lowercase()
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("content://")) {
                return null
            }

            val bytes = Base64.decode(data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            bitmap.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}
