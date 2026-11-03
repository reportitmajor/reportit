package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reportit.app.databinding.FragmentTrackBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.data.repository.FirestoreRepository
import com.reportit.app.data.session.SessionManager

class TrackFragment : Fragment() {

    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private var trackingListener: ListenerRegistration? = null
    private var currentActiveReportId: String = "RI-2026-004821"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: session.getFirebaseUid()

        if (currentUserId.isNotEmpty()) {
            trackingListener = FirestoreRepository.listenToUserLatestReport(currentUserId) { report ->
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    if (report != null) {
                        bindReportData(report)
                    }
                }
            }
        }

        setupVerificationActions()
    }

    private fun bindReportData(report: GrievanceReport) {
        currentActiveReportId = report.id
        binding.tvTrackingId.text = "Report #${report.id}"
        binding.tvTrackingStatusBadge.text = report.status.label
        binding.tvTrackingStatusBadge.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (report.status == ReportStatus.RESOLVED || report.status == ReportStatus.CITIZEN_CLOSED)
                R.color.status_resolved_bg
            else R.color.status_in_progress_bg
        )
        binding.tvTrackingStatusBadge.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (report.status == ReportStatus.RESOLVED || report.status == ReportStatus.CITIZEN_CLOSED)
                    R.color.status_resolved
                else R.color.status_in_progress
            )
        )

        binding.tvTrackingIssueTitle.text = "${report.category} • ${report.severity.label}"
        binding.tvTrackingLocation.text = "📍 ${report.locationAddress}"
        val officer = report.assignedOfficer ?: "Pending Assignment"
        binding.tvTrackingDept.text = "🏛️ ${report.department} • Assigned: $officer"

        if (report.status == ReportStatus.RESOLVED) {
            binding.layoutVerificationButtons.visibility = View.VISIBLE
            binding.tvVerificationFeedback.visibility = View.GONE
        }
    }

    private fun setupVerificationActions() {
        // 2-Way Citizen Resolution Verification: YES
        binding.btnVerifyYes.setOnClickListener {
            binding.layoutVerificationButtons.visibility = View.GONE
            binding.tvVerificationFeedback.visibility = View.VISIBLE
            binding.tvVerificationFeedback.text = "🎉 Resolution Confirmed! Ticket #$currentActiveReportId is officially CLOSED.\n+10 Civic Points added to your Citizen Score!"
            binding.tvVerificationFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_resolved))

            binding.tvTrackingStatusBadge.text = "CLOSED & VERIFIED"
            binding.tvTrackingStatusBadge.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_resolved_bg)
            binding.tvTrackingStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_resolved))

            FirestoreRepository.updateReportStatus(currentActiveReportId, ReportStatus.CITIZEN_CLOSED) {}

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("✅ Ticket Closed & Verified")
                .setMessage("Thank you for confirming the on-ground resolution. Your validation ensures municipal transparency and public accountability.")
                .setPositiveButton("Awesome", null)
                .show()
        }

        // 2-Way Citizen Resolution Verification: NO
        binding.btnVerifyNo.setOnClickListener {
            binding.layoutVerificationButtons.visibility = View.GONE
            binding.tvVerificationFeedback.visibility = View.VISIBLE
            binding.tvVerificationFeedback.text = "🚨 Resolution Rejected. Grievance reopened and escalated to Municipal Supervisor & SLA Monitoring Queue."
            binding.tvVerificationFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_critical))

            binding.tvTrackingStatusBadge.text = "REOPENED / ESCALATED"
            binding.tvTrackingStatusBadge.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_critical_bg)
            binding.tvTrackingStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_critical))

            FirestoreRepository.updateReportStatus(currentActiveReportId, ReportStatus.ESCALATED) {}

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🚨 Complaint Reopened & Escalated")
                .setMessage("We have notified the zonal executive engineer that the issue still exists. An emergency supervisor audit has been scheduled.")
                .setPositiveButton("Understood", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        trackingListener?.remove()
        trackingListener = null
        _binding = null
    }
}
