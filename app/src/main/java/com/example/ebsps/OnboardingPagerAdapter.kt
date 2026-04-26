package com.example.ebsps

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    private val onboardingData = listOf(
        OnboardingSlide(
            "Selamat Datang di e-BSPS",
            "Aplikasi untuk membantu survey dan pendataan rumah program BSPS.",
            R.drawable.undraw_task
        ),
        OnboardingSlide(
            "Survey Cepat & Mudah",
            "Lakukan pendataan kondisi rumah secara digital langsung dari lapangan.",
            R.drawable.undraw_email
        ),
        OnboardingSlide(
            "Laporan Terstruktur",
            "Hasil survey tersimpan rapi dan mudah untuk dilihat kembali.",
            R.drawable.undraw_complete
        )
    )
    
    override fun getItemCount(): Int = onboardingData.size
    
    override fun createFragment(position: Int): Fragment {
        val slide = onboardingData[position]
        return OnboardingFragment.newInstance(slide.title, slide.description, slide.imageResource)
    }
}

data class OnboardingSlide(
    val title: String,
    val description: String,
    val imageResource: Int
)

