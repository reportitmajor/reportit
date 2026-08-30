package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reportit.app.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Return to Citizen mode
        binding.btnBackToCitizen.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNav(R.id.nav_home)
        }

        // Assign Crew action
        binding.btnAssignOfficer.setOnClickListener {
            val teams = arrayOf(
                "Eng. Suresh K. (Road Maintenance Team 4)",
                "Eng. Anand V. (Zonal Asphalt Crew)",
                "Eng. Meena R. (Emergency Response Unit)"
            )
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("👷 Assign Field Officer / Crew")
                .setItems(teams) { _, which ->
                    Toast.makeText(requireContext(), "Work Order assigned to: ${teams[which]}", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Re-Route Department
        binding.btnReRouteDept.setOnClickListener {
            val departments = arrayOf(
                "BBMP Major Road Infrastructure",
                "Solid Waste Management",
                "Stormwater & Drainage Dept",
                "BWSSB Water Supply & Sewerage",
                "BESCOM Electrical Maintenance"
            )
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🏛️ Re-Route Civic Department")
                .setItems(departments) { _, which ->
                    Toast.makeText(requireContext(), "Re-routed to: ${departments[which]}", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
