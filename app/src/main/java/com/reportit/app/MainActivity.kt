package com.reportit.app

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.onesignal.OneSignal
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.reportit.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val languages = arrayOf(
        "English (Default)",
        "ಕನ್ನಡ (Kannada)",
        "हिन्दी (Hindi)",
        "தமிழ் (Tamil)",
        "తెలుగు (Telugu)"
    )
    private val languageTags = arrayOf("en", "kn", "hi", "ta", "te")
    private var currentLanguageIndex = 0

    private val colorActive = Color.parseColor("#0066FF")
    private val colorInactive = Color.parseColor("#64748B")

    // Retained OneSignal push subscription observer
    private var pushSubscriptionObserver: IPushSubscriptionObserver? = null
    private var hasShownOneSignalDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("reportit_lang_pref", Context.MODE_PRIVATE)
        currentLanguageIndex = prefs.getInt("selected_lang_index", 0)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle System Insets: status bar at the top and gesture pill / nav bar at the bottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainer.setPadding(0, systemBars.top, 0, 0)
            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Set initial fragment to Home
        if (savedInstanceState == null) {
            selectTab(0)
        }

        // Preload GBA GIS Ward boundaries asynchronously
        com.reportit.app.gis.GbaSpatialManager.init(this)

        setupBottomNavigation()

        // Setup OneSignal push verification observer
        setupOneSignalVerification()
    }

    private fun setupBottomNavigation() {
        binding.navHome.setOnClickListener {
            selectTab(0)
        }

        binding.navReports.setOnClickListener {
            selectTab(1)
        }

        binding.fabCenterCamera.setOnClickListener {
            navigateToFragment(ReportIssueFragment())
        }

        binding.navMap.setOnClickListener {
            selectTab(2)
        }

        binding.navProfile.setOnClickListener {
            selectTab(3)
        }
    }

    private fun selectTab(index: Int) {
        // Reset all tabs
        setTabState(binding.ivNavHome, binding.tvNavHome, false)
        setTabState(binding.ivNavReports, binding.tvNavReports, false)
        setTabState(binding.ivNavMap, binding.tvNavMap, false)
        setTabState(binding.ivNavProfile, binding.tvNavProfile, false)

        when (index) {
            0 -> {
                setTabState(binding.ivNavHome, binding.tvNavHome, true)
                loadFragment(HomeFragment(), false)
            }
            1 -> {
                setTabState(binding.ivNavReports, binding.tvNavReports, true)
                loadFragment(ReportsFragment(), false)
            }
            2 -> {
                setTabState(binding.ivNavMap, binding.tvNavMap, true)
                loadFragment(MapFragment(), false)
            }
            3 -> {
                setTabState(binding.ivNavProfile, binding.tvNavProfile, true)
                loadFragment(ProfileFragment(), false)
            }
        }
    }

    private fun setTabState(icon: ImageView, text: TextView, isActive: Boolean) {
        val color = if (isActive) colorActive else colorInactive
        icon.imageTintList = ColorStateList.valueOf(color)
        text.setTextColor(color)
        text.typeface = if (isActive) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
    }

    fun showLanguageDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_select_language))
            .setSingleChoiceItems(languages, currentLanguageIndex) { dialog, which ->
                switchAppLanguage(which)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    fun switchAppLanguage(which: Int) {
        if (which < 0 || which >= languageTags.size) return
        currentLanguageIndex = which
        val langTag = languageTags[which]

        val prefs = getSharedPreferences("reportit_lang_pref", Context.MODE_PRIVATE)
        prefs.edit().putInt("selected_lang_index", which).putString("selected_lang_tag", langTag).apply()

        // Apply AndroidX runtime locale switching across entire application
        val appLocale = LocaleListCompat.forLanguageTags(langTag)
        AppCompatDelegate.setApplicationLocales(appLocale)

        Toast.makeText(this, "Language switched to ${languages[which]}", Toast.LENGTH_SHORT).show()
    }

    fun updateLanguageButton(langCode: String) {
        val index = languageTags.indexOfFirst { it.equals(langCode, ignoreCase = true) }
        if (index >= 0) {
            switchAppLanguage(index)
        }
    }

    fun showNotificationsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("🔔 Civic Notifications (3 New)")
            .setMessage(
                "1. BBMP Action: Crew deployed for Silk Board waterlogging.\n" +
                "2. Report #NR12345 (Pothole at Kasavanahalli) assigned to BBMP Road Dept.\n" +
                "3. Report #NR12340 (Garbage at Sarjapur Rd) marked RESOLVED! Please verify."
            )
            .setPositiveButton("Clear All", null)
            .show()
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
        when (menuItemId) {
            R.id.nav_home -> selectTab(0)
            R.id.nav_reports -> selectTab(1)
            R.id.nav_map -> selectTab(2)
            R.id.nav_track -> {
                setTabState(binding.ivNavHome, binding.tvNavHome, false)
                setTabState(binding.ivNavReports, binding.tvNavReports, true)
                setTabState(binding.ivNavMap, binding.tvNavMap, false)
                setTabState(binding.ivNavProfile, binding.tvNavProfile, false)
                loadFragment(TrackFragment(), false)
            }
            R.id.nav_profile -> selectTab(3)
        }
    }

    private fun setupOneSignalVerification() {
        // Sync citizen user ID if logged in
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (!currentUid.isNullOrEmpty()) {
            OneSignal.login(currentUid)
        }

        val observer = object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: com.onesignal.user.subscriptions.PushSubscriptionChangedState) {
                val newId = state.current.id
                if (!hasShownOneSignalDialog && !newId.isNullOrEmpty() && !newId.startsWith("local-")) {
                    hasShownOneSignalDialog = true
                    runOnUiThread {
                        showOneSignalIntegrationDialog()
                    }
                }
            }
        }
        pushSubscriptionObserver = observer
        OneSignal.User.pushSubscription.addObserver(observer)

        // Evaluate immediately in case subscription ID is already server-assigned
        val currentId = OneSignal.User.pushSubscription.id
        if (!hasShownOneSignalDialog && !currentId.isNullOrEmpty() && !currentId.startsWith("local-")) {
            hasShownOneSignalDialog = true
            showOneSignalIntegrationDialog()
        }
    }

    private fun showOneSignalIntegrationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Your OneSignal SDK integration is complete!")
            .setMessage("You can now send Push Notifications & In-App Messages through OneSignal. Tap below to enable push notifications.")
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch {
                    OneSignal.Notifications.requestPermission(true)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        pushSubscriptionObserver?.let {
            OneSignal.User.pushSubscription.removeObserver(it)
        }
    }
}