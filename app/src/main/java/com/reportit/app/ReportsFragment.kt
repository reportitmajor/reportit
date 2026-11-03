package com.reportit.app

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.reportit.app.R
import com.reportit.app.data.GrievanceDataProvider
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportSeverity
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.data.model.TimelineEvent
import com.reportit.app.data.repository.FirestoreRepository
import com.reportit.app.data.session.SessionManager
import com.reportit.app.databinding.FragmentReportsBinding
import com.reportit.app.ui.reports.GrievanceReportAdapter

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GrievanceReportAdapter
    private val allReports: MutableList<GrievanceReport> = mutableListOf()
    private var activeFilter = "ALL"
    private var searchQuery = ""

    private var currentlySelectedReport: GrievanceReport? = null
    private var isViewingResolvedPhoto = false
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndFilters()
        setupDetailActions()
        updateKpiSummary()
        applyFiltersAndSearch()
        startLiveReportsListener()
    }

    private fun startLiveReportsListener() {
        firestoreListener = FirestoreRepository.listenToAllReports(
            onUpdate = { cloudReports ->
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    allReports.clear()
                    allReports.addAll(cloudReports)
                    applyFiltersAndSearch()
                }
            },
            onError = { _ ->
                // Offline handling
            }
        )
    }

    private fun setupRecyclerView() {
        adapter = GrievanceReportAdapter(
            onItemClick = { report ->
                openReportDetail(report)
            },
            onUpvoteClick = { report ->
                handleUpvote(report)
            }
        )

        binding.rvReportsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReportsList.adapter = adapter
    }

    private fun handleUpvote(report: GrievanceReport) {
        val index = allReports.indexOfFirst { it.id == report.id }
        if (index != -1) {
            val target = allReports[index]
            if (target.isUserUpvoted) {
                target.isUserUpvoted = false
                target.upvotesCount = maxOf(0, target.upvotesCount - 1)
                Toast.makeText(requireContext(), "Upvote removed for ${target.id}", Toast.LENGTH_SHORT).show()
                FirestoreRepository.toggleUpvote(target.id, false) {}
            } else {
                target.isUserUpvoted = true
                target.upvotesCount += 1
                Toast.makeText(requireContext(), "You and ${target.upvotesCount - 1} citizens validated ${target.id}", Toast.LENGTH_SHORT).show()
                FirestoreRepository.toggleUpvote(target.id, true) {}
            }
            applyFiltersAndSearch()
        }
    }

    private fun setupSearchAndFilters() {
        // Toggle Search Bar
        binding.btnSearchReports.setOnClickListener {
            if (binding.layoutSearchBar.visibility == View.VISIBLE) {
                binding.layoutSearchBar.visibility = View.GONE
                binding.etSearchInput.setText("")
                searchQuery = ""
                applyFiltersAndSearch()
            } else {
                binding.layoutSearchBar.visibility = View.VISIBLE
                binding.etSearchInput.requestFocus()
            }
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearchInput.setText("")
            searchQuery = ""
            applyFiltersAndSearch()
        }

        binding.etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                binding.btnClearSearch.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                applyFiltersAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnFilterReports.setOnClickListener {
            showDepartmentFilterDialog()
        }

        // Filter Pills
        binding.filterPillAll.setOnClickListener { setFilter("ALL") }
        binding.filterPillActive.setOnClickListener { setFilter("ACTIVE") }
        binding.filterPillNeedsAction.setOnClickListener { setFilter("NEEDS_ACTION") }
        binding.filterPillResolved.setOnClickListener { setFilter("RESOLVED") }
        binding.filterPillEscalated.setOnClickListener { setFilter("ESCALATED") }

        // Summary Card Click shortcuts
        binding.summaryStatActive.setOnClickListener { setFilter("ACTIVE") }
        binding.summaryStatNeedsAction.setOnClickListener { setFilter("NEEDS_ACTION") }
        binding.summaryStatResolved.setOnClickListener { setFilter("RESOLVED") }
        binding.summaryStatEscalated.setOnClickListener { setFilter("ESCALATED") }

        binding.btnResetFilters.setOnClickListener {
            activeFilter = "ALL"
            searchQuery = ""
            binding.etSearchInput.setText("")
            binding.layoutSearchBar.visibility = View.GONE
            updatePillVisuals()
            applyFiltersAndSearch()
        }
    }

    private fun setFilter(filter: String) {
        activeFilter = filter
        updatePillVisuals()
        applyFiltersAndSearch()
    }

    private fun updatePillVisuals() {
        updateSinglePill(binding.filterPillAll, activeFilter == "ALL")
        updateSinglePill(binding.filterPillActive, activeFilter == "ACTIVE")
        updateSinglePill(binding.filterPillNeedsAction, activeFilter == "NEEDS_ACTION")
        updateSinglePill(binding.filterPillResolved, activeFilter == "RESOLVED")
        updateSinglePill(binding.filterPillEscalated, activeFilter == "ESCALATED")
    }

    private fun updateSinglePill(pill: TextView, isSelected: Boolean) {
        if (isSelected) {
            pill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#006494"))
            pill.setTextColor(Color.WHITE)
        } else {
            pill.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            pill.setTextColor(Color.parseColor("#64748B"))
        }
    }

    private fun applyFiltersAndSearch() {
        var filteredList = allReports.filter { report ->
            when (activeFilter) {
                "ACTIVE" -> report.status == ReportStatus.IN_PROGRESS || report.status == ReportStatus.ASSIGNED || report.status == ReportStatus.AI_ANALYZED
                "NEEDS_ACTION" -> report.status == ReportStatus.RESOLVED
                "RESOLVED" -> report.status == ReportStatus.CITIZEN_CLOSED
                "ESCALATED" -> report.status == ReportStatus.ESCALATED || report.slaRemainingHours < 0
                else -> true
            }
        }

        if (searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter { report ->
                report.id.contains(searchQuery, ignoreCase = true) ||
                report.title.contains(searchQuery, ignoreCase = true) ||
                report.description.contains(searchQuery, ignoreCase = true) ||
                report.wardName.contains(searchQuery, ignoreCase = true) ||
                report.department.contains(searchQuery, ignoreCase = true) ||
                report.locationAddress.contains(searchQuery, ignoreCase = true)
            }
        }

        adapter.submitList(filteredList.toList())

        // Feed results count text
        binding.tvFeedCount.text = "Showing ${filteredList.size} of ${allReports.size} items"

        // Empty state visibility
        if (filteredList.isEmpty()) {
            binding.rvReportsList.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvReportsList.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }

        updateKpiSummary()
    }

    private fun updateKpiSummary() {
        val total = allReports.size
        val active = allReports.count { it.status == ReportStatus.IN_PROGRESS || it.status == ReportStatus.ASSIGNED || it.status == ReportStatus.AI_ANALYZED }
        val needsAction = allReports.count { it.status == ReportStatus.RESOLVED }
        val closed = allReports.count { it.status == ReportStatus.CITIZEN_CLOSED }
        val escalated = allReports.count { it.status == ReportStatus.ESCALATED || it.slaRemainingHours < 0 }

        binding.tvTotalReportsBadge.text = "$total Total"
        binding.tvSummaryCountActive.text = active.toString()
        binding.tvSummaryCountNeedsAction.text = needsAction.toString()
        binding.tvSummaryCountResolved.text = closed.toString()
        binding.tvSummaryCountEscalated.text = escalated.toString()
    }

    private fun showDepartmentFilterDialog() {
        val departments = arrayOf(
            "All Municipal Divisions",
            "BBMP Road Infrastructure Division",
            "BBMP Solid Waste Management (SWM)",
            "Bangalore Water Supply (BWSSB)",
            "Bangalore Electricity (BESCOM)"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter by Municipal Division")
            .setItems(departments) { dialog, which ->
                if (which == 0) {
                    searchQuery = ""
                    binding.etSearchInput.setText("")
                } else {
                    val selectedDept = departments[which]
                    searchQuery = selectedDept.split(" ")[0]
                    binding.etSearchInput.setText(searchQuery)
                    binding.layoutSearchBar.visibility = View.VISIBLE
                }
                applyFiltersAndSearch()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ================= DETAIL VIEW IMPLEMENTATION =================
    private fun openReportDetail(report: GrievanceReport) {
        currentlySelectedReport = report
        isViewingResolvedPhoto = false

        binding.tvDetailHeaderId.text = "Report ${report.id}"
        binding.tvDetailHeaderCategory.text = report.category
        binding.tvDetailTitle.text = report.title
        binding.tvDetailDescription.text = report.description
        binding.tvDetailLocation.text = report.locationAddress
        binding.tvDetailWard.text = report.wardName
        binding.tvDetailOfficer.text = report.assignedOfficer ?: "Junior Engineer (BBMP Zonal Control)"
        binding.tvDetailCrew.text = report.assignedCrew ?: "Municipal Field Crew"

        // Severity & Confidence Badges
        binding.tvDetailSeverityBadge.text = "${report.severity.label} Severity"
        binding.tvDetailSeverityBadge.setTextColor(Color.parseColor(report.severity.colorHex))
        binding.tvDetailSeverityBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.severity.bgHex))

        binding.tvDetailConfidenceBadge.text = "AI Confidence: ${report.aiConfidence}%"

        // Status Pill
        binding.tvDetailStatusPill.text = report.status.label
        binding.tvDetailStatusPill.setTextColor(Color.parseColor(report.status.colorHex))
        binding.tvDetailStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.status.bgHex))

        // SLA Text
        if (report.status == ReportStatus.CITIZEN_CLOSED) {
            binding.tvDetailSlaTarget.text = "Completed & Verified"
            binding.tvDetailSlaTarget.setTextColor(Color.parseColor("#059669"))
        } else if (report.status == ReportStatus.ESCALATED || report.slaRemainingHours < 0) {
            val breachHours = kotlin.math.abs(report.slaRemainingHours)
            binding.tvDetailSlaTarget.text = "Breached by ${breachHours}h (Escalated)"
            binding.tvDetailSlaTarget.setTextColor(Color.parseColor("#DC2626"))
        } else {
            binding.tvDetailSlaTarget.text = "${report.slaTotalHours} Hours (${report.slaRemainingHours}h Left)"
            binding.tvDetailSlaTarget.setTextColor(Color.parseColor("#2563EB"))
        }

        // Photo View
        updateDetailPhotoDisplay(report)

        // 2-Way Citizen Verification Box
        setupTwoWayVerificationBox(report)

        // Render Timeline
        renderTimelineEvents(report.timeline)

        // Check ownership for Delete capability
        val session = SessionManager(requireContext())
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: session.getFirebaseUid()
        val currentUserEmail = session.getUserEmail()
        val currentUserName = session.getUserName()

        val isOwner = (report.userId != null && report.userId == currentUserId)
                || (!report.userEmail.isNullOrEmpty() && report.userEmail.equals(currentUserEmail, ignoreCase = true))
                || report.reportedBy.contains("You", ignoreCase = true)
                || (currentUserName.isNotEmpty() && report.reportedBy.equals(currentUserName, ignoreCase = true))
                || report.userId.isNullOrEmpty()

        if (isOwner) {
            binding.btnDetailDelete.visibility = View.VISIBLE
            binding.btnDeleteReport.visibility = View.VISIBLE
        } else {
            binding.btnDetailDelete.visibility = View.GONE
            binding.btnDeleteReport.visibility = View.GONE
        }

        // Switch to detail view
        binding.layoutReportsList.visibility = View.GONE
        binding.layoutReportDetail.visibility = View.VISIBLE
    }

    private fun updateDetailPhotoDisplay(report: GrievanceReport) {
        if (isViewingResolvedPhoto && (report.resolvedImageResId != null || !report.resolutionPhotoUrl.isNullOrEmpty())) {
            if (!report.resolutionPhotoUrl.isNullOrEmpty()) {
                com.reportit.app.utils.ImageUtils.loadUrlToImageView(
                    binding.ivDetailPhoto,
                    report.resolutionPhotoUrl,
                    report.resolvedImageResId ?: com.reportit.app.R.drawable.img_pothole_evidence
                )
            } else if (report.resolvedImageResId != null) {
                binding.ivDetailPhoto.setImageResource(report.resolvedImageResId)
            }
            binding.tvPhotoCaptionOverlay.text = report.resolutionNotes ?: "Municipal repair proof submitted by field crew"
            binding.tvPhotoProofStatus.text = "Resolution Proof"
            binding.tvPhotoProofStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
            binding.tvPhotoProofStatus.setTextColor(Color.parseColor("#059669"))

            binding.tabResolvedPhoto.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            binding.tabResolvedPhoto.setTextColor(Color.parseColor("#0F172A"))
            binding.tabOriginalPhoto.backgroundTintList = null
            binding.tabOriginalPhoto.setTextColor(Color.parseColor("#64748B"))
        } else {
            if (!report.imageUrl.isNullOrEmpty()) {
                com.reportit.app.utils.ImageUtils.loadUrlToImageView(
                    binding.ivDetailPhoto,
                    report.imageUrl,
                    report.originalImageResId
                )
            } else if (!report.imageBase64.isNullOrEmpty()) {
                val bitmap = com.reportit.app.utils.ImageUtils.base64ToBitmap(report.imageBase64)
                if (bitmap != null) {
                    binding.ivDetailPhoto.setImageBitmap(bitmap)
                } else {
                    binding.ivDetailPhoto.setImageResource(report.originalImageResId)
                }
            } else {
                binding.ivDetailPhoto.setImageResource(report.originalImageResId)
            }
            binding.tvPhotoCaptionOverlay.text = "Initial citizen capture on ${report.submissionDate}"
            binding.tvPhotoProofStatus.text = "GPS Locked"
            binding.tvPhotoProofStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
            binding.tvPhotoProofStatus.setTextColor(Color.parseColor("#2563EB"))

            binding.tabOriginalPhoto.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            binding.tabOriginalPhoto.setTextColor(Color.parseColor("#0F172A"))
            binding.tabResolvedPhoto.backgroundTintList = null
            binding.tabResolvedPhoto.setTextColor(Color.parseColor("#64748B"))
        }
    }

    private fun setupTwoWayVerificationBox(report: GrievanceReport) {
        when (report.status) {
            ReportStatus.RESOLVED -> {
                binding.cardTwoWayVerification.visibility = View.VISIBLE
                binding.layoutVerificationButtons.visibility = View.VISIBLE
                binding.tvVerificationOutcomeText.visibility = View.GONE
                binding.tvVerificationInstruction.text =
                    "The municipal division has reported this grievance as resolved. Please inspect the on-ground work and confirm or dispute resolution."

                binding.btnConfirmResolution.setOnClickListener {
                    confirmResolution(report)
                }

                binding.btnDisputeResolution.setOnClickListener {
                    disputeResolution(report)
                }
            }
            ReportStatus.CITIZEN_CLOSED -> {
                binding.cardTwoWayVerification.visibility = View.VISIBLE
                binding.layoutVerificationButtons.visibility = View.GONE
                binding.tvVerificationOutcomeText.visibility = View.VISIBLE
                binding.tvVerificationOutcomeText.text = "Resolution Verified and Closed by Citizen. +15 Civic Score Awarded."
                binding.tvVerificationInstruction.text = "This civic ticket is officially closed and verified on-ground."
            }
            ReportStatus.ESCALATED -> {
                binding.cardTwoWayVerification.visibility = View.VISIBLE
                binding.layoutVerificationButtons.visibility = View.GONE
                binding.tvVerificationOutcomeText.visibility = View.VISIBLE
                binding.tvVerificationOutcomeText.text = "Grievance Escalated to Zonal Chief Engineer & Ward Committee."
                binding.tvVerificationOutcomeText.setTextColor(Color.parseColor("#DC2626"))
                binding.tvVerificationOutcomeText.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                binding.tvVerificationInstruction.text = "Statutory SLA breached or on-ground dispute registered. Priority inspection in progress."
            }
            else -> {
                binding.cardTwoWayVerification.visibility = View.GONE
            }
        }
    }

    private fun confirmResolution(report: GrievanceReport) {
        report.status = ReportStatus.CITIZEN_CLOSED
        report.slaRemainingHours = 0
        report.timeline.add(
            TimelineEvent(
                stageTitle = "Citizen Verified & Closed",
                timestamp = "Just Now",
                description = "Citizen verified satisfactory on-ground repair work. +15 Civic Score awarded.",
                actor = "You (Citizen Reporter)",
                isCompleted = true,
                isCurrent = true
            )
        )

        // Update UI
        binding.layoutVerificationButtons.visibility = View.GONE
        binding.tvVerificationOutcomeText.visibility = View.VISIBLE
        binding.tvVerificationOutcomeText.text = "Resolution Verified and Closed by Citizen. +15 Civic Score Awarded."
        binding.tvVerificationInstruction.text = "Thank you for confirming resolution. Your verification closes the accountability loop."

        binding.tvDetailStatusPill.text = report.status.label
        binding.tvDetailStatusPill.setTextColor(Color.parseColor(report.status.colorHex))
        binding.tvDetailStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.status.bgHex))
        binding.tvDetailSlaTarget.text = "Completed & Verified"
        binding.tvDetailSlaTarget.setTextColor(Color.parseColor("#059669"))

        renderTimelineEvents(report.timeline)
        applyFiltersAndSearch()

        // Sync 2-Way Citizen Verification to Firestore
        com.reportit.app.data.repository.FirestoreRepository.updateReportStatus(
            reportId = report.id,
            newStatus = ReportStatus.CITIZEN_CLOSED
        ) { success ->
            android.util.Log.d("ReportsFragment", "Firestore citizen close sync: $success")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ticket Closed & Verified")
            .setMessage("Thank you for confirming on-ground completion of Report ${report.id}. 15 Civic Reputation Points have been credited to your Citizen Profile.")
            .setPositiveButton("Done", null)
            .show()
    }

    private fun disputeResolution(report: GrievanceReport) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Dispute Resolution")
            .setMessage("Are you sure this issue was not properly fixed? Confirming will reopen the ticket and escalate directly to the Zonal Executive Engineer.")
            .setPositiveButton("Yes, Escalate Ticket") { _, _ ->
                report.status = ReportStatus.ESCALATED
                report.isDisputed = true
                report.timeline.add(
                    TimelineEvent(
                        stageTitle = "Citizen Disputed & Escalated",
                        timestamp = "Just Now",
                        description = "Citizen rejected resolution proof. Immediate penalty flag and re-inspection assigned to Chief Engineer.",
                        actor = "You (Citizen Reporter)",
                        isCompleted = false,
                        isCurrent = true
                    )
                )

                binding.layoutVerificationButtons.visibility = View.GONE
                binding.tvVerificationOutcomeText.visibility = View.VISIBLE
                binding.tvVerificationOutcomeText.text = "Grievance Reopened & Escalated to Zonal Chief Engineer."
                binding.tvVerificationOutcomeText.setTextColor(Color.parseColor("#DC2626"))
                binding.tvVerificationOutcomeText.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))

                binding.tvDetailStatusPill.text = report.status.label
                binding.tvDetailStatusPill.setTextColor(Color.parseColor(report.status.colorHex))
                binding.tvDetailStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.status.bgHex))

                renderTimelineEvents(report.timeline)
                applyFiltersAndSearch()

                // Sync Dispute Escalation to Firestore
                com.reportit.app.data.repository.FirestoreRepository.updateReportStatus(
                    reportId = report.id,
                    newStatus = ReportStatus.ESCALATED
                ) { success ->
                    android.util.Log.d("ReportsFragment", "Firestore dispute escalation sync: $success")
                }

                Toast.makeText(requireContext(), "Ticket ${report.id} reopened with high-priority escalation.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renderTimelineEvents(timeline: List<TimelineEvent>) {
        binding.layoutTimelineContainer.removeAllViews()

        for (i in timeline.indices) {
            val event = timeline[i]
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_timeline_step, binding.layoutTimelineContainer, false)

            val tvTitle = itemView.findViewById<TextView>(R.id.tvTimelineTitle)
            val tvTime = itemView.findViewById<TextView>(R.id.tvTimelineTime)
            val tvDesc = itemView.findViewById<TextView>(R.id.tvTimelineDescription)
            val tvActor = itemView.findViewById<TextView>(R.id.tvTimelineActor)
            val viewIndicator = itemView.findViewById<View>(R.id.viewTimelineIndicator)
            val lineConnecting = itemView.findViewById<View>(R.id.lineConnecting)

            tvTitle.text = event.stageTitle
            tvTime.text = event.timestamp
            tvDesc.text = event.description
            tvActor.text = "Action: ${event.actor}"

            if (event.isCurrent) {
                viewIndicator.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2563EB"))
                tvTitle.setTextColor(Color.parseColor("#2563EB"))
            } else if (event.isCompleted) {
                viewIndicator.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#059669"))
                tvTitle.setTextColor(Color.parseColor("#0F172A"))
            } else {
                viewIndicator.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CBD5E1"))
                tvTitle.setTextColor(Color.parseColor("#94A3B8"))
            }

            if (i == timeline.size - 1) {
                lineConnecting.visibility = View.GONE
            } else {
                lineConnecting.visibility = View.VISIBLE
            }

            binding.layoutTimelineContainer.addView(itemView)
        }
    }

    private fun setupDetailActions() {
        binding.btnBackFromDetail.setOnClickListener {
            binding.layoutReportDetail.visibility = View.GONE
            binding.layoutReportsList.visibility = View.VISIBLE
        }

        binding.tabOriginalPhoto.setOnClickListener {
            isViewingResolvedPhoto = false
            currentlySelectedReport?.let { updateDetailPhotoDisplay(it) }
        }

        binding.tabResolvedPhoto.setOnClickListener {
            if (currentlySelectedReport?.resolvedImageResId != null || !currentlySelectedReport?.resolutionPhotoUrl.isNullOrEmpty()) {
                isViewingResolvedPhoto = true
                currentlySelectedReport?.let { updateDetailPhotoDisplay(it) }
            } else {
                Toast.makeText(requireContext(), "Municipal completion proof not yet submitted for this grievance.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDetailShare.setOnClickListener {
            currentlySelectedReport?.let { report ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Tracking Civic Grievance ${report.id} on Report-It: ${report.title} (${report.wardName}). Status: ${report.status.label}."
                    )
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Share Civic Grievance"))
            }
        }

        binding.btnDetailDelete.setOnClickListener {
            currentlySelectedReport?.let { report ->
                confirmAndDeleteReport(report)
            }
        }

        binding.btnDeleteReport.setOnClickListener {
            currentlySelectedReport?.let { report ->
                confirmAndDeleteReport(report)
            }
        }

        binding.btnDetailMore.setOnClickListener {
            currentlySelectedReport?.let { report ->
                val session = SessionManager(requireContext())
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: session.getFirebaseUid()
                val isOwner = (report.userId != null && report.userId == currentUserId)
                        || report.reportedBy.contains("You", ignoreCase = true)
                        || report.userId.isNullOrEmpty()

                val options = if (isOwner) {
                    arrayOf(
                        "Copy Tracking Link",
                        "Download Official PDF Receipt",
                        "Contact Nodal Officer (${report.department.split(" ")[0]})",
                        "Request Emergency Re-route",
                        "Withdraw & Delete Grievance"
                    )
                } else {
                    arrayOf(
                        "Copy Tracking Link",
                        "Download Official PDF Receipt",
                        "Contact Nodal Officer (${report.department.split(" ")[0]})",
                        "Request Emergency Re-route"
                    )
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Grievance Options")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> Toast.makeText(requireContext(), "Tracking link for ${report.id} copied to clipboard", Toast.LENGTH_SHORT).show()
                            1 -> Toast.makeText(requireContext(), "Generating official digital receipt PDF for ${report.id}...", Toast.LENGTH_SHORT).show()
                            2 -> Toast.makeText(requireContext(), "Connecting to ${report.assignedOfficer}...", Toast.LENGTH_LONG).show()
                            3 -> Toast.makeText(requireContext(), "Re-routing request logged with Central Control Room", Toast.LENGTH_SHORT).show()
                            4 -> confirmAndDeleteReport(report)
                        }
                    }
                    .setNegativeButton("Close", null)
                    .show()
            }
        }
    }

    private fun confirmAndDeleteReport(report: GrievanceReport) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Withdraw & Delete Grievance?")
            .setMessage("Are you sure you want to delete Report ${report.id} (${report.title})? This will permanently remove it from the municipal registry and cannot be undone.")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Delete Permanently") { dialog, _ ->
                dialog.dismiss()
                performDeleteReport(report)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeleteReport(report: GrievanceReport) {
        val ctx = context ?: return
        Toast.makeText(ctx, "Deleting report ${report.id}...", Toast.LENGTH_SHORT).show()

        val session = SessionManager(ctx)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: session.getFirebaseUid()

        FirestoreRepository.deleteReport(report.id, currentUserId) { success, error ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread

                // Remove locally from allReports list
                allReports.removeAll { it.id == report.id }

                // Return to feed
                binding.layoutReportDetail.visibility = View.GONE
                binding.layoutReportsList.visibility = View.VISIBLE

                applyFiltersAndSearch()

                if (success) {
                    Toast.makeText(ctx, "Report ${report.id} was successfully withdrawn and deleted.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(ctx, "Report removed locally (${error ?: "offline"}).", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener?.remove()
        firestoreListener = null
        _binding = null
    }
}
