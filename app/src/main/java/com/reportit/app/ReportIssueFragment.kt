package com.reportit.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.reportit.app.ai.CivicIssueClassifier
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportSeverity
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.data.repository.CloudinaryRepository
import com.reportit.app.data.repository.FirestoreRepository
import com.reportit.app.data.repository.ImgBBRepository
import com.reportit.app.data.session.SessionManager
import com.reportit.app.databinding.FragmentReportIssueBinding
import com.reportit.app.gis.GbaSpatialManager
import com.reportit.app.utils.ImageUtils
import com.reportit.app.utils.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import android.util.Log

class ReportIssueFragment : Fragment() {

    private var _binding: FragmentReportIssueBinding? = null
    private val binding get() = _binding!!

    private lateinit var classifier: CivicIssueClassifier
    private var capturedBitmap: Bitmap? = null

    private var currentStep = 1

    private var detectedCategory = "Road Pothole (BBMP Roads)"
    private var detectedSeverity = "High Severity"
    private var detectedConfidence = "Confidence: 94%"
    private var aiConfidenceInt: Int = 94
    private var submittedReportId: String = "#NR123456"
    private var selectedTimeframe = "Today"
    private var selectedWard = "Ward 150 - Bellandur, Mahadevapura"
    private var selectedLocation = "Bellandur, Bengaluru"

    // GBA Spatial Resolution Fields
    private var detectedGbaWardNumber: Int = 150
    private var detectedGbaWardName: String = "Bellandur"
    private var detectedGbaZone: String = "East City Corporation"
    private var detectedMlaName: String = "Manjula A. / Arvind Limbavali"
    private var detectedAcName: String = "Mahadevapura"
    private var detectedAeeName: String = "Er. Suresh K. (AEE)"
    private var detectedAeeContact: String = "+91 80 2860 5510"
    private var currentLatitude: Double = 12.9260
    private var currentLongitude: Double = 77.6762
    private var uploadedImageUrl: String? = null
    private var uploadedThumbUrl: String? = null

    private val categoriesList = arrayOf(
        "Road Pothole (BBMP Roads)",
        "Garbage Dump / Blackspot (BBMP SWM)",
        "Water Pipe Leakage (BWSSB)",
        "Sewage / Manhole Overflow (BWSSB)",
        "Broken Streetlight (BBMP Electrical)",
        "Fallen Tree / Hazard (BBMP Forest / BTP)"
    )

    private val wardsList = arrayOf(
        "Ward 150 - Bellandur, Mahadevapura",
        "Ward 174 - HSR Layout, Bommanahalli",
        "Ward 84 - Indiranagar, East Zone",
        "Ward 160 - Koramangala, South Zone",
        "Ward 112 - Domlur, East Zone",
        "Ward 120 - Jayanagar, South Zone",
        "Ward 198 - Whitefield, Mahadevapura"
    )

