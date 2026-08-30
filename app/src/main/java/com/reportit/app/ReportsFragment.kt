package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.reportit.app.databinding.FragmentReportsBinding

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

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

        // Filter chips feedback
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                when (chipId) {
                    R.id.chipAll -> showFilterToast("Showing All Reports (3)")
                    R.id.chipPending -> showFilterToast("Filtered: Pending Triage (1)")
                    R.id.chipInProgress -> showFilterToast("Filtered: In Progress (1)")
                    R.id.chipResolved -> showFilterToast("Filtered: Resolved & Awaiting Verification (1)")
                }
            }
        }

        // Navigate to Track for items
        binding.cardReportItem1.setOnClickListener { openTrack("RI-2026-004821") }
        binding.btnTrackReport1.setOnClickListener { openTrack("RI-2026-004821") }

        binding.cardReportItem2.setOnClickListener { openTrack("RI-2026-004790") }
        binding.btnTrackReport2.setOnClickListener { openTrack("RI-2026-004790") }

        binding.cardReportItem3.setOnClickListener { openTrack("RI-2026-004812") }
        binding.btnTrackReport3.setOnClickListener { openTrack("RI-2026-004812") }
    }

    private fun showFilterToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun openTrack(reportId: String) {
        (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_track)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
