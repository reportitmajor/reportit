package com.reportit.app.utils

import android.util.Log
import com.reportit.app.data.model.GrievanceReport
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object CloudflareNotificationDispatcher {

    private const val TAG = "CloudflareNotifier"

    // Set your deployed Cloudflare Worker URL here
    var workerBaseUrl: String = "https://black-hat-9d94.reportitmajor.workers.dev"

    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Triggers the Cloudflare Worker to broadcast a push notification to all citizens / ward subscribers
     * whenever a new grievance report is submitted.
     */
    fun dispatchNewReportNotification(report: GrievanceReport) {
        executor.execute {
            try {
                val url = URL("$workerBaseUrl/new-report")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.doOutput = true

                val payload = JSONObject().apply {
                    put("reportId", report.id)
                    put("category", report.category)
                    put("wardName", report.wardName)
                    put("severity", report.severity.label)
                    put("status", report.status.label)
                    put("userId", report.userId)
                    put("message", "🚨 New ${report.category} reported in ${report.wardName}. Immediate municipal attention required.")
                }

                conn.outputStream.use { os ->
                    os.write(payload.toString().toByteArray())
                }

                val responseCode = conn.responseCode
                Log.d(TAG, "Cloudflare Worker dispatch response: $responseCode")
            } catch (e: Exception) {
                Log.w(TAG, "Cloudflare notification trigger skipped or pending worker deployment: ${e.message}")
            }
        }
    }

    /**
     * Triggers the Cloudflare Worker to notify the specific citizen when their grievance status changes
     * (e.g. Field Crew Dispatched, Resolved - Verification Required).
     */
    fun dispatchStatusUpdateNotification(report: GrievanceReport, newStatus: String) {
        executor.execute {
            try {
                val url = URL("$workerBaseUrl/status-update")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.doOutput = true

                val payload = JSONObject().apply {
                    put("reportId", report.id)
                    put("category", report.category)
                    put("wardName", report.wardName)
                    put("status", newStatus)
                    put("userId", report.userId)
                    put("type", "STATUS_UPDATE")
                    put("message", "⚡ Report #${report.id} (${report.category}) updated to: $newStatus.")
                }

                conn.outputStream.use { os ->
                    os.write(payload.toString().toByteArray())
                }

                val responseCode = conn.responseCode
                Log.d(TAG, "Cloudflare Worker status update dispatch response: $responseCode")
            } catch (e: Exception) {
                Log.w(TAG, "Cloudflare notification trigger skipped or pending worker deployment: ${e.message}")
            }
        }
    }
}
