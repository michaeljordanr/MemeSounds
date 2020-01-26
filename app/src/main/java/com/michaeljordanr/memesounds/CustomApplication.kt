package com.michaeljordanr.memesounds

import android.app.Application
import android.util.Log
import com.flurry.android.FlurryAgent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
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

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    Log.d("FCM", token)
                })
    }
}