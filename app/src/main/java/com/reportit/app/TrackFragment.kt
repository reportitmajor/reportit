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

class TrackFragment : Fragment() {

    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!

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

        // 2-Way Citizen Resolution Verification: YES
        binding.btnVerifyYes.setOnClickListener {
            binding.layoutVerificationButtons.visibility = View.GONE
            binding.tvVerificationFeedback.visibility = View.VISIBLE
            binding.tvVerificationFeedback.text = "🎉 Resolution Confirmed! Ticket #RI-2026-004821 is officially CLOSED.\n+10 Civic Points added to your Citizen Score!"
            binding.tvVerificationFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_resolved))
            
            binding.tvTrackingStatusBadge.text = "CLOSED & VERIFIED"
            binding.tvTrackingStatusBadge.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.status_resolved_bg)
            binding.tvTrackingStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_resolved))

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

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🚨 Complaint Reopened & Escalated")
                .setMessage("We have notified the zonal executive engineer that the issue still exists. An emergency supervisor audit has been scheduled.")
                .setPositiveButton("Understood", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
