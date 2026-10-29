package com.example.somafern.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * Reads image from Uri, scales it down to max 480x480 to keep payload small (~20KB-40KB),
     * compresses to JPEG with 75% quality, and converts to Base64 data string.
     */
    fun compressAndEncodeToBase64(
        context: Context,
        uri: Uri,
        maxWidth: Int = 480,
        maxHeight: Int = 480,
        quality: Int = 75
    ): String? {
        return try {
            val originalBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: return null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height, 1.0f)
            val targetWidth = (width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (height * scale).toInt().coerceAtLeast(1)

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            outputStream.close()

            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun uriToBase64(context: Context, uri: Uri): String? {
        return compressAndEncodeToBase64(context, uri)
    }

    /**
     * Decodes Base64 data string (with or without 'data:image/...;base64,' prefix) to Bitmap.
     */
    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val pureBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            val decodedBytes = Base64.decode(pureBase64.trim(), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Determines if a string is a Base64 encoded image string.
     */
    fun isBase64Image(str: String): Boolean {
        return str.startsWith("data:image") || (str.length > 200 && !str.startsWith("http://") && !str.startsWith("https://"))
    }
}
