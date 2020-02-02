package com.michaeljordanr.memesounds

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.flurry.android.FlurryAgent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

class CustomApplication : Application() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, Utils.FLURRY_KEY)
        AppCenter.start(
                this, Utils.APP_CENTER_KEY, Analytics::class.java, Crashes::class.java
        )

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    Log.d("FCM", token)
                })
    }

    fun sendAnalytics(event: String, params: Map<String, String>) {
        sendFirebaseAnalytics(event, params)
        sendOthersAnalytics(event, params)
    }

    private fun sendFirebaseAnalytics(event: String, params: Map<String, String>) {
        val bundle = Bundle()
        params.forEach {
            bundle.putString(it.key, it.value)
        }
        firebaseAnalytics.logEvent(event, bundle)
    }

    private fun sendOthersAnalytics(event: String, params: Map<String, String>) {
        FlurryAgent.logEvent(event, params)
        Analytics.trackEvent(event, params)
    }
}