package com.reportit.app.data.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

data class ImgBBUploadResult(
    val imageUrl: String,
    val thumbUrl: String,
    val deleteUrl: String? = null
)

object ImgBBRepository {

    private const val TAG = "ImgBBRepository"
    private const val API_KEY = "1f55bd38229f316cfde6f72ae27ccd45"
    private const val UPLOAD_ENDPOINT = "https://api.imgbb.com/1/upload"

    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Compresses the provided bitmap and uploads it to ImgBB cloud storage.
     * Returns direct CDN URLs for instant loading without credit-card dependencies.
     */
    fun uploadBitmap(
        bitmap: Bitmap,
        onSuccess: (ImgBBUploadResult) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            try {
                // 1. Resize and compress bitmap to high quality lightweight JPEG
                val maxDim = 1280
                val width = bitmap.width
                val height = bitmap.height
                val scaledBitmap = if (width > maxDim || height > maxDim) {
                    val ratio = width.toFloat() / height.toFloat()
                    val targetW = if (ratio > 1f) maxDim else (maxDim * ratio).toInt()
                    val targetH = if (ratio > 1f) (maxDim / ratio).toInt() else maxDim
                    Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                } else {
                    bitmap
                }

                val baos = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, baos)
                val imageBytes = baos.toByteArray()
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                // 2. Perform HTTP POST to ImgBB API
                val url = URL("$UPLOAD_ENDPOINT?key=$API_KEY")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.doInput = true
                conn.connectTimeout = 20000
                conn.readTimeout = 25000
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "image=" + URLEncoder.encode(base64Image, "UTF-8")
                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(postData)
                writer.flush()
                writer.close()

                val responseCode = conn.responseCode
                val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                val reader = BufferedReader(InputStreamReader(stream))
                val responseSb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseSb.append(line)
                }
                reader.close()
                conn.disconnect()

                val rawResponse = responseSb.toString()
                Log.d(TAG, "ImgBB Upload Response ($responseCode): $rawResponse")

                if (responseCode in 200..299) {
                    val json = JSONObject(rawResponse)
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        val imageUrl = data.optString("display_url").ifEmpty { data.optString("url") }
                        val thumbObj = data.optJSONObject("thumb")
                        val thumbUrl = thumbObj?.optString("url") ?: imageUrl
                        val deleteUrl = data.optString("delete_url")

                        onSuccess(
                            ImgBBUploadResult(
                                imageUrl = imageUrl,
                                thumbUrl = thumbUrl,
                                deleteUrl = deleteUrl
                            )
                        )
                        return@execute
                    }
                }

                val errorJson = try { JSONObject(rawResponse) } catch (_: Exception) { null }
                val errorMsg = errorJson?.optJSONObject("error")?.optString("message")
                    ?: "ImgBB upload failed with HTTP status $responseCode"
                onError(errorMsg)

            } catch (e: Exception) {
                Log.e(TAG, "Exception during ImgBB upload: ${e.message}", e)
                onError(e.localizedMessage ?: "Network error during photo upload")
            }
        }
    }
}
