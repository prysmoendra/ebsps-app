package com.example.ebsps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        
        // Navigate to appropriate activity after 2.5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val onboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)
            
            val intent = if (onboardingCompleted) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            
            startActivity(intent)
            finish() // Close the splash screen
        }, 2500) // 2.5 seconds = 2500 milliseconds
    }
}
