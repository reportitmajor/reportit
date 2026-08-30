package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.reportit.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Quick Report CTA
        binding.btnQuickReport.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(ReportIssueFragment())
        }

        binding.cardReportCTA.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(ReportIssueFragment())
        }

        // Category clicks pre-route to Report flow
        binding.catPothole.setOnClickListener { openReportCategory("Road Pothole") }
        binding.catGarbage.setOnClickListener { openReportCategory("Garbage Dump") }
        binding.catDrainage.setOnClickListener { openReportCategory("Drainage Overflow") }
        binding.catStreetlight.setOnClickListener { openReportCategory("Broken Streetlight") }
        binding.catWater.setOnClickListener { openReportCategory("Water Pipe Leakage") }
        binding.catTraffic.setOnClickListener { openReportCategory("Traffic Hazard") }

        // View All Reports navigation
        binding.tvViewAllReports.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_reports)
        }

        // Recent Card 1 -> Open Track
        binding.cardRecent1.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_track)
        }

        // Recent Card 2 -> Open Track
        binding.cardRecent2.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_track)
        }
    }

    private fun openReportCategory(category: String) {
        Toast.makeText(requireContext(), "Selected category: $category", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.navigateToFragment(ReportIssueFragment.newInstance(category))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
