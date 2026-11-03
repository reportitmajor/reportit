package com.reportit.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import com.reportit.app.R
import com.reportit.app.data.GrievanceDataProvider
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportSeverity
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.data.repository.FirestoreRepository
import com.reportit.app.databinding.FragmentMapBinding
import com.reportit.app.utils.ImageUtils

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var firestoreListener: ListenerRegistration? = null
    private val allRealReports: MutableList<GrievanceReport> = mutableListOf()
    private var currentlySelectedReport: GrievanceReport? = null
    private var activeLayer = "Civic Issues"
    private var isBookmarked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDetailSheetActions()
        setupTopAndFloatingControls()
        startLiveReportsSync()
    }

    private fun startLiveReportsSync() {
        // 1. Fetch current GPS location and update user location beacon
        com.reportit.app.utils.LocationHelper.fetchCurrentLocation(requireContext()) { lat, lon, _ ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.interactiveMapView.setUserLocation(lat, lon, centerOnIt = false)
            }
        }

        // 2. Connect real-time Firestore synchronization for authentic citizen reports
        firestoreListener = FirestoreRepository.listenToAllReports(
            onUpdate = { cloudReports ->
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    allRealReports.clear()
                    allRealReports.addAll(cloudReports)
                    binding.interactiveMapView.setReports(allRealReports)
                    populateNearbySheet(allRealReports)

                    // Auto-center on primary authentic issue cluster if not already centered
                    allRealReports.firstOrNull { it.latitude != 0.0 && it.longitude != 0.0 }?.let { primary ->
                        binding.interactiveMapView.centerOnCoordinates(primary.latitude, primary.longitude, animated = true)
                    }
                }
            },
            onError = { _ ->
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    binding.interactiveMapView.setReports(allRealReports)
                    populateNearbySheet(allRealReports)
                }
            }
        )
    }

    private fun populateNearbySheet(reports: List<GrievanceReport>) {
        val container = binding.containerNearbyCards
        container.removeAllViews()

        if (reports.isEmpty()) {
            binding.tvNearbyEmptyState.visibility = View.VISIBLE
            return
        }

        binding.tvNearbyEmptyState.visibility = View.GONE
        val density = resources.displayMetrics.density

        // Display up to 10 most recent/relevant reports
        val displayList = reports.take(10)

        for (report in displayList) {
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (8 * density).toInt()
                }
                radius = 14 * density
                strokeWidth = (1 * density).toInt()
                strokeColor = Color.parseColor("#E2E8F0")
                setCardBackgroundColor(Color.WHITE)
                elevation = 0f
                isClickable = true
                isFocusable = true
            }

            val cardRow = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding((10 * density).toInt(), (10 * density).toInt(), (10 * density).toInt(), (10 * density).toInt())
            }

            // Thumbnail card
            val thumbCard = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams((60 * density).toInt(), (60 * density).toInt())
                radius = 10 * density
                strokeWidth = 0
            }
            val thumbImg = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            if (!report.imageUrl.isNullOrEmpty()) {
                ImageUtils.loadUrlToImageView(thumbImg, report.imageUrl, report.originalImageResId)
            } else if (!report.imageBase64.isNullOrEmpty()) {
                val bmp = ImageUtils.base64ToBitmap(report.imageBase64)
                if (bmp != null) thumbImg.setImageBitmap(bmp) else thumbImg.setImageResource(report.originalImageResId)
            } else {
                thumbImg.setImageResource(report.originalImageResId)
            }
            thumbCard.addView(thumbImg)
            cardRow.addView(thumbCard)

            // Content column
            val contentCol = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (12 * density).toInt()
                }
                orientation = LinearLayout.VERTICAL
            }

            // Title and severity badge row
            val titleRow = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val categoryIconBadge = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams((20 * density).toInt(), (20 * density).toInt())
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(report.severity.colorHex))
                }
            }
            val catIcon = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams((11 * density).toInt(), (11 * density).toInt(), android.view.Gravity.CENTER)
                setImageResource(getCategoryIconRes(report.category))
                imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
            categoryIconBadge.addView(catIcon)
            titleRow.addView(categoryIconBadge)

            val tvTitle = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (6 * density).toInt()
                }
                text = report.title
                textSize = 13f
                setTextColor(Color.parseColor("#0F172A"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            titleRow.addView(tvTitle)

            val tvSeverity = TextView(requireContext()).apply {
                text = report.severity.label
                textSize = 9f
                setTextColor(Color.parseColor(report.severity.colorHex))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 6 * density
                    setColor(Color.parseColor(report.severity.bgHex))
                }
                setPadding((6 * density).toInt(), (2 * density).toInt(), (6 * density).toInt(), (2 * density).toInt())
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            titleRow.addView(tvSeverity)
            contentCol.addView(titleRow)

            // Ward & date
            val tvLocation = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = (2 * density).toInt()
                }
                text = "${report.wardName} • ${report.submissionDate.split(",").firstOrNull() ?: report.submissionDate}"
                textSize = 11f
                setTextColor(Color.parseColor("#64748B"))
            }
            contentCol.addView(tvLocation)

            // Status chip
            val statusRow = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = (3 * density).toInt()
                }
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val statusDot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams((6 * density).toInt(), (6 * density).toInt())
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(report.status.colorHex))
                }
            }
            val tvStatus = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = (4 * density).toInt()
                }
                text = report.status.label
                textSize = 10f
                setTextColor(Color.parseColor(report.status.colorHex))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            statusRow.addView(statusDot)
            statusRow.addView(tvStatus)
            contentCol.addView(statusRow)

            cardRow.addView(contentCol)

            val chevron = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams((16 * density).toInt(), (16 * density).toInt())
                setImageResource(R.drawable.ic_arrow_right_chevron)
                imageTintList = ColorStateList.valueOf(Color.parseColor("#94A3B8"))
            }
            cardRow.addView(chevron)

            card.addView(cardRow)
            card.setOnClickListener {
                showSelectedIssueDetail(report)
                if (report.latitude != 0.0 && report.longitude != 0.0) {
                    binding.interactiveMapView.centerOnCoordinates(report.latitude, report.longitude, animated = true)
                }
            }
            container.addView(card)
        }
    }

    private fun showSelectedIssueDetail(report: GrievanceReport) {
        currentlySelectedReport = report
        binding.interactiveMapView.setSelectedReport(report, centerOnIt = true)

        // Title and Severity
        binding.tvDetailSheetTitle.text = report.title
        binding.tvDetailSheetSeverity.text = report.severity.label
        binding.tvDetailSheetSeverity.setTextColor(Color.parseColor(report.severity.colorHex))
        binding.tvDetailSheetSeverity.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.severity.bgHex))

        // Category Icon & Badge
        binding.ivDetailCategoryIcon.setImageResource(getCategoryIconRes(report.category))
        binding.layoutDetailCategoryBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.severity.colorHex))

        // Subtitle & Location
        binding.tvDetailSubtitle.text = "Tracking ID: ${report.id}"
        binding.tvDetailLocation.text = "${report.locationAddress.ifEmpty { report.wardName }}"

        // Status chip
        binding.tvDetailStatusChip.text = report.status.label
        binding.tvDetailStatusChip.setTextColor(Color.parseColor(report.status.colorHex))
        binding.dotDetailStatusIndicator.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.status.colorHex))
        binding.tvDetailUpdatedTime.text = report.submissionDate.split(",").firstOrNull() ?: report.submissionDate

        // Info Grid: Authority, Status, SLA, Severity
        binding.tvDetailAuthority.text = when {
            report.department.contains("BBMP", true) -> "BBMP"
            report.department.contains("BESCOM", true) -> "BESCOM"
            report.department.contains("BWSSB", true) -> "BWSSB"
            else -> "Municipal Board"
        }
        binding.tvDetailDepartment.text = report.department
        binding.tvDetailStatus.text = report.status.label
        binding.tvDetailStatus.setTextColor(Color.parseColor(report.status.colorHex))
        binding.tvDetailSla.text = if (report.slaRemainingHours > 0) "${report.slaRemainingHours}h Left" else "Closed"
        binding.tvDetailSeverityValue.text = report.severity.label
        binding.tvDetailSeverityValue.setTextColor(Color.parseColor(report.severity.colorHex))

        // Overview
        binding.tvDetailOverview.text = report.description.ifEmpty {
            "Civic grievance registered at ${report.locationAddress}. Inspection and resolution assigned to ${report.department}."
        }

        // Thumbnail photo
        if (!report.imageUrl.isNullOrEmpty()) {
            ImageUtils.loadUrlToImageView(binding.ivDetailThumbnail, report.imageUrl, report.originalImageResId)
        } else if (!report.imageBase64.isNullOrEmpty()) {
            val bmp = ImageUtils.base64ToBitmap(report.imageBase64)
            if (bmp != null) binding.ivDetailThumbnail.setImageBitmap(bmp) else binding.ivDetailThumbnail.setImageResource(report.originalImageResId)
        } else {
            binding.ivDetailThumbnail.setImageResource(report.originalImageResId)
        }

        // Timeline Update
        val latestTimeline = report.timeline.lastOrNull()
        if (latestTimeline != null) {
            binding.tvDetailUpdate1.text = "${latestTimeline.stageTitle}: ${latestTimeline.description}"
            binding.tvDetailUpdate1Date.text = latestTimeline.timestamp
        } else {
            binding.tvDetailUpdate1.text = "Report verified and assigned to ${report.department}."
            binding.tvDetailUpdate1Date.text = report.submissionDate
        }

        // Expand Bottom Sheet
        binding.bottomSheetSelectedDetail.visibility = View.VISIBLE
        val behavior = BottomSheetBehavior.from(binding.bottomSheetSelectedDetail)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideSelectedIssueDetail() {
        if (_binding == null) return
        val behavior = BottomSheetBehavior.from(binding.bottomSheetSelectedDetail)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.bottomSheetSelectedDetail.visibility = View.GONE
        binding.interactiveMapView.setSelectedReport(null)
        currentlySelectedReport = null
    }

    private fun getCategoryIconRes(category: String): Int {
        val cat = category.lowercase()
        return when {
            cat.contains("pothole") || cat.contains("road") -> R.drawable.ic_pothole_road
            cat.contains("waste") || cat.contains("garbage") -> R.drawable.ic_solid_waste
            cat.contains("light") || cat.contains("street") || cat.contains("bescom") -> R.drawable.ic_street_light
            cat.contains("water") || cat.contains("sewer") || cat.contains("drain") || cat.contains("manhole") -> R.drawable.ic_water_sewer
            else -> R.drawable.ic_radar_target
        }
    }

    private fun setupDetailSheetActions() {
        binding.btnCloseDetailSheet.setOnClickListener {
            hideSelectedIssueDetail()
        }

        binding.btnSheetGetDirections.setOnClickListener {
            val report = currentlySelectedReport
            if (report != null && report.latitude != 0.0 && report.longitude != 0.0) {
                val uri = Uri.parse("geo:${report.latitude},${report.longitude}?q=${report.latitude},${report.longitude}(${Uri.encode(report.title)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${report.latitude},${report.longitude}"))
                    startActivity(browserIntent)
                }
            } else {
                Toast.makeText(requireContext(), "Location coordinates not available for navigation", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSheetViewDetails.setOnClickListener {
            val report = currentlySelectedReport
            if (report != null) {
                Toast.makeText(requireContext(), "Audit trail: Ticket #${report.id} • ${report.department}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSheetShareIssue.setOnClickListener {
            val report = currentlySelectedReport
            if (report != null) {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Bengaluru Civic Alert: ${report.title} reported at ${report.locationAddress} on Report-It (Status: ${report.status.label}).")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Share Civic Issue"))
            }
        }

        binding.btnSheetBookmark.setOnClickListener {
            isBookmarked = !isBookmarked
            if (isBookmarked) {
                Toast.makeText(requireContext(), "Issue saved to your Watchlist!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Issue removed from Watchlist", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnViewTimelineLink.setOnClickListener {
            val report = currentlySelectedReport
            val timelineMsg = if (report != null && report.timeline.isNotEmpty()) {
                report.timeline.joinToString("\n\n") { "• ${it.stageTitle} (${it.timestamp}):\n  ${it.description}" }
            } else {
                "• Ticket Created on ${report?.submissionDate ?: "Today"}\n• AI Verification verified (${report?.aiConfidence ?: 94}% confidence)\n• SLA: ${report?.slaRemainingHours ?: 48} Hours"
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Issue Progress Timeline")
                .setMessage(timelineMsg)
                .setPositiveButton("Close", null)
                .show()
        }
    }

    private fun setupTopAndFloatingControls() {
        binding.btnHeaderSearch.setOnClickListener { showMapSearchDialog() }
        binding.btnHeaderFilter.setOnClickListener { showMapFilterDialog() }
        binding.cardLocationSelector.setOnClickListener { showWardSelectorDialog() }
        binding.btnMapRecenterTop.setOnClickListener { recenterMapLocation() }
        binding.cardMapSearchBar.setOnClickListener { showMapSearchDialog() }
        binding.btnMapLayers.setOnClickListener { showLayerSwitcherDialog() }
        binding.btnMapRecenterFloat.setOnClickListener { recenterMapLocation() }
        binding.btnMapZoomIn.setOnClickListener { binding.interactiveMapView.zoomIn() }
        binding.btnMapZoomOut.setOnClickListener { binding.interactiveMapView.zoomOut() }
        binding.btnMapSeeAll.setOnClickListener { expandNearbySheet() }

        binding.interactiveMapView.onReportSelected = { report ->
            showSelectedIssueDetail(report)
        }
        binding.interactiveMapView.onMapClicked = {
            hideSelectedIssueDetail()
        }
    }

    private fun expandNearbySheet() {
        val behavior = BottomSheetBehavior.from(binding.bottomSheetNearbyIssues)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun recenterMapLocation() {
        com.reportit.app.utils.LocationHelper.fetchCurrentLocation(requireContext()) { lat, lon, _ ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.interactiveMapView.setUserLocation(lat, lon, centerOnIt = true)
                val ward = com.reportit.app.gis.GbaSpatialManager.resolvePoint(requireContext(), lat, lon)
                if (ward != null) {
                    binding.tvMapLocationTitle.text = "Near me • ${ward.wardName}"
                    binding.tvMapWardSubtitle.text = "Ward ${ward.wardNumber} • ${ward.acName}"
                    Toast.makeText(requireContext(), "📍 Live GPS: Ward ${ward.wardNumber} • ${ward.wardName}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "📍 Live GPS: Lat $lat, Lon $lon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showMapSearchDialog() {
        val input = EditText(requireContext())
        input.hint = "Potholes near me, Silk Board flooding, etc."
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Search Civic Map")
            .setView(input)
            .setPositiveButton("Search") { _, _ ->
                val q = input.text.toString().trim()
                if (q.isNotEmpty()) {
                    val found = allRealReports.firstOrNull { it.title.contains(q, true) || it.locationAddress.contains(q, true) }
                    if (found != null && found.latitude != 0.0 && found.longitude != 0.0) {
                        binding.interactiveMapView.centerOnCoordinates(found.latitude, found.longitude, animated = true)
                        showSelectedIssueDetail(found)
                    } else {
                        Toast.makeText(requireContext(), "Showing results for: '$q'", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMapFilterDialog() {
        val filterCategories = arrayOf(
            "All Issues (Show Everything)",
            "Roads & Drainage (BBMP)",
            "Solid Waste / Garbage (BBMP SWM)",
            "Water & Sewage (BWSSB)",
            "Electrical & Streetlights (BESCOM)",
            "High & Critical Severity Only",
            "Radius: Within 1 km",
            "Radius: Within 5 km"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter Civic Map")
            .setItems(filterCategories) { dialog, which ->
                Toast.makeText(requireContext(), "Applied: ${filterCategories[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Clear All", null)
            .show()
    }

    private fun showLayerSwitcherDialog() {
        val layers = arrayOf(
            "Civic Issues Layer (Active Pins & Landmarks)",
            "Problem Density Heatmap (Hotspots)",
            "My Reports Only (Your complaints)",
            "Resolved Issues Layer (Audit history)"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Map Layers")
            .setItems(layers) { dialog, which ->
                activeLayer = layers[which]
                when (which) {
                    1 -> binding.interactiveMapView.toggleHeatmap(true)
                    else -> binding.interactiveMapView.toggleHeatmap(false)
                }
                Toast.makeText(requireContext(), "Layer switched: $activeLayer", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    private fun showWardSelectorDialog() {
        val wards = arrayOf(
            "Ward 150 - Bellandur, Mahadevapura" to Pair(12.9260, 77.6762),
            "Ward 174 - HSR Layout, Bommanahalli" to Pair(12.9116, 77.6389),
            "Ward 84 - Indiranagar, East Zone" to Pair(12.9784, 77.6408),
            "Ward 160 - Koramangala, South Zone" to Pair(12.9352, 77.6245),
            "Ward 112 - Domlur, East Zone" to Pair(12.9609, 77.6387),
            "Ward 120 - Jayanagar, South Zone" to Pair(12.9299, 77.5824),
            "Ward 198 - Whitefield, Mahadevapura" to Pair(12.9698, 77.7499),
            "Ward 34 - Bande Mutt, RR Nagar" to Pair(12.8868, 77.4502)
        )
        val wardNames = wards.map { it.first }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Bengaluru Ward")
            .setItems(wardNames) { dialog, which ->
                val selected = wards[which]
                binding.tvMapLocationTitle.text = "Near me • ${selected.first.substringAfter('-').substringBefore(',')}"
                binding.tvMapWardSubtitle.text = selected.first.substringBefore('-').trim()
                binding.interactiveMapView.centerOnCoordinates(selected.second.first, selected.second.second, animated = true)
                Toast.makeText(requireContext(), "Map centered to ${selected.first}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        binding.interactiveMapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.interactiveMapView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener?.remove()
        firestoreListener = null
        _binding = null
    }
}
