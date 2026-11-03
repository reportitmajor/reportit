package com.reportit.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * Scales bitmap to max dimension and compresses to JPEG Base64 string.
     * Produces compact ~30KB - 45KB payload safe for Cloud Firestore documents.
     */
    fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 640, quality: Int = 65): String {
        val width = bitmap.width
        val height = bitmap.height

        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1.0f) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
            }
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Decodes Base64 string back into Android Bitmap.
     */
    fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val cleanStr = if (base64Str.startsWith("data:image")) {
                base64Str.substringAfter(",")
            } else {
                base64Str
            }
            val decodedBytes = Base64.decode(cleanStr, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private val memoryCache = object : android.util.LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
    private val imageExecutor = java.util.concurrent.Executors.newFixedThreadPool(4)

    /**
     * Loads image from direct URL (e.g. ImgBB CDN) asynchronously with LruCache.
     */
    fun loadUrlToImageView(imageView: android.widget.ImageView, url: String?, fallbackResId: Int) {
        if (url.isNullOrEmpty()) {
            imageView.setImageResource(fallbackResId)
            return
        }

        // Check in-memory cache first
        val cached = memoryCache.get(url)
        if (cached != null) {
            imageView.setImageBitmap(cached)
            return
        }

        imageView.setImageResource(fallbackResId)
        imageView.tag = url

        imageExecutor.execute {
            try {
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.doInput = true
                conn.connect()
                val input = conn.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()
                conn.disconnect()

                if (bitmap != null) {
                    memoryCache.put(url, bitmap)
                    imageView.post {
                        if (imageView.tag == url) {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep fallback on error
            }
        }
    }
}
