package com.example.ebsps

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class DetailSurveyActivity : AppCompatActivity() {
    
    private lateinit var ivSurveyImage: ImageView
    private lateinit var tvOwnerName: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvRoofCondition: TextView
    private lateinit var tvWallCondition: TextView
    private lateinit var tvFloorCondition: TextView
    private lateinit var tvSanitationCondition: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var btnBack: ImageButton
    
    private lateinit var db: FirebaseFirestore
    private var surveyId: String = ""
    private var currentImagePath: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_survey)
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        
        // Get survey ID from intent
        surveyId = intent.getStringExtra("survey_id") ?: ""
        if (surveyId.isEmpty()) {
            finish()
            return
        }
        
        // Initialize views
        initializeViews()
        
        // Set up click listeners
        setupClickListeners()
        
        // Load survey data
        loadSurveyData()
    }
    
    private fun initializeViews() {
        ivSurveyImage = findViewById(R.id.iv_survey_image)
        tvOwnerName = findViewById(R.id.tv_owner_name)
        tvAddress = findViewById(R.id.tv_address)
        tvRoofCondition = findViewById(R.id.tv_roof_condition)
        tvWallCondition = findViewById(R.id.tv_wall_condition)
        tvFloorCondition = findViewById(R.id.tv_floor_condition)
        tvSanitationCondition = findViewById(R.id.tv_sanitation_condition)
        tvTimestamp = findViewById(R.id.tv_timestamp)
        btnBack = findViewById(R.id.btn_back)
    }
    
    private fun setupClickListeners() {
        ivSurveyImage.setOnClickListener {
            if (currentImagePath.isNotEmpty()) {
                showImageZoomDialog()
            }
        }
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }
    
    private fun showImageZoomDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_zoom_image)
        
        val photoView = dialog.findViewById<ImageView>(R.id.photo_view)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btn_close)
        
        // Load image into PhotoView
        try {
            val imageFile = File(currentImagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(photoView)
            } else {
                photoView.setImageResource(R.drawable.error_image)
            }
        } catch (e: Exception) {
            Log.e("DetailSurveyActivity", "Error loading image in zoom dialog", e)
            photoView.setImageResource(R.drawable.error_image)
        }
        
        // Set close button click listener
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        // Show dialog
        dialog.show()
    }
    
    private fun loadSurveyData() {
        db.collection("surveys")
            .document(surveyId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val survey = document.toObject(Survey::class.java)
                    survey?.let {
                        val surveyWithId = it.copy(id = document.id)
                        displaySurveyData(surveyWithId)
                    }
                } else {
                    Log.d("DetailSurveyActivity", "No such document")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.w("DetailSurveyActivity", "Error getting document", e)
                finish()
            }
    }
    
    private fun displaySurveyData(survey: Survey) {
        // Store image path for zoom functionality
        currentImagePath = survey.mainPhotoUrl
        
        // Load image from local file path
        if (survey.mainPhotoUrl.isNotEmpty()) {
            try {
                val imageFile = File(survey.mainPhotoUrl)
                if (imageFile.exists()) {
                    Log.d("DetailSurveyActivity", "Loading image from local path: ${survey.mainPhotoUrl}")
                    Glide.with(this)
                        .load(imageFile)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivSurveyImage)
                } else {
                    Log.w("DetailSurveyActivity", "Image file not found: ${survey.mainPhotoUrl}")
                    ivSurveyImage.setImageResource(R.drawable.error_image)
                }
            } catch (e: Exception) {
                Log.e("DetailSurveyActivity", "Error loading local image", e)
                ivSurveyImage.setImageResource(R.drawable.error_image)
            }
        } else {
            ivSurveyImage.setImageResource(R.drawable.placeholder_image)
        }
        
        // Display text data
        tvOwnerName.text = survey.ownerName
        tvAddress.text = survey.address
        tvRoofCondition.text = survey.roofCondition
        tvWallCondition.text = survey.wallCondition
        tvFloorCondition.text = survey.floorCondition
        tvSanitationCondition.text = survey.sanitationCondition
        
        // Format and display timestamp
        val timestamp = survey.timestamp
        val dateFormat = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", java.util.Locale("id", "ID"))
        val formattedDate = dateFormat.format(timestamp.toDate())
        tvTimestamp.text = "Dibuat pada: $formattedDate"
    }
}
