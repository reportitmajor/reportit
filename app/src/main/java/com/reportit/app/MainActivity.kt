package com.reportit.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reportit.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
    private var currentLanguageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Set initial fragment to Home
        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), false)
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        // Bottom Navigation Item Clicks
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment(), false)
                    true
                }
                R.id.nav_reports -> {
                    loadFragment(ReportsFragment(), false)
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment(), false)
                    true
                }
                R.id.nav_track -> {
                    loadFragment(TrackFragment(), false)
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment(), false)
                    true
                }
                else -> false
            }
        }

        // FAB Report Issue button
        binding.fabReport.setOnClickListener {
            navigateToFragment(ReportIssueFragment())
        }

        // Quick Language Switcher Button
        binding.btnLanguageQuick.setOnClickListener {
            showLanguageDialog()
        }

        // Top Notification Bell
        binding.btnNotifications.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("🔔 Civic Notifications")
                .setMessage("• Report #RI-2026-004821 assigned to BBMP Road Dept.\n• Report #RI-2026-004790 resolved! Please verify.\n• +10 Civic Score awarded for community participation.")
                .setPositiveButton("Clear", null)
                .show()
        }
    }

    private fun showLanguageDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("🌐 Select Language (IndicTrans2)")
            .setSingleChoiceItems(languages, currentLanguageIndex) { dialog, which ->
                currentLanguageIndex = which
                val langName = languages[which].split(" ")[0]
                updateLanguageButton(langName)
                Toast.makeText(this, "Language switched to ${languages[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun updateLanguageButton(langCode: String) {
        binding.btnLanguageQuick.text = "🌐 $langCode"
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    fun navigateToFragment(fragment: Fragment) {
        loadFragment(fragment, true)
    }

    fun navigateToBottomNav(menuItemId: Int) {
        binding.bottomNavigation.selectedItemId = menuItemId
    }
}