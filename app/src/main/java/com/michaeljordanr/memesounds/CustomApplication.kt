package com.michaeljordanr.memesounds

import android.app.Application
import com.flurry.android.FlurryAgent
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, Utils.FLURRY_KEY)
        AppCenter.start(
                this, Utils.APP_CENTER_KEY, Analytics::class.java, Crashes::class.java
        )
    }
}