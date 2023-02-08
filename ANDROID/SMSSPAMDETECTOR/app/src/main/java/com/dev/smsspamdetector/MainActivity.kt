package com.dev.smsspamdetector

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // make the app works in light mode only
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val currentNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        if (currentNightMode != Configuration.UI_MODE_NIGHT_NO) {
            //// of the dark mode is not removed the code below wont execute to prevent code execute duplication
            return
        }
        setContentView(R.layout.activity_main)
        val intent = Intent(this, Installation::class.java)
        // launching a new screen after 3.5 seconds
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, 3500)
    }


}