package com.reportit.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportStatus

object FirestoreRepository {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_REPORTS = "reports"

    /**
     * Saves or updates the citizen / officer profile upon authentication.
     */
    fun saveUserProfile(
        uid: String,
        name: String,
        email: String,
        role: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        if (uid.isEmpty()) {
            onComplete?.invoke(false)
            return
        }

        val userDoc = db.collection(COLLECTION_USERS).document(uid)
        val data = hashMapOf(
            "uid" to uid,
            "fullName" to name,
            "email" to email,
            "role" to role,
            "lastActiveAt" to Timestamp.now()
        )

        userDoc.set(data, SetOptions.merge())
            .addOnSuccessListener {
                // Ensure default karma points if document is brand new
                userDoc.get().addOnSuccessListener { snapshot ->
                    if (!snapshot.contains("karmaPoints")) {
                        userDoc.update(
                            mapOf(
                                "karmaPoints" to 50,
                                "totalReportsCount" to 0,
                                "createdAt" to Timestamp.now()
                            )
                        )
                    }
                    onComplete?.invoke(true)
                }
            }
            .addOnFailureListener {
                onComplete?.invoke(false)
            }
    }

    /**
     * Submits a grievance report document with embedded compressed photo,
     * AI detection confidence, GPS coordinates, and initial SUBMITTED status.
     */
    fun submitReport(
        report: GrievanceReport,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docRef = db.collection(COLLECTION_REPORTS).document(report.id)
        docRef.set(report.toFirestoreMap())
            .addOnSuccessListener {
                // Reward reporter with 25 karma points and increment report counter
                val userId = report.userId
                if (!userId.isNullOrEmpty()) {
                    db.collection(COLLECTION_USERS).document(userId).update(
                        mapOf(
                            "karmaPoints" to FieldValue.increment(25),
                            "totalReportsCount" to FieldValue.increment(1)
                        )
                    )
                }
                // Trigger Cloudflare Worker to dispatch OneSignal push notification
                com.reportit.app.utils.CloudflareNotificationDispatcher.dispatchNewReportNotification(report)

                onComplete(true, report.id)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.localizedMessage)
            }
    }

    /**
     * Real-time listener for all grievance reports, sorted chronologically.
     */
    fun listenToAllReports(
        onUpdate: (List<GrievanceReport>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection(COLLECTION_REPORTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reports = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { GrievanceReport.fromFirestoreMap(it) }
                    }
                    onUpdate(reports)
                }
            }
    }

    /**
     * Updates grievance status, assigning officers and crew teams in real time.
     */
    fun updateReportStatus(
        reportId: String,
        newStatus: ReportStatus,
        assignedOfficer: String? = null,
        assignedCrew: String? = null,
        onComplete: (Boolean) -> Unit
    ) {
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus.name,
            "updatedAt" to Timestamp.now()
        )
        if (assignedOfficer != null) updates["assignedOfficer"] = assignedOfficer
        if (assignedCrew != null) updates["assignedCrew"] = assignedCrew

        db.collection(COLLECTION_REPORTS).document(reportId)
            .update(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Increments or decrements civic validation upvotes for a report.
     */
    fun toggleUpvote(
        reportId: String,
        increment: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        val delta = if (increment) 1L else -1L
        db.collection(COLLECTION_REPORTS).document(reportId)
            .update("upvotesCount", FieldValue.increment(delta))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Real-time listener for citizen profile stats (Karma score & total submitted reports).
     */
    fun listenToUserProfile(
        uid: String,
        onUpdate: (karmaPoints: Long, totalReports: Long, role: String) -> Unit
    ): ListenerRegistration? {
        if (uid.isEmpty()) return null

        return db.collection(COLLECTION_USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val karma = snapshot.getLong("karmaPoints") ?: 50L
                val total = snapshot.getLong("totalReportsCount") ?: 0L
                val role = snapshot.getString("role") ?: "CITIZEN"
                onUpdate(karma, total, role)
            }
    }

    /**
     * Listens to the user's most recently submitted report for real-time status tracking.
     */
    fun listenToUserLatestReport(
        userId: String,
        onUpdate: (GrievanceReport?) -> Unit
    ): ListenerRegistration? {
        if (userId.isEmpty()) return null

        return db.collection(COLLECTION_REPORTS)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    onUpdate(null)
                    return@addSnapshotListener
                }
                val doc = snapshot.documents.firstOrNull()
                val report = doc?.data?.let { GrievanceReport.fromFirestoreMap(it) }
                onUpdate(report)
            }
    }

    /**
     * Deletes a grievance report from Firestore and decrements the user's report counter.
     */
    fun deleteReport(
        reportId: String,
        userId: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection(COLLECTION_REPORTS).document(reportId)
            .delete()
            .addOnSuccessListener {
                if (!userId.isNullOrEmpty()) {
                    db.collection(COLLECTION_USERS).document(userId).update(
                        mapOf(
                            "totalReportsCount" to FieldValue.increment(-1)
                        )
                    )
                }
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.localizedMessage)
            }
    }
}

