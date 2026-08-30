package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reportit.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val languages = arrayOf(
        "English (Default)",
        "ಕನ್ನಡ (Kannada)",
        "हिन्दी (Hindi)",
        "தமிழ் (Tamil)",
        "తెలుగు (Telugu)",
        "മലയാളം (Malayalam)",
        "मराठी (Marathi)",
        "বাংলা (Bengali)"
    )
    private var selectedLanguageIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Language Picker Dialog (IndicTrans2 integration)
        val showLanguageDialog = {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🌐 Select Regional Language (IndicTrans2)")
                .setSingleChoiceItems(languages, selectedLanguageIndex) { dialog, which ->
                    selectedLanguageIndex = which
                    binding.tvCurrentLanguage.text = "${languages[which]} (Selected)"
                    (activity as? MainActivity)?.updateLanguageButton(languages[which].split(" ")[0])
                    Toast.makeText(requireContext(), "Language changed to ${languages[which]}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.cardLanguageSelect.setOnClickListener { showLanguageDialog() }
        binding.btnChangeLanguage.setOnClickListener { showLanguageDialog() }

        // Switch to Admin / Authority Command Center
        binding.cardSwitchAdmin.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(AdminDashboardFragment())
        }

        // About Report-It Dialog
        binding.btnAboutApp.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("ℹ️ About Report-It")
                .setMessage("Report-It is an AI-powered civic issue reporting and predictive geospatial intelligence platform connecting citizens directly with municipal authorities.\n\nTagline: Report. Resolve. Transform.\nVersion: 1.0 (Phase-1 Architecture)")
                .setPositiveButton("Close", null)
                .show()
        }

        // Privacy Policy Dialog
        binding.btnPrivacyPolicy.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🔒 Privacy & Moderation")
                .setMessage("Report-It protects citizen anonymity by stripping sensitive camera EXIF data while preserving GPS coordinates for civic work. Inappropriate or abusive uploads are quarantined by automated moderation.")
                .setPositiveButton("Understood", null)
                .show()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
