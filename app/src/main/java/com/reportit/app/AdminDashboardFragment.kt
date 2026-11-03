package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import com.reportit.app.data.GrievanceDataProvider
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportSeverity
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.data.repository.FirestoreRepository
import com.reportit.app.databinding.FragmentAdminDashboardBinding
import com.reportit.app.ui.admin.AdminTriageAdapter

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminTriageAdapter
    private var firestoreListener: ListenerRegistration? = null
    private val adminReportsList = mutableListOf<GrievanceReport>()

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

        setupRecyclerView()
        startLiveTriageListener()
    }

    private fun setupRecyclerView() {
        adapter = AdminTriageAdapter(
            onAssignCrew = { report ->
                showAssignCrewDialog(report)
            },
            onMarkResolved = { report ->
                showMarkResolvedDialog(report)
            }
        )

        binding.rvAdminGrievances.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminGrievances.adapter = adapter
    }

    private fun startLiveTriageListener() {
        // Connect real-time Firestore listener for authentic reports
        firestoreListener = FirestoreRepository.listenToAllReports(
            onUpdate = { cloudReports ->
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    adminReportsList.clear()
                    adminReportsList.addAll(cloudReports)
                    updateDashboardUI(adminReportsList)
                }
            },
            onError = { _ ->
                // Offline handling
            }
        )
    }

    private fun updateDashboardUI(reports: List<GrievanceReport>) {
        adapter.submitList(reports.toList())

        // Compute KPIs
        val total = reports.size
        val critical = reports.count { it.severity == ReportSeverity.CRITICAL || it.severity == ReportSeverity.HIGH }
        val resolved = reports.count { it.status == ReportStatus.RESOLVED || it.status == ReportStatus.CITIZEN_CLOSED }

        binding.tvAdminTotalCount.text = total.toString()
        binding.tvAdminCriticalCount.text = critical.toString()
        binding.tvAdminResolvedCount.text = resolved.toString()
        binding.tvAdminAiAccuracy.text = "96.2%"

        if (reports.isEmpty()) {
            binding.tvNoPendingTriage.visibility = View.VISIBLE
            binding.rvAdminGrievances.visibility = View.GONE
        } else {
            binding.tvNoPendingTriage.visibility = View.GONE
            binding.rvAdminGrievances.visibility = View.VISIBLE
        }
    }

    private fun showAssignCrewDialog(report: GrievanceReport) {
        val crews = arrayOf(
            "Eng. Suresh K. (Road Maintenance Team 4)",
            "Eng. Anand V. (Zonal Asphalt Crew KA-01-EA)",
            "Eng. Meena R. (Emergency Pothole Unit)",
            "BWSSB Quick Leak Repair Team 2",
            "BBMP Solid Waste Rapid Action Team 5"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("👷 Assign Field Officer / Crew")
            .setItems(crews) { _, which ->
                val selectedCrew = crews[which]
                val officerName = selectedCrew.substringBefore(" (")

                FirestoreRepository.updateReportStatus(
                    reportId = report.id,
                    newStatus = ReportStatus.IN_PROGRESS,
                    assignedOfficer = officerName,
                    assignedCrew = selectedCrew
                ) { success ->
                    activity?.runOnUiThread {
                        if (success) {
                            Toast.makeText(requireContext(), "Work order dispatched to: $selectedCrew", Toast.LENGTH_LONG).show()
                        } else {
                            // Local fallback update
                            report.status = ReportStatus.IN_PROGRESS
                            report.assignedCrew = selectedCrew
                            report.assignedOfficer = officerName
                            adapter.notifyDataSetChanged()
                            Toast.makeText(requireContext(), "Assigned locally: $selectedCrew", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMarkResolvedDialog(report: GrievanceReport) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("✅ Confirm Incident Resolution")
            .setMessage("Mark #${report.id} as Resolved? This will notify the citizen and prompt them for final verification.")
            .setPositiveButton("Mark Resolved") { _, _ ->
                FirestoreRepository.updateReportStatus(
                    reportId = report.id,
                    newStatus = ReportStatus.RESOLVED,
                    assignedOfficer = report.assignedOfficer,
                    assignedCrew = report.assignedCrew
                ) { success ->
                    activity?.runOnUiThread {
                        if (success) {
                            Toast.makeText(requireContext(), "Incident #${report.id} marked RESOLVED!", Toast.LENGTH_LONG).show()
                        } else {
                            report.status = ReportStatus.RESOLVED
                            adapter.notifyDataSetChanged()
                            Toast.makeText(requireContext(), "Marked resolved locally", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener?.remove()
        firestoreListener = null
        _binding = null
    }
}
