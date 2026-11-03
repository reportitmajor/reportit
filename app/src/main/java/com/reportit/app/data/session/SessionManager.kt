package com.reportit.app.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "reportit_auth_session"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_IS_GUEST = "key_is_guest"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_AUTH_PROVIDER = "key_auth_provider"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_FIREBASE_UID = "key_firebase_uid"

        const val PROVIDER_EMAIL = "EMAIL"
        const val PROVIDER_GOOGLE = "GOOGLE"
        const val PROVIDER_GUEST = "GUEST"

        const val ROLE_CITIZEN = "CITIZEN"
        const val ROLE_OFFICER = "OFFICER"
    }

    fun saveUserSession(
        name: String,
        email: String,
        provider: String = PROVIDER_EMAIL,
        role: String = ROLE_CITIZEN,
        uid: String = ""
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_GUEST, false)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_AUTH_PROVIDER, provider)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_FIREBASE_UID, uid)
            .apply()
    }

    fun getFirebaseUid(): String = prefs.getString(KEY_FIREBASE_UID, "") ?: ""

    fun startGuestSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_GUEST, true)
            .putString(KEY_USER_NAME, "Guest Citizen")
            .putString(KEY_USER_EMAIL, "guest@reportit.app")
            .putString(KEY_AUTH_PROVIDER, PROVIDER_GUEST)
            .putString(KEY_USER_ROLE, ROLE_CITIZEN)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun isGuestMode(): Boolean = prefs.getBoolean(KEY_IS_GUEST, false)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Citizen User") ?: "Citizen User"

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "user@reportit.app") ?: "user@reportit.app"

    fun getAuthProvider(): String = prefs.getString(KEY_AUTH_PROVIDER, PROVIDER_EMAIL) ?: PROVIDER_EMAIL

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, ROLE_CITIZEN) ?: ROLE_CITIZEN

    fun logout() {
        prefs.edit().clear().apply()
    }
}
