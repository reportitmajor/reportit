package com.reportit.app.data.model

import androidx.annotation.DrawableRes

enum class ReportSeverity(val label: String, val colorHex: String, val bgHex: String) {
    CRITICAL("Critical", "#DC2626", "#FEE2E2"),
    HIGH("High", "#EA580C", "#FFEDD5"),
    MEDIUM("Medium", "#D97706", "#FEF3C7"),
    LOW("Low", "#2563EB", "#DBEAFE")
}

enum class ReportStatus(val label: String, val colorHex: String, val bgHex: String, val stageIndex: Int) {
    SUBMITTED("Submitted", "#64748B", "#F1F5F9", 1),
    AI_ANALYZED("AI Analyzed", "#7C3AED", "#EDE9FE", 2),
    VERIFIED("Verified", "#2563EB", "#DBEAFE", 3),
    ASSIGNED("Assigned", "#0284C7", "#E0F2FE", 4),
    IN_PROGRESS("In Progress", "#2563EB", "#DBEAFE", 5),
    RESOLVED("Resolved - Verification Required", "#059669", "#D1FAE5", 6),
    CITIZEN_CLOSED("Closed & Verified", "#059669", "#D1FAE5", 7),
    ESCALATED("Escalated - SLA Breached", "#DC2626", "#FEE2E2", 5)
}

data class TimelineEvent(
    val stageTitle: String,
    val timestamp: String,
    val description: String,
    val actor: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

data class GrievanceReport(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val department: String,
    val wardName: String,
    val locationAddress: String,
    val latitude: Double,
    val longitude: Double,
    val severity: ReportSeverity,
    var status: ReportStatus,
    val slaTotalHours: Int,
    var slaRemainingHours: Int,
    val submissionDate: String,
    val reportedBy: String = "You (Citizen Reporter)",
    val aiConfidence: Int = 94,
    @param:DrawableRes val originalImageResId: Int = com.reportit.app.R.drawable.img_pothole_evidence,
    @param:DrawableRes val resolvedImageResId: Int? = null,
    var assignedOfficer: String? = null,
    var assignedCrew: String? = null,
    var upvotesCount: Int = 0,
    var isUserUpvoted: Boolean = false,
    var isDisputed: Boolean = false,
    val timeline: MutableList<TimelineEvent> = mutableListOf(),
    val imageBase64: String? = null,
    val imageUrl: String? = null,
    val thumbUrl: String? = null,
    val acName: String? = null,
    val mlaName: String? = null,
    val gbaZone: String? = null,
    val aeeName: String? = null,
    val aeeContact: String? = null,
    val userId: String? = null,
    val userEmail: String? = null,
    val resolutionPhotoUrl: String? = null,
    val resolutionNotes: String? = null
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "department" to department,
            "wardName" to wardName,
            "locationAddress" to locationAddress,
            "latitude" to latitude,
            "longitude" to longitude,
            "severity" to severity.name,
            "status" to status.name,
            "slaTotalHours" to slaTotalHours,
            "slaRemainingHours" to slaRemainingHours,
            "submissionDate" to submissionDate,
            "reportedBy" to reportedBy,
            "aiConfidence" to aiConfidence,
            "assignedOfficer" to assignedOfficer,
            "assignedCrew" to assignedCrew,
            "upvotesCount" to upvotesCount,
            "imageBase64" to imageBase64,
            "imageUrl" to imageUrl,
            "thumbUrl" to thumbUrl,
            "acName" to acName,
            "mlaName" to mlaName,
            "gbaZone" to gbaZone,
            "aeeName" to aeeName,
            "aeeContact" to aeeContact,
            "userId" to userId,
            "userEmail" to userEmail,
            "resolutionPhotoUrl" to resolutionPhotoUrl,
            "resolutionNotes" to resolutionNotes,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any>): GrievanceReport {
            val statusStr = data["status"] as? String ?: "SUBMITTED"
            val severityStr = data["severity"] as? String ?: "HIGH"
            val reportStatus = try { ReportStatus.valueOf(statusStr) } catch (_: Exception) { ReportStatus.SUBMITTED }
            val reportSeverity = try { ReportSeverity.valueOf(severityStr) } catch (_: Exception) { ReportSeverity.HIGH }

            return GrievanceReport(
                id = data["id"] as? String ?: "RI-${System.currentTimeMillis() % 1000000}",
                title = data["title"] as? String ?: "Civic Issue",
                description = data["description"] as? String ?: "",
                category = data["category"] as? String ?: "Roads & Infrastructure",
                department = data["department"] as? String ?: "Municipal Corporation",
                wardName = data["wardName"] as? String ?: "Bengaluru Central",
                locationAddress = data["locationAddress"] as? String ?: "Bengaluru",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 12.9716,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 77.5946,
                severity = reportSeverity,
                status = reportStatus,
                slaTotalHours = (data["slaTotalHours"] as? Number)?.toInt() ?: 48,
                slaRemainingHours = (data["slaRemainingHours"] as? Number)?.toInt() ?: 48,
                submissionDate = data["submissionDate"] as? String ?: "Today",
                reportedBy = data["reportedBy"] as? String ?: "Citizen Reporter",
                aiConfidence = (data["aiConfidence"] as? Number)?.toInt() ?: 85,
                originalImageResId = com.reportit.app.R.drawable.img_pothole_evidence,
                assignedOfficer = data["assignedOfficer"] as? String,
                assignedCrew = data["assignedCrew"] as? String,
                upvotesCount = (data["upvotesCount"] as? Number)?.toInt() ?: 0,
                imageBase64 = data["imageBase64"] as? String,
                imageUrl = data["imageUrl"] as? String,
                thumbUrl = data["thumbUrl"] as? String,
                acName = data["acName"] as? String,
                mlaName = data["mlaName"] as? String,
                gbaZone = data["gbaZone"] as? String,
                aeeName = data["aeeName"] as? String,
                aeeContact = data["aeeContact"] as? String,
                userId = data["userId"] as? String,
                userEmail = data["userEmail"] as? String,
                resolutionPhotoUrl = (data["resolutionPhotoUrl"] as? String)?.takeIf { it.isNotEmpty() } ?: (data["resolvedImageUrl"] as? String),
                resolutionNotes = data["resolutionNotes"] as? String
            )
        }
    }
}
