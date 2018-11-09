package com.michaeljordanr.memesounds

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity(), Runnable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val h = Handler()
        h.postDelayed(this, 2000)
    }

    override fun run() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
