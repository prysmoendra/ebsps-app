package com.example.ebsps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var btnSelesai: Button
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        viewPager = findViewById(R.id.viewPager)
        btnSelesai = findViewById(R.id.btn_selesai)
        indicator1 = findViewById(R.id.indicator_1)
        indicator2 = findViewById(R.id.indicator_2)
        indicator3 = findViewById(R.id.indicator_3)
        
        val adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter
        
        // Set initial indicator state
        updateIndicators(0)
        
        // Update button text and indicators based on current page
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
                updateButtonText(position, adapter.itemCount)
            }
        })
        
        btnSelesai.setOnClickListener {
            // Mark onboarding as completed
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("onboarding_completed", true).apply()
            
            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateIndicators(position: Int) {
        // Reset all indicators to inactive (gray)
        indicator1.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        indicator2.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        indicator3.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        
        // Set active indicator based on position (orange)
        when (position) {
            0 -> indicator1.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
            1 -> indicator2.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
            2 -> indicator3.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
        }
    }
    
    private fun updateButtonText(position: Int, totalPages: Int) {
        if (position == totalPages - 1) {
            btnSelesai.text = "Selesai"
        } else {
            btnSelesai.text = "Lewati"
        }
    }
}
