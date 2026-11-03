package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reportit.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val bengaluruWards = arrayOf(
        "Ward 150 - Bellandur, Mahadevapura",
        "Ward 174 - HSR Layout, Bommanahalli",
        "Ward 84 - Indiranagar, East Zone",
        "Ward 160 - Koramangala, South Zone",
        "Ward 112 - Domlur, East Zone",
        "Ward 120 - Jayanagar, South Zone",
        "Ward 198 - Whitefield, Mahadevapura",
        "Ward 77 - Rajajinagar, West Zone",
        "Ward 11 - Malleshwaram, West Zone",
        "Ward 177 - JP Nagar, South Zone"
    )
    private var selectedWardIndex = 0

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

        // Top Header Controls
        binding.btnCitySelector.setOnClickListener {
            Toast.makeText(requireContext(), "📍 Bengaluru Urban District (BBMP Area)", Toast.LENGTH_SHORT).show()
        }

        binding.btnLanguageQuick.setOnClickListener {
            (activity as? MainActivity)?.showLanguageDialog()
        }

        binding.btnNotifications.setOnClickListener {
            (activity as? MainActivity)?.showNotificationsDialog()
        }

        // Ward Selector Dialog
        binding.btnWardSelector.setOnClickListener {
            showWardSelectorDialog()
        }

        // Hero CTA - Report a Civic Issue
        binding.btnQuickReport.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(ReportIssueFragment())
        }
        binding.cardReportCTA.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(ReportIssueFragment())
        }

        // Live Updates Banner
        binding.cardLiveAlert.setOnClickListener {
            showLiveMonsoonAdvisoryDialog()
        }
        binding.btnLiveUpdatesAll.setOnClickListener {
            showLiveMonsoonAdvisoryDialog()
        }

        // My Reports Section
        binding.tvViewAllReports.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_reports)
        }
        binding.cardRecentPothole.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_track)
        }
        binding.cardRecentGarbage.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_track)
        }
        binding.cardRecentStreetlight.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_track)
        }

        // Civic Issues Around You
        binding.tvViewMapIssues.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_map)
        }
        binding.cardIssueAround1.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_map)
        }
        binding.cardIssueAround2.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_map)
        }

        // Explore More Grid
        binding.cardExploreHeatmap.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_map)
        }

        binding.cardExploreEmergency.setOnClickListener {
            showEmergencyHelplinesDialog()
        }

        binding.cardExploreScore.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_profile)
        }

        binding.cardExploreDepts.setOnClickListener {
            showDepartmentsDirectoryDialog()
        }
    }

    private fun showWardSelectorDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📍 Select Bengaluru Ward")
            .setSingleChoiceItems(bengaluruWards, selectedWardIndex) { dialog, which ->
                selectedWardIndex = which
                val selectedWard = bengaluruWards[which]
                binding.tvCurrentWard.text = selectedWard
                Toast.makeText(requireContext(), "Active Ward: $selectedWard", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLiveMonsoonAdvisoryDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("⚠️ Bengaluru Monsoon & Flood Bulletin")
            .setMessage(
                "• Heavy waterlogging reported near Silk Board Junction & Bellandur Ecospace underpass.\n" +
                "• 12 active citizen reports verified by BBMP Control Room.\n" +
                "• High-capacity dewatering pumps actively operating.\n" +
                "• BBMP Sahaya Helpline: 1533 / 080-22221188."
            )
            .setPositiveButton("View on Map") { _, _ ->
                (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_map)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEmergencyHelplinesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🚨 Bengaluru Emergency Civic Helplines")
            .setMessage(
                "• BBMP Central Sahaya: 1533 / 080-22221188\n" +
                "• BESCOM (Live Wire / Power): 1912\n" +
                "• BWSSB (Water / Sewage): 1916\n" +
                "• Bengaluru Traffic Police (BTP): 103 / 080-22943030\n" +
                "• Emergency Police / Ambulance: 112"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDepartmentsDirectoryDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🏛️ Bengaluru Municipal Departments")
            .setMessage(
                "• BBMP Road Infra: Potholes, road widening, asphalt paving.\n" +
                "• BBMP Solid Waste (SWM): Garbage clearance, blackspot monitoring.\n" +
                "• BWSSB: Drinking water pipeline supply, sewer line maintenance.\n" +
                "• BESCOM: Street power transformers, hanging wires.\n" +
                "• BBMP Forest Cell: Storm debris & tree fall clearance."
            )
            .setPositiveButton("Got It", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
