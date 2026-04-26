package com.example.ebsps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddSurveyActivity : AppCompatActivity() {
    
    private lateinit var etOwnerName: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnTakePhoto: Button
    private lateinit var btnSaveSurvey: Button
    private lateinit var btnBack: ImageButton
    
    private lateinit var rgRoofCondition: RadioGroup
    private lateinit var rgWallCondition: RadioGroup
    private lateinit var rgFloorCondition: RadioGroup
    private lateinit var rgSanitationCondition: RadioGroup
    
    private lateinit var db: FirebaseFirestore
    
    private var photoUri: Uri? = null
    private var localImagePath: String = ""
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val CAMERA_REQUEST = 101
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_survey)
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        
        // Log Firebase configuration
        Log.d("AddSurveyActivity", "Firebase Firestore initialized")
        
        // Initialize views
        initializeViews()
        
        // Set up click listeners
        setupClickListeners()
    }
    
    private fun savePhotoToInternalStorage() {
        try {
            photoUri?.let { uri ->
                Log.d("AddSurveyActivity", "Saving photo to internal storage from URI: $uri")
                
                // Read the photo data
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    // Create unique filename with timestamp
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "survey_image_${timeStamp}.jpg"
                    
                    // Get internal storage directory
                    val internalDir = File(filesDir, "survey_images")
                    if (!internalDir.exists()) {
                        internalDir.mkdirs()
                    }
                    
                    // Create the file
                    val imageFile = File(internalDir, fileName)
                    
                    // Save bitmap as JPEG
                    val fileOutputStream = FileOutputStream(imageFile)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
                    fileOutputStream.close()
                    
                    // Get absolute file path
                    localImagePath = imageFile.absolutePath
                    
                    Log.d("AddSurveyActivity", "Photo saved successfully to: $localImagePath")
                    Log.d("AddSurveyActivity", "File size: ${imageFile.length()} bytes")
                    
                    Toast.makeText(this, "Foto berhasil disimpan", Toast.LENGTH_SHORT).show()
                    
                    // Clean up bitmap to free memory
                    bitmap.recycle()
                } else {
                    Log.e("AddSurveyActivity", "Failed to decode bitmap from URI")
                    Toast.makeText(this, "Gagal membaca foto dari kamera", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Log.e("AddSurveyActivity", "Photo URI is null")
                Toast.makeText(this, "Error: URI foto tidak valid", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AddSurveyActivity", "Error saving photo to internal storage", e)
            Toast.makeText(this, "Error menyimpan foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun initializeViews() {
        etOwnerName = findViewById(R.id.et_owner_name)
        etAddress = findViewById(R.id.et_address)
        btnTakePhoto = findViewById(R.id.btn_take_photo)
        btnSaveSurvey = findViewById(R.id.btn_save_survey)
        btnBack = findViewById(R.id.btn_back)
        
        rgRoofCondition = findViewById(R.id.rg_roof_condition)
        rgWallCondition = findViewById(R.id.rg_wall_condition)
        rgFloorCondition = findViewById(R.id.rg_floor_condition)
        rgSanitationCondition = findViewById(R.id.rg_sanitation_condition)
    }
    
    private fun setupClickListeners() {
        btnTakePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        
        btnSaveSurvey.setOnClickListener {
            saveSurvey()
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    
    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            Log.d("AddSurveyActivity", "Created photo file: ${photoFile.absolutePath}")
            
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            Log.d("AddSurveyActivity", "Photo URI: $photoUri")
            
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST)
            } else {
                Log.e("AddSurveyActivity", "No camera app found")
                Toast.makeText(this, "Tidak ada aplikasi kamera yang tersedia", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AddSurveyActivity", "Error opening camera", e)
            Toast.makeText(this, "Error membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "SURVEY_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("AddSurveyActivity", "Photo captured successfully")
                savePhotoToInternalStorage()
            } else {
                Log.e("AddSurveyActivity", "Photo capture failed or cancelled")
                Toast.makeText(this, "Pengambilan foto dibatalkan atau gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveSurvey() {
        Log.d("AddSurveyActivity", "=== STARTING SURVEY SAVE PROCESS ===")
        
        // Validate required fields
        val ownerName = etOwnerName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        
        Log.d("AddSurveyActivity", "Owner Name: '$ownerName'")
        Log.d("AddSurveyActivity", "Address: '$address'")
        Log.d("AddSurveyActivity", "Local Image Path: '$localImagePath'")
        
        if (ownerName.isEmpty() || address.isEmpty()) {
            Log.w("AddSurveyActivity", "Validation failed: Empty owner name or address")
            Toast.makeText(this, "Nama pemilik dan alamat harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get selected radio button values
        val roofCondition = getSelectedRadioButtonText(rgRoofCondition)
        val wallCondition = getSelectedRadioButtonText(rgWallCondition)
        val floorCondition = getSelectedRadioButtonText(rgFloorCondition)
        val sanitationCondition = getSelectedRadioButtonText(rgSanitationCondition)
        
        Log.d("AddSurveyActivity", "Roof Condition: '$roofCondition'")
        Log.d("AddSurveyActivity", "Wall Condition: '$wallCondition'")
        Log.d("AddSurveyActivity", "Floor Condition: '$floorCondition'")
        Log.d("AddSurveyActivity", "Sanitation Condition: '$sanitationCondition'")
        
        if (roofCondition.isEmpty() || wallCondition.isEmpty() || 
            floorCondition.isEmpty() || sanitationCondition.isEmpty()) {
            Log.w("AddSurveyActivity", "Validation failed: Not all conditions selected")
            Toast.makeText(this, "Semua kondisi harus dipilih", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d("AddSurveyActivity", "All validations passed, creating progress dialog...")
        
        // Show progress dialog
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Menyimpan data survey...")
            setCancelable(false)
            show()
        }
        
        Log.d("AddSurveyActivity", "Progress dialog shown successfully")
        
        // Create Survey object
        val survey = Survey(
            ownerName = ownerName,
            address = address,
            mainPhotoUrl = localImagePath,
            roofCondition = roofCondition,
            wallCondition = wallCondition,
            floorCondition = floorCondition,
            sanitationCondition = sanitationCondition
        )
        
        Log.d("AddSurveyActivity", "Survey object created: $survey")
        Log.d("AddSurveyActivity", "Starting Firestore save operation...")
        
        // Save to Firestore and wait for response
        db.collection("surveys")
            .add(survey)
            .addOnSuccessListener { documentReference ->
                Log.d("AddSurveyActivity", "=== FIRESTORE SAVE SUCCESS ===")
                Log.d("AddSurveyActivity", "Document ID: ${documentReference.id}")
                
                // Dismiss progress dialog
                try {
                    if (progressDialog.isShowing) {
                        Log.d("AddSurveyActivity", "Dismissing progress dialog...")
                        progressDialog.dismiss()
                        Log.d("AddSurveyActivity", "Progress dialog dismissed successfully")
                    } else {
                        Log.w("AddSurveyActivity", "Progress dialog was already dismissed")
                    }
                } catch (e: Exception) {
                    Log.e("AddSurveyActivity", "Error dismissing progress dialog", e)
                }
                
                // Show success message
                Log.d("AddSurveyActivity", "Showing success toast...")
                Toast.makeText(this, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                
                // Navigate back to MainActivity
                Log.d("AddSurveyActivity", "Calling finish() to return to MainActivity...")
                finish()
                Log.d("AddSurveyActivity", "finish() called successfully")
            }
            .addOnFailureListener { e ->
                Log.e("AddSurveyActivity", "=== FIRESTORE SAVE FAILED ===")
                Log.e("AddSurveyActivity", "Error: ${e.message}")
                Log.e("AddSurveyActivity", "Error type: ${e.javaClass.simpleName}")
                Log.e("AddSurveyActivity", "Full error: $e")
                
                // Dismiss progress dialog
                try {
                    if (progressDialog.isShowing) {
                        Log.d("AddSurveyActivity", "Dismissing progress dialog due to failure...")
                        progressDialog.dismiss()
                        Log.d("AddSurveyActivity", "Progress dialog dismissed successfully")
                    }
                } catch (e2: Exception) {
                    Log.e("AddSurveyActivity", "Error dismissing progress dialog", e2)
                }
                
                // Show error message
                val errorMessage = when {
                    e.message?.contains("network") == true -> "Gagal menyimpan: Masalah koneksi internet"
                    e.message?.contains("permission") == true -> "Gagal menyimpan: Tidak ada izin akses"
                    e.message?.contains("collection") == true -> "Gagal menyimpan: Database collection tidak ditemukan"
                    else -> "Gagal menyimpan data: ${e.message}"
                }
                
                Log.d("AddSurveyActivity", "Showing error toast: $errorMessage")
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
    }
    
    private fun getSelectedRadioButtonText(radioGroup: RadioGroup): String {
        val selectedId = radioGroup.checkedRadioButtonId
        return if (selectedId != -1) {
            findViewById<RadioButton>(selectedId).text.toString()
        } else {
            ""
        }
    }
}
