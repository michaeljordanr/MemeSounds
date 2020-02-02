package com.michaeljordanr.memesounds

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity(), Runnable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(this, 2000)
    }

    override fun run() {
        val customApplication = applicationContext as? CustomApplication

        startActivity(Intent(this, MainActivity::class.java))
        finish()

        intent?.extras?.let {
            val url = it.getString(MyFirebaseMessagingService.WEB_URL_PARAM) ?: ""
            url.isNotBlank().let {
                val openBrowserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(openBrowserIntent)

                customApplication?.let { app ->
                    val params = HashMap<String, String>()
                    params["url"] = url

                    app.sendAnalytics("open_url", params)
                }
            }
        }
    }
}