    // Real Camera Hardware Launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onRealPhotoCaptured(bitmap)
        } else {
            Toast.makeText(requireContext(), "Camera capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Permission Launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission is required to capture civic issue photos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Location Permission Launcher
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            refreshLocationFromGps()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission denied. You can select your Bengaluru ward manually.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Photo Gallery Picker Launcher
    private val pickGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(uri)
            if (bitmap != null) {
                onRealPhotoCaptured(bitmap)
            } else {
                Toast.makeText(requireContext(), "Unable to process selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_CATEGORY = "arg_category"

        fun newInstance(category: String): ReportIssueFragment {
            val fragment = ReportIssueFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_CATEGORY)?.let {
            detectedCategory = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportIssueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        classifier = CivicIssueClassifier(requireContext())

        setupHeaderControls()
        setupStep1Actions()
        setupStep2CameraActions()
        setupStep3AIActions()
        setupStep4LocationActions()
        setupStep5DescribeActions()
        setupStep6RoutingActions()
        setupStep7ReviewActions()
        setupStep8SuccessActions()
        setupStep9TrackingActions()

        renderStep(1)
    }

    private fun setupHeaderControls() {
        binding.btnBackReport.setOnClickListener {
            handleBackNavigation()
        }

        binding.btnCloseReport.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun handleBackNavigation() {
        when (currentStep) {
            1 -> requireActivity().supportFragmentManager.popBackStack()
            2 -> renderStep(1)
            3 -> renderStep(1)
            4 -> renderStep(3)
            5 -> renderStep(4)
            6 -> renderStep(5)
            7 -> renderStep(6)
            8 -> requireActivity().supportFragmentManager.popBackStack()
            9 -> requireActivity().supportFragmentManager.popBackStack()
            else -> renderStep(1)
        }
    }

    // Step 1: Add Photo Actions
    private fun setupStep1Actions() {
        binding.btnTakePhoto.setOnClickListener {
            launchCamera()
        }

        binding.btnUploadGallery.setOnClickListener {
            pickGalleryLauncher.launch("image/*")
        }

        binding.btnSkipPhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Proceeding without photo evidence", Toast.LENGTH_SHORT).show()
            renderStep(4)
        }
    }

    // Step 2: Camera Actions
    private fun setupStep2CameraActions() {
        binding.btnShutterCapture.setOnClickListener {
            launchCamera()
        }

        binding.btnCameraGallery.setOnClickListener {
            pickGalleryLauncher.launch("image/*")
        }

        binding.btnCameraFlash.setOnClickListener {
            Toast.makeText(requireContext(), "Flash toggled", Toast.LENGTH_SHORT).show()
        }

        binding.btnCameraSwitch.setOnClickListener {
            launchCamera()
        }

        binding.btnCancelCamera.setOnClickListener {
            renderStep(1)
        }
    }

    private fun launchCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePictureLauncher.launch(null)
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun refreshLocationFromGps(onComplete: (() -> Unit)? = null) {
        if (!LocationHelper.hasLocationPermission(requireContext())) {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            onComplete?.invoke()
            return
        }

        LocationHelper.fetchCurrentLocation(requireContext()) { lat, lon, isAccurate ->
            activity?.runOnUiThread {
                if (_binding != null) {
                    currentLatitude = lat
                    currentLongitude = lon

                    val gbaWard = GbaSpatialManager.resolvePoint(requireContext(), lat, lon)
                    if (gbaWard != null) {
                        detectedGbaWardNumber = gbaWard.wardNumber
                        detectedGbaWardName = gbaWard.wardName
                        detectedGbaZone = gbaWard.gbaZone
                        detectedMlaName = gbaWard.mlaName
                        detectedAcName = gbaWard.acName
                        detectedAeeName = gbaWard.aeeName
                        detectedAeeContact = gbaWard.aeeContact

                        selectedWard = "Ward ${gbaWard.wardNumber} — ${gbaWard.wardName} (${gbaWard.acName})"
                        selectedLocation = "${gbaWard.wardName}, ${gbaWard.gbaZone}"

                        binding.tvLocationName.text = "${gbaWard.wardName}, ${gbaWard.gbaZone}"
                        binding.tvLocationWard.text = "🏛️ Ward ${gbaWard.wardNumber}: ${gbaWard.wardName}\n🗳️ MLA: ${gbaWard.mlaName} (${gbaWard.acName})\n🏢 Zone: ${gbaWard.gbaZone}"

                        if (isAccurate) {
                            Toast.makeText(
                                requireContext(),
                                "📍 Live GPS: Ward ${gbaWard.wardNumber} • ${gbaWard.wardName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                onComplete?.invoke()
            }
        }
    }

    private fun onRealPhotoCaptured(bitmap: Bitmap) {
        capturedBitmap = bitmap
        binding.ivAICapturedPhoto.setImageBitmap(bitmap)
        binding.ivReviewEvidencePhoto.setImageBitmap(bitmap)

        refreshLocationFromGps()

        renderStep(3)
        runAIClassification(bitmap)
    }

    private fun runAIClassification(bitmap: Bitmap) {
        binding.cardAIScanning.visibility = View.VISIBLE
        binding.cardAIResult.visibility = View.GONE
        binding.btnAIContinue.isEnabled = false
        binding.tvAIScanStatus.text = "Analyzing image with Gemini Flash Vision..."

        Executors.newSingleThreadExecutor().execute {
            var isGeminiPrimary = true
            val result = try {
                com.reportit.app.ai.GeminiVisionClassifier.classify(bitmap)
            } catch (e: Exception) {
                Log.w("ReportIssueFragment", "Gemini Vision primary failed or offline: ${e.message}. Falling back to on-device YOLOv8.")
                isGeminiPrimary = false
                activity?.runOnUiThread {
                    if (_binding != null) {
                        binding.tvAIScanStatus.text = "Offline: Running on-device YOLOv8 fallback..."
                    }
                }
                classifier.classify(bitmap)
            }

            activity?.runOnUiThread {
                if (_binding != null) {
                    binding.cardAIScanning.visibility = View.GONE
                    binding.cardAIResult.visibility = View.VISIBLE
                    binding.btnAIContinue.isEnabled = true

                    detectedCategory = result.categoryName
                    aiConfidenceInt = if (result.isIssueDetected) result.confidencePercentage else 0
                    val engineBadge = if (isGeminiPrimary) "✨ Gemini Flash" else "📱 YOLOv8 Fallback"
                    detectedConfidence = if (result.isIssueDetected) "$engineBadge (${result.confidencePercentage}%)" else "Confidence < 25%"
                    detectedSeverity = result.severity

                    binding.tvAIDetectedName.text = result.categoryName
                    binding.tvAIConfidence.text = detectedConfidence
                    binding.tvAIDescription.text = result.summary
                    binding.tvDescribeCategory.text = result.categoryName

                    if (result.isIssueDetected) {
                        binding.tvAISeverity.visibility = View.VISIBLE
                        binding.tvAISeverity.text = result.severity
                        binding.tvAIConfidence.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D1FAE5"))
                        binding.tvAIConfidence.setTextColor(Color.parseColor("#059669"))
                        Toast.makeText(
                            requireContext(),
                            if (isGeminiPrimary) "✨ Gemini Vision: ${result.categoryName}" else "📱 YOLO Fallback: ${result.categoryName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.tvAISeverity.visibility = View.GONE
                        binding.tvAIConfidence.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1F5F9"))
                        binding.tvAIConfidence.setTextColor(Color.parseColor("#64748B"))
                        Toast.makeText(
                            requireContext(),
                            "ℹ️ No civic hazard detected in this photo. You can manually pick a category or proceed.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // Step 3: AI Analysis Actions
    private fun setupStep3AIActions() {
        binding.btnChangeAICategory.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Category")
                .setItems(categoriesList) { dialog, which ->
                    detectedCategory = categoriesList[which]
                    binding.tvAIDetectedName.text = detectedCategory
                    binding.tvDescribeCategory.text = detectedCategory
                    Toast.makeText(requireContext(), "Category updated: $detectedCategory", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnAIContinue.setOnClickListener {
            renderStep(4)
        }
    }

    // Step 4: Confirm Location Actions
    private fun setupStep4LocationActions() {
        binding.btnAdjustLocation.setOnClickListener {
            val options = arrayOf("📍 Re-scan Live GPS Location", "📝 Pick Bengaluru Ward from List")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Location Options")
                .setItems(options) { dialog, which ->
                    dialog.dismiss()
                    if (which == 0) {
                        Toast.makeText(requireContext(), "Fetching high-accuracy GPS...", Toast.LENGTH_SHORT).show()
                        refreshLocationFromGps()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Select Bengaluru Ward")
                            .setItems(wardsList) { wardDialog, wardIndex ->
                                selectedWard = wardsList[wardIndex]
                                binding.tvLocationWard.text = selectedWard
                                Toast.makeText(requireContext(), "Location updated: $selectedWard", Toast.LENGTH_SHORT).show()
                                wardDialog.dismiss()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnLocationContinue.setOnClickListener {
            renderStep(5)
        }
    }

    // Step 5: Describe Issue Actions
    private fun setupStep5DescribeActions() {
        binding.btnVoiceInput.setOnClickListener {
            binding.etIssueDescription.setText("Large road pothole with deep asphalt depression causing traffic slowdown near signal.")
            Toast.makeText(requireContext(), "Voice input transcribed into text!", Toast.LENGTH_SHORT).show()
        }

        binding.optNoticeToday.setOnClickListener {
            setTimeframeSelection("Today")
        }

        binding.optNoticeYesterday.setOnClickListener {
            setTimeframeSelection("Yesterday")
        }

        binding.optNoticeOlder.setOnClickListener {
            setTimeframeSelection("More than 2 days")
        }

        binding.btnDescribeContinue.setOnClickListener {
            renderStep(6)
        }
    }

    private fun setTimeframeSelection(timeframe: String) {
        selectedTimeframe = timeframe
        binding.optNoticeToday.strokeColor = if (timeframe == "Today") Color.parseColor("#0066FF") else Color.parseColor("#E2E8F0")
        binding.optNoticeYesterday.strokeColor = if (timeframe == "Yesterday") Color.parseColor("#0066FF") else Color.parseColor("#E2E8F0")
        binding.optNoticeOlder.strokeColor = if (timeframe == "More than 2 days") Color.parseColor("#0066FF") else Color.parseColor("#E2E8F0")
    }

    // Step 6: Smart Routing Actions
    private fun setupStep6RoutingActions() {
        binding.btnRoutingContinue.setOnClickListener {
            renderStep(7)
        }
    }

    // Step 7: Review & Submit Actions
    private fun setupStep7ReviewActions() {
        binding.btnEditSummary.setOnClickListener {
            renderStep(5)
        }

        binding.btnFinalSubmitReport.setOnClickListener {
            submitReportToFirestore()
        }
    }

    private fun submitReportToFirestore() {
        binding.btnFinalSubmitReport.isEnabled = false
        binding.btnFinalSubmitReport.text = "Uploading Image to Municipal Cloud..."

        val photo = capturedBitmap
        if (photo != null) {
            if (CloudinaryRepository.isConfigured()) {
                CloudinaryRepository.uploadBitmap(
                    bitmap = photo,
                    onSuccess = { result ->
                        uploadedImageUrl = result.imageUrl
                        uploadedThumbUrl = result.thumbUrl
                        proceedFirestoreSubmission(photo)
                    },
                    onError = { error ->
                        Log.w("ReportIssueFragment", "Cloudinary upload error: $error, attempting ImgBB")
                        attemptImgBBUpload(photo)
                    }
                )
            } else {
                attemptImgBBUpload(photo)
            }
        } else {
            proceedFirestoreSubmission(null)
        }
    }

    private fun attemptImgBBUpload(photo: Bitmap) {
        ImgBBRepository.uploadBitmap(
            bitmap = photo,
            onSuccess = { result ->
                uploadedImageUrl = result.imageUrl
                uploadedThumbUrl = result.thumbUrl
                proceedFirestoreSubmission(photo)
            },
            onError = { error ->
                Log.w("ReportIssueFragment", "ImgBB upload error: $error, falling back to compressed local buffer in Firestore")
                proceedFirestoreSubmission(photo)
            }
        )
    }

    private fun proceedFirestoreSubmission(photo: Bitmap?) {
        activity?.runOnUiThread {
            if (_binding != null) {
                binding.btnFinalSubmitReport.text = "Saving to Municipal Cloud..."
            }
        }

        Executors.newSingleThreadExecutor().execute {
            val session = SessionManager(requireContext())
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = firebaseUser?.uid ?: session.getFirebaseUid()
            val currentUserName = session.getUserName()
            val currentUserEmail = session.getUserEmail()

            val randomNum = (100000..999999).random()
            val reportId = "RI-BLR-2026-$randomNum"

            val description = binding.etIssueDescription.text?.toString()?.trim()
                ?.ifEmpty { "$detectedCategory reported by citizen." }
                ?: "$detectedCategory reported by citizen."

            val severity = when {
                detectedSeverity.contains("critical", ignoreCase = true) -> ReportSeverity.CRITICAL
                detectedSeverity.contains("high", ignoreCase = true) -> ReportSeverity.HIGH
                detectedSeverity.contains("low", ignoreCase = true) -> ReportSeverity.LOW
                else -> ReportSeverity.MEDIUM
            }

            val department = when {
                detectedCategory.contains("Pothole", ignoreCase = true) || detectedCategory.contains("Road", ignoreCase = true) -> "BBMP Major Road Infrastructure"
                detectedCategory.contains("Garbage", ignoreCase = true) -> "BBMP Solid Waste Management"
                detectedCategory.contains("Water", ignoreCase = true) || detectedCategory.contains("Sewage", ignoreCase = true) -> "BWSSB Water Supply & Sewerage"
                detectedCategory.contains("Streetlight", ignoreCase = true) -> "BBMP Electrical Division"
                else -> "Municipal Corporation"
            }

            val imageBase64String = if (uploadedImageUrl.isNullOrEmpty() && photo != null) {
                ImageUtils.bitmapToBase64(photo, quality = 65, maxDimension = 480)
            } else {
                null
            }

            val newReport = GrievanceReport(
                id = reportId,
                title = "$detectedCategory Incident",
                description = description,
                category = detectedCategory,
                department = department,
                wardName = selectedWard,
                locationAddress = "$selectedWard, $selectedLocation",
                latitude = currentLatitude,
                longitude = currentLongitude,
                severity = severity,
                status = ReportStatus.SUBMITTED,
                slaTotalHours = 48,
                slaRemainingHours = 48,
                submissionDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
                reportedBy = currentUserName,
                aiConfidence = aiConfidenceInt,
                originalImageResId = R.drawable.img_pothole_evidence,
                assignedOfficer = detectedAeeName,
                assignedCrew = null,
                upvotesCount = 1,
                isUserUpvoted = true,
                imageBase64 = imageBase64String,
                imageUrl = uploadedImageUrl,
                thumbUrl = uploadedThumbUrl,
                acName = detectedAcName,
                mlaName = detectedMlaName,
                gbaZone = detectedGbaZone,
                aeeName = detectedAeeName,
                aeeContact = detectedAeeContact,
                userId = currentUserId,
                userEmail = currentUserEmail
            )

            FirestoreRepository.submitReport(newReport) { success, id ->
                activity?.runOnUiThread {
                    binding.btnFinalSubmitReport.isEnabled = true
                    binding.btnFinalSubmitReport.text = "Submit Report"

                    if (success) {
                        submittedReportId = reportId
                        binding.tvSuccessReportId.text = "#$reportId"
                        Toast.makeText(requireContext(), "Grievance #$reportId uploaded successfully!", Toast.LENGTH_LONG).show()
                        renderStep(8)
                    } else {
                        Toast.makeText(requireContext(), "Submission failed: $id", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Step 8: Success Actions
    private fun setupStep8SuccessActions() {
        binding.btnCopyReportId.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Report ID", submittedReportId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Report ID $submittedReportId copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewMyReport.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_reports)
        }

        binding.btnReportAnother.setOnClickListener {
            capturedBitmap = null
            binding.etIssueDescription.setText("")
            renderStep(1)
        }
    }

    // Step 9: Detailed Tracking Actions
    private fun setupStep9TrackingActions() {
        binding.btnShareTracking.setOnClickListener {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Civic Grievance #$submittedReportId")
                putExtra(android.content.Intent.EXTRA_TEXT, "I reported a $detectedCategory issue at $selectedWard on Report-It! Tracking ID: $submittedReportId")
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Grievance"))
        }

        binding.btnDoneTracking.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun renderStep(step: Int) {
        currentStep = step

        // Hide all views first
        binding.step1AddPhoto.visibility = View.GONE
        binding.step2CameraView.visibility = View.GONE
        binding.step3AIDetection.visibility = View.GONE
        binding.step4ConfirmLocation.visibility = View.GONE
        binding.step5DescribeIssue.visibility = View.GONE
        binding.step6SmartRouting.visibility = View.GONE
        binding.step7ReviewSubmit.visibility = View.GONE
        binding.step8Success.visibility = View.GONE
        binding.step9DetailedTracking.visibility = View.GONE

        // Update Stepper Bar visibility
        if (step in 1..7) {
            binding.stepperBar.visibility = View.VISIBLE
            binding.topBarReport.visibility = View.VISIBLE
            updateStepperUI(step)
        } else {
            binding.stepperBar.visibility = View.GONE
            binding.topBarReport.visibility = if (step == 8) View.GONE else View.VISIBLE
        }

        // Show active view
        when (step) {
            1 -> {
                binding.tvReportTitle.text = "Report a Civic Issue"
                binding.step1AddPhoto.visibility = View.VISIBLE
            }
            2 -> {
                binding.stepperBar.visibility = View.GONE
                binding.topBarReport.visibility = View.GONE
                binding.step2CameraView.visibility = View.VISIBLE
            }
            3 -> {
                binding.tvReportTitle.text = "AI Analysis"
                binding.step3AIDetection.visibility = View.VISIBLE
            }
            4 -> {
                binding.tvReportTitle.text = "Confirm Location"
                binding.step4ConfirmLocation.visibility = View.VISIBLE
            }
            5 -> {
                binding.tvReportTitle.text = "Describe Issue"
                binding.step5DescribeIssue.visibility = View.VISIBLE
            }
            6 -> {
                binding.tvReportTitle.text = "Smart Routing"
                binding.step6SmartRouting.visibility = View.VISIBLE
            }
            7 -> {
                binding.tvReportTitle.text = "Review & Submit"
                binding.step7ReviewSubmit.visibility = View.VISIBLE
                capturedBitmap?.let { bitmap ->
                    binding.ivReviewEvidencePhoto.setImageBitmap(bitmap)
                }
            }
            8 -> {
                binding.step8Success.visibility = View.VISIBLE
            }
            9 -> {
                binding.tvReportTitle.text = "Report Details"
                binding.step9DetailedTracking.visibility = View.VISIBLE
            }
        }

        // Scroll to top
        binding.scrollViewContent.scrollTo(0, 0)
    }

    private fun updateStepperUI(activeStep: Int) {
        val circles = arrayOf(
            binding.stepCircle1,
            binding.stepCircle2,
            binding.stepCircle3,
            binding.stepCircle4,
            binding.stepCircle5,
            binding.stepCircle6
        )

        val lines = arrayOf(
            binding.stepLine1,
            binding.stepLine2,
            binding.stepLine3,
            binding.stepLine4,
            binding.stepLine5
        )

        val mapStepToCircle = when (activeStep) {
            1 -> 1
            2 -> 1
            3 -> 2
            4 -> 3
            5 -> 4
            6 -> 5
            7 -> 6
            else -> 1
        }

        for (i in circles.indices) {
            val circleIndex = i + 1
            when {
                circleIndex < mapStepToCircle -> {
                    // Completed step
                    circles[i].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#059669"))
                    circles[i].setTextColor(Color.WHITE)
                    circles[i].text = "✓"
                }
                circleIndex == mapStepToCircle -> {
                    // Active step
                    circles[i].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0066FF"))
                    circles[i].setTextColor(Color.WHITE)
                    circles[i].text = "$circleIndex"
                }
                else -> {
                    // Upcoming step
                    circles[i].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
                    circles[i].setTextColor(Color.parseColor("#64748B"))
                    circles[i].text = "$circleIndex"
                }
            }
        }

        for (i in lines.indices) {
            val lineIndex = i + 1
            if (lineIndex < mapStepToCircle) {
                lines[i].setBackgroundColor(Color.parseColor("#059669"))
            } else {
                lines[i].setBackgroundColor(Color.parseColor("#E2E8F0"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
