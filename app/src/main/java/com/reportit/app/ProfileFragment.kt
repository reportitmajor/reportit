package com.reportit.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reportit.app.data.session.SessionManager
import com.reportit.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var profileListener: com.google.firebase.firestore.ListenerRegistration? = null

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

        sessionManager = SessionManager(requireContext())

        // Display current authenticated user info
        binding.tvProfileName.text = sessionManager.getUserName()
        val roleSuffix = if (sessionManager.getUserRole() == SessionManager.ROLE_OFFICER) " • [Field Officer]" else ""
        binding.tvProfileContact.text = "${sessionManager.getUserEmail()}$roleSuffix"

        // Connect real-time Firestore listener for live Citizen Karma & Gamification
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: sessionManager.getFirebaseUid()
        if (currentUid.isNotEmpty()) {
            profileListener = com.reportit.app.data.repository.FirestoreRepository.listenToUserProfile(currentUid) { karma, totalReports, role ->
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    val score = karma.toInt()
                    binding.tvCivicKarmaScore.text = "🏆 Citizen Civic Score: $score / 100"

                    val rankBadge = when {
                        score >= 300 -> "Civic Champion"
                        score >= 100 -> "Civic Guardian"
                        else -> "Civic Starter"
                    }
                    binding.tvCivicRankBadge.text = rankBadge
                    binding.tvCivicStatsDescription.text = "Earned via $totalReports submitted reports and active civic participation."
                }
            }
        }

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
            // Sign out of Firebase Auth
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                ).build()
                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso).signOut()
            } catch (e: Exception) {
                // Ignore if not signed in via Google or Firebase
            }

            sessionManager.logout()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileListener?.remove()
        profileListener = null
        _binding = null
    }
}
