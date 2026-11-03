package com.reportit.app.data.repository

import android.graphics.Bitmap
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

data class CloudinaryUploadResult(
    val imageUrl: String,
    val thumbUrl: String,
    val publicId: String
)

object CloudinaryRepository {
    private const val TAG = "CloudinaryRepository"

    // Cloudinary configuration credentials
    var cloudName: String = "inigcvil"
    var uploadPreset: String = "reportit" // For unsigned direct uploads
    var apiKey: String = "334821795689779"

    private val executor = Executors.newSingleThreadExecutor()

    fun isConfigured(): Boolean {
        return cloudName.isNotEmpty() && (uploadPreset.isNotEmpty() || apiKey.isNotEmpty())
    }

    /**
     * Uploads bitmap directly to Cloudinary CDN via standard REST endpoint:
     * https://api.cloudinary.com/v1_1/<cloud_name>/image/upload
     */
    fun uploadBitmap(
        bitmap: Bitmap,
        onSuccess: (CloudinaryUploadResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isConfigured()) {
            onError("Cloudinary credentials not yet configured.")
            return
        }

        executor.execute {
            try {
                // Compress bitmap
                val maxDim = 1280
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW = if (ratio > 1f) maxDim else (maxDim * ratio).toInt()
                val targetH = if (ratio > 1f) (maxDim / ratio).toInt() else maxDim
                val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)

                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                val imageBytes = baos.toByteArray()

                val boundary = "==CloudinaryBoundary" + UUID.randomUUID().toString()
                val url = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.doInput = true
                conn.connectTimeout = 20000
                conn.readTimeout = 25000
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                val dos = DataOutputStream(conn.outputStream)

                // Add upload_preset if configured
                if (uploadPreset.isNotEmpty()) {
                    dos.writeBytes("--$boundary\r\n")
                    dos.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                    dos.writeBytes("$uploadPreset\r\n")
                }

                // Add file binary
                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"report_photo.jpg\"\r\n")
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                dos.write(imageBytes)
                dos.writeBytes("\r\n")

                dos.writeBytes("--$boundary--\r\n")
                dos.flush()
                dos.close()

                val responseCode = conn.responseCode
                val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                val reader = BufferedReader(InputStreamReader(stream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                conn.disconnect()

                val responseBody = sb.toString()
                Log.d(TAG, "Cloudinary Response ($responseCode): $responseBody")

                if (responseCode in 200..299) {
                    val json = JSONObject(responseBody)
                    val secureUrl = json.optString("secure_url").ifEmpty { json.optString("url") }
                    val publicId = json.optString("public_id")
                    // Cloudinary allows automatic on-the-fly thumbnail transformations
                    val thumbUrl = if (secureUrl.contains("/upload/")) {
                        secureUrl.replace("/upload/", "/upload/c_thumb,w_300,g_face/")
                    } else {
                        secureUrl
                    }

                    onSuccess(
                        CloudinaryUploadResult(
                            imageUrl = secureUrl,
                            thumbUrl = thumbUrl,
                            publicId = publicId
                        )
                    )
                } else {
                    onError("Cloudinary upload failed: $responseBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cloudinary upload error", e)
                onError(e.localizedMessage ?: "Network error during Cloudinary upload")
            }
        }
    }
}
