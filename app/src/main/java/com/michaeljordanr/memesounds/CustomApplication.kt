package com.michaeljordanr.memesounds

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CustomApplication : Application() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(baseContext)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    Log.d("FCM", token.toString())
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

    }
}