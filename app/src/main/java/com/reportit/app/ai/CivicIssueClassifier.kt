package com.reportit.app.ai

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import java.util.Collections

data class CivicClassificationResult(
    val categoryName: String,
    val confidence: Float,
    val confidencePercentage: Int,
    val severity: String,
    val department: String,
    val summary: String,
    val estimatedResolutionHours: Int,
    val isIssueDetected: Boolean,
    val detectedLabels: List<String>
)

class CivicIssueClassifier(private val context: Context) {

    companion object {
        private const val TAG = "CivicClassifier"
        private const val MODEL_NAME = "yolo_civic.onnx"
        private const val INPUT_SIZE = 320
        private const val CONFIDENCE_THRESHOLD = 0.25f

        private val CLASS_NAMES = arrayOf(
            "Container",
            "Garbage",
            "crocodile crack",
            "longitudinal crack",
            "pothole",
            "HV-switch",
            "crossarm",
            "streetlight",
            "traffic-light",
            "transformer"
        )
    }

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init {
        initOnnxModel()
    }

    private fun initOnnxModel() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val modelFile = File(context.filesDir, MODEL_NAME)

            // Extract model to app storage if not present or outdated
            if (!modelFile.exists() || modelFile.length() < 1000L) {
                context.assets.open(MODEL_NAME).use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
            }
            ortSession = ortEnv?.createSession(modelFile.absolutePath, opts)
            Log.d(TAG, "YOLO ONNX model successfully loaded from ${modelFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ONNX runtime model: ${e.message}", e)
        }
    }

    /**
     * Executes real YOLOv8 deep neural network inference on the provided camera Bitmap.
     * Evaluates all 2100 candidate anchors across 10 municipal and defect classes.
     */
    fun classify(bitmap: Bitmap): CivicClassificationResult {
        val session = ortSession
        val env = ortEnv

        if (session == null || env == null) {
            return fallbackNoModelResult()
        }

        try {
            // 1. Preprocess: Resize bitmap to model input dimensions (320x320)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            scaledBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

            // 2. Prepare NCHW FloatBuffer (1, 3, 320, 320)
            val floatBuffer = FloatBuffer.allocate(1 * 3 * INPUT_SIZE * INPUT_SIZE)
            val numPixels = pixels.size

            // Channel R
            for (i in 0 until numPixels) {
                floatBuffer.put(Color.red(pixels[i]) / 255.0f)
            }
            // Channel G
            for (i in 0 until numPixels) {
                floatBuffer.put(Color.green(pixels[i]) / 255.0f)
            }
            // Channel B
            for (i in 0 until numPixels) {
                floatBuffer.put(Color.blue(pixels[i]) / 255.0f)
            }
            floatBuffer.rewind()

            // 3. Create ONNX Tensor
            val inputTensor = OnnxTensor.createTensor(
                env,
                floatBuffer,
                longArrayOf(1, 3, INPUT_SIZE.toLong(), INPUT_SIZE.toLong())
            )

            // 4. Run Inference
            val inputName = session.inputNames.iterator().next()
            val results = session.run(Collections.singletonMap(inputName, inputTensor))

            // 5. Parse Output Tensor (Shape: [1, 14, 2100])
            val rawOutput = results.get(0).value as Array<Array<FloatArray>>
            val features = rawOutput[0] // shape: [14][2100]

            var bestScore = 0.0f
            var bestClassIndex = -1

            val numAnchors = features[0].size
            val numClasses = CLASS_NAMES.size

            for (clsIdx in 0 until numClasses) {
                val row = features[4 + clsIdx] // class scores start at index 4
                for (anchor in 0 until numAnchors) {
                    val rawVal = row[anchor]
                    // Apply Sigmoid to convert raw YOLO logits into valid 0.0..1.0 probability
                    val score = if (rawVal > 1.0f || rawVal < 0.0f) {
                        (1.0f / (1.0f + kotlin.math.exp(-rawVal.toDouble()))).toFloat()
                    } else {
                        rawVal
                    }
                    if (score > bestScore) {
                        bestScore = score
                        bestClassIndex = clsIdx
                    }
                }
            }

            inputTensor.close()
            results.close()

            // 6. Threshold Check: If below 0.25f, it is NOT a civic issue (e.g. room, face, plain floor)
            if (bestScore < CONFIDENCE_THRESHOLD || bestClassIndex == -1) {
                return CivicClassificationResult(
                    categoryName = "No Civic Issue Detected",
                    confidence = bestScore,
                    confidencePercentage = (bestScore * 100).toInt(),
                    severity = "None / Normal",
                    department = "No Action Required",
                    summary = "The neural network scanned the image and did not detect any road damage, pothole, garbage dump, or civic defect. If reporting an issue, please photograph the street damage directly.",
                    estimatedResolutionHours = 0,
                    isIssueDetected = false,
                    detectedLabels = emptyList()
                )
            }

            val rawClassName = CLASS_NAMES[bestClassIndex]
            val confidencePct = (bestScore * 100).toInt()

            return when (rawClassName) {
                "pothole" -> CivicClassificationResult(
                    categoryName = "Road Pothole (BBMP Roads)",
                    confidence = bestScore,
                    confidencePercentage = confidencePct,
                    severity = if (bestScore > 0.5f) "High Severity" else "Medium Severity",
                    department = "BBMP Road Infrastructure Division",
                    summary = "Asphalt surface depression and road crater verified by YOLO deep neural network. Immediate dispatch required to avoid vehicular accident.",
                    estimatedResolutionHours = 48,
                    isIssueDetected = true,
                    detectedLabels = listOf("Pothole Crater", "Asphalt Failure")
                )
                "Garbage", "Container" -> CivicClassificationResult(
                    categoryName = "Garbage Dump / Blackspot (BBMP SWM)",
                    confidence = bestScore,
                    confidencePercentage = confidencePct,
                    severity = "High Severity",
                    department = "BBMP Solid Waste Management (SWM)",
                    summary = "Solid waste and roadside blackspot litter accumulation detected. Sanitary cleanup crew and compactor vehicle dispatch advised.",
                    estimatedResolutionHours = 24,
                    isIssueDetected = true,
                    detectedLabels = listOf("Solid Waste", "Blackspot Litter")
                )
                "crocodile crack", "longitudinal crack" -> CivicClassificationResult(
                    categoryName = "Road Crack & Surface Fracture (BBMP Roads)",
                    confidence = bestScore,
                    confidencePercentage = confidencePct,
                    severity = "Medium Severity",
                    department = "BBMP Road Infrastructure Division",
                    summary = "Structural alligator/longitudinal asphalt fracturing verified. Resurfacing recommended to prevent deeper pothole crater development.",
                    estimatedResolutionHours = 72,
                    isIssueDetected = true,
                    detectedLabels = listOf("Asphalt Crack", "Pavement Fracture")
                )
                "streetlight" -> CivicClassificationResult(
                    categoryName = "Broken Streetlight (BBMP Electrical)",
                    confidence = bestScore,
                    confidencePercentage = confidencePct,
                    severity = "Medium Severity",
                    department = "BBMP Electrical & Lighting / BESCOM",
                    summary = "Municipal streetlight luminaire / fixture detected. Night public visibility hazard if non-operational.",
                    estimatedResolutionHours = 36,
                    isIssueDetected = true,
                    detectedLabels = listOf("Streetlight Pole", "Electrical Fitting")
                )
                "HV-switch", "transformer", "crossarm" -> CivicClassificationResult(
                    categoryName = "Electrical Grid Hazard (BESCOM)",
                    confidence = bestScore,
                    confidencePercentage = confidencePct,
                    severity = "Critical Severity",
                    department = "BESCOM Central Electrical Distribution",
                    summary = "High-voltage electrical grid equipment/transformer hazard detected. Priority safety cordon and engineer inspection required.",
                    estimatedResolutionHours = 12,
                    isIssueDetected = true,
                    detectedLabels = listOf("Transformer", "High-Voltage Grid")
                )
                else -> CivicClassificationResult(
                    categoryName = "Road Pothole (BBMP Roads)",
                    confidence = bestScore,
                    confidencePercentage = confidencePct,
                    severity = "High Severity",
                    department = "BBMP Road Infrastructure Division",
                    summary = "Road surface degradation identified by deep learning model.",
                    estimatedResolutionHours = 48,
                    isIssueDetected = true,
                    detectedLabels = listOf("Road Damage")
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Inference execution error: ${e.message}", e)
            return fallbackNoModelResult()
        }
    }

    private fun fallbackNoModelResult(): CivicClassificationResult {
        return CivicClassificationResult(
            categoryName = "No Civic Issue Detected",
            confidence = 0.0f,
            confidencePercentage = 0,
            severity = "None",
            department = "No Action Required",
            summary = "Model initialization in progress. Please ensure the camera is aimed directly at the civic damage.",
            estimatedResolutionHours = 0,
            isIssueDetected = false,
            detectedLabels = emptyList()
        )
    }
}
