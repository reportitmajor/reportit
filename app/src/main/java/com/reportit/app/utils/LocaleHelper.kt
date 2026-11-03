package com.reportit.app.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    const val PREFS_NAME = "reportit_lang_pref"
    const val KEY_LANG_TAG = "selected_lang_tag"
    const val KEY_LANG_INDEX = "selected_lang_index"

    val SUPPORTED_LANG_NAMES = arrayOf(
        "English (Default)",
        "ಕನ್ನಡ (Kannada)",
        "हिन्दी (Hindi)",
        "தமிழ் (Tamil)",
        "తెలుగు (Telugu)"
    )

    val SUPPORTED_LANG_TAGS = arrayOf("en", "kn", "hi", "ta", "te")

    fun getSelectedLanguageTag(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG_TAG, "en") ?: "en"
    }

    fun getSelectedLanguageIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_LANG_INDEX, 0)
    }

    fun setLocale(context: Context, languageTag: String, index: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LANG_TAG, languageTag)
            .putInt(KEY_LANG_INDEX, index)
            .commit()

        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        // AndroidX per-app language setting
        val appLocale = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocale)

        // Update active context resources configuration
        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)

        // Also update application context resources
        val appRes = context.applicationContext.resources
        val appConfig = Configuration(appRes.configuration)
        appConfig.setLocale(locale)
        appConfig.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        appRes.updateConfiguration(appConfig, appRes.displayMetrics)
    }

    fun wrap(context: Context): Context {
        val langTag = getSelectedLanguageTag(context)
        val locale = Locale.forLanguageTag(langTag)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun applyConfiguration(config: Configuration?, context: Context) {
        if (config == null) return
        val langTag = getSelectedLanguageTag(context)
        val locale = Locale.forLanguageTag(langTag)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
    }
}

