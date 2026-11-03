package com.reportit.app.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiVisionClassifier {

    private const val TAG = "GeminiVision"
    private const val API_KEY = "YOUR_GEMINI_API_KEY"
    private const val MODEL_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent"

    /**
     * Executes real Gemini Flash Multimodal Vision inference on the provided Bitmap.
     * Analyzes road hazards, garbage dumps, broken streetlights, water leakages, and civic defects.
     * Throws an Exception if offline or API unavailable, triggering the local YOLO fallback.
     */
    fun classify(bitmap: Bitmap): CivicClassificationResult {
        // 1. Downscale bitmap for fast mobile upload and encode to Base64 JPEG
        val maxDim = 800
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val targetW = if (ratio > 1f) maxDim else (maxDim * ratio).toInt()
        val targetH = if (ratio > 1f) (maxDim / ratio).toInt() else maxDim
        val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)

        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val imageBytes = baos.toByteArray()
        val base64Data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        // 2. Prepare System Prompt & JSON Request Body
        val promptText = "You are an expert municipal infrastructure inspector in India. " +
                "Inspect this citizen photo and identify any civic defect or hazard (e.g. road pothole, pavement crack, " +
                "garbage dump/blackspot, open manhole/drain, water pipe leakage, broken streetlight, hazardous electric wire/transformer). " +
                "Return a raw JSON object with keys: " +
                "\"isIssueDetected\" (boolean, true if real civic hazard, false if indoor room, selfie, pet, or plain surface), " +
                "\"categoryName\" (e.g. 'Road Pothole (BBMP Roads)', 'Garbage Dump (BBMP SWM)', 'Water Leakage (BWSSB)', 'Broken Streetlight (BESCOM)', 'Electrical Hazard (BESCOM)'), " +
                "\"department\" (e.g. 'BBMP Road Infrastructure Division', 'BBMP Solid Waste Management', 'BWSSB Water Supply', 'BESCOM Electrical Distribution'), " +
                "\"severity\" ('Critical Severity', 'High Severity', 'Medium Severity', or 'None / Normal'), " +
                "\"confidencePercentage\" (integer 0-100), " +
                "\"summary\" (2-sentence clear municipal summary), " +
                "\"estimatedResolutionHours\" (integer, e.g. 24, 48, 72)."

        val rootPayload = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply { put("text", promptText) })
                put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Data)
                    })
                })
            }
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply { put("parts", partsArray) })
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
            })
        }

        // 3. Perform HTTP POST
        val url = URL(MODEL_ENDPOINT)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("X-goog-api-key", API_KEY)
        conn.connectTimeout = 8000
        conn.readTimeout = 12000
        conn.doOutput = true

        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(rootPayload.toString())
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            val errorStream = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            throw IllegalStateException("Gemini API error ($responseCode): $errorStream")
        }

        val responseJson = conn.inputStream.bufferedReader().use { it.readText() }
        Log.d(TAG, "Gemini Vision raw response received: ${responseJson.take(150)}...")

        // 4. Parse Candidates
        val root = JSONObject(responseJson)
        val candidates = root.optJSONArray("candidates")
            ?: throw IllegalStateException("No candidates in Gemini response")
        if (candidates.length() == 0) throw IllegalStateException("Empty candidates in Gemini response")

        val contentObj = candidates.getJSONObject(0).optJSONObject("content")
            ?: throw IllegalStateException("No content object")
        val parts = contentObj.optJSONArray("parts")
            ?: throw IllegalStateException("No parts object")
        val resultText = parts.getJSONObject(0).optString("text")

        // Parse result text (can be JSON Object or JSON Array with single object)
        val trimmed = resultText.trim()
        val dataObj = if (trimmed.startsWith("[")) {
            JSONArray(trimmed).getJSONObject(0)
        } else {
            JSONObject(trimmed)
        }

        val isIssue = dataObj.optBoolean("isIssueDetected", true)
        val category = dataObj.optString("categoryName", "Road Pothole (BBMP Roads)")
        val dept = dataObj.optString("department", "BBMP Road Infrastructure Division")
        val severity = dataObj.optString("severity", "High Severity")
        val confPct = dataObj.optInt("confidencePercentage", 95)
        val summary = dataObj.optString("summary", "Civic issue verified by Gemini Flash Multimodal AI.")
        val resHours = dataObj.optInt("estimatedResolutionHours", 48)

        return CivicClassificationResult(
            categoryName = if (isIssue) category else "No Civic Issue Detected",
            confidence = confPct / 100.0f,
            confidencePercentage = confPct,
            severity = if (isIssue) severity else "None / Normal",
            department = if (isIssue) dept else "No Action Required",
            summary = summary,
            estimatedResolutionHours = if (isIssue) resHours else 0,
            isIssueDetected = isIssue,
            detectedLabels = if (isIssue) listOf(category, "Gemini Multimodal AI") else emptyList()
        )
    }
}
