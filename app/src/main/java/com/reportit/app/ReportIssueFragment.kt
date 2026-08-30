package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reportit.app.databinding.FragmentReportIssueBinding

class ReportIssueFragment : Fragment() {

    private var _binding: FragmentReportIssueBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory: String = "Road Pothole"

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
            selectedCategory = it
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

        // Set initial category display
        binding.tvDetectedCategory.text = "🛣️ $selectedCategory"

        // Simulated Photo capture
        binding.btnCapturePhoto.setOnClickListener {
            binding.tvCaptureTitle.text = "✅ Photo Attached: pothole_evidence_01.jpg"
            binding.ivCapturePreview.setImageResource(R.drawable.ic_check_circle)
            Toast.makeText(requireContext(), "📸 Geo-tagged photo captured with GPS coordinates!", Toast.LENGTH_SHORT).show()
        }

        binding.btnUploadGallery.setOnClickListener {
            binding.tvCaptureTitle.text = "✅ Image Loaded from Device"
            binding.ivCapturePreview.setImageResource(R.drawable.ic_check_circle)
            Toast.makeText(requireContext(), "Image evidence uploaded", Toast.LENGTH_SHORT).show()
        }

        // Adjust GPS Location
        binding.btnAdjustLocation.setOnClickListener {
            Toast.makeText(requireContext(), "📍 Pinpoint map location adjusted", Toast.LENGTH_SHORT).show()
        }

        // Submit Geo-Report
        binding.btnSubmitReport.setOnClickListener {
            val reportId = "RI-2026-004821"
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🎉 Report Submitted Successfully!")
                .setMessage("Your civic complaint has been analyzed by AI and auto-routed to the BBMP Road Infrastructure Department.\n\nTracking ID: $reportId\nPriority: High\nEstimated SLA: 48 Hours")
                .setPositiveButton("Track Live Status") { _, _ ->
                    (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_track)
                }
                .setNegativeButton("Done") { dialog, _ ->
                    dialog.dismiss()
                    (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_home)
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
