package com.reportit.app

import android.app.Application
import android.content.Context
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.reportit.app.utils.LocaleHelper

class ReportItApplication : Application() {

    companion object {
        const val ONESIGNAL_APP_ID = "dc639f71-d339-4858-ab5b-05e1e26e221e"
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()

        // Enable verbose OneSignal logging for debugging and integration validation
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // Initialize OneSignal SDK with Application Context and App ID
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
    }
}
