package com.example.ebsps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var surveyAdapter: SurveyAdapter
    private lateinit var db: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance()
        
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        surveyAdapter = SurveyAdapter(onDeleteSurvey = { survey ->
            showDeleteConfirmationDialog(survey)
        })
        recyclerView.adapter = surveyAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Set up FAB
        val fabAddSurvey = findViewById<FloatingActionButton>(R.id.fab_add_survey)
        fabAddSurvey.setOnClickListener {
            val intent = Intent(this, AddSurveyActivity::class.java)
            startActivity(intent)
        }
        
        // Load surveys from Firestore
        loadSurveys()
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "=== onResume() CALLED ===")
        Log.d("MainActivity", "Current activity state: ${if (isFinishing) "FINISHING" else "ACTIVE"}")
        
        // Reload surveys when returning from AddSurveyActivity
        loadSurveys()
    }
    
    private fun showDeleteConfirmationDialog(survey: Survey) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Survey")
            .setMessage("Apakah Anda yakin ingin menghapus survey untuk ${survey.ownerName}?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                deleteSurvey(survey)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun deleteSurvey(survey: Survey) {
        survey.id?.let { surveyId ->
            db.collection("surveys")
                .document(surveyId)
                .delete()
                .addOnSuccessListener {
                    Log.d("MainActivity", "Survey deleted successfully: $surveyId")
                    Toast.makeText(this, "Survey berhasil dihapus", Toast.LENGTH_SHORT).show()
                    // Survey will be automatically removed from the list due to Firestore listener
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error deleting survey: $e")
                    Toast.makeText(this, "Gagal menghapus survey: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun loadSurveys() {
        Log.d("MainActivity", "=== LOADING SURVEYS FROM FIRESTORE ===")
        Log.d("MainActivity", "Firestore instance: $db")
        
        try {
            db.collection("surveys")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("MainActivity", "=== FIRESTORE ERROR ===")
                        Log.e("MainActivity", "Error getting documents: ${e.message}")
                        Log.e("MainActivity", "Error type: ${e.javaClass.simpleName}")
                        Log.e("MainActivity", "Full error: $e")
                        return@addSnapshotListener
                    }
                    
                    Log.d("MainActivity", "=== FIRESTORE SNAPSHOT RECEIVED ===")
                    Log.d("MainActivity", "Snapshot exists: ${snapshot != null}")
                    Log.d("MainActivity", "Snapshot metadata: ${snapshot?.metadata}")
                    
                    val surveys = mutableListOf<Survey>()
                    if (snapshot != null && !snapshot.isEmpty) {
                        Log.d("MainActivity", "Snapshot has ${snapshot.size()} documents")
                        
                        for (document in snapshot) {
                            Log.d("MainActivity", "Processing document: ${document.id}")
                            Log.d("MainActivity", "Document data: ${document.data}")
                            
                            val survey = document.toObject(Survey::class.java)
                            if (survey != null) {
                                val surveyWithId = survey.copy(id = document.id)
                                surveys.add(surveyWithId)
                                Log.d("MainActivity", "Added survey: ${surveyWithId.ownerName} - ${surveyWithId.address}")
                            } else {
                                Log.w("MainActivity", "Failed to convert document ${document.id} to Survey object")
                            }
                        }
                        
                        Log.d("MainActivity", "Successfully loaded ${surveys.size} surveys")
                    } else {
                        Log.d("MainActivity", "Snapshot is null or empty")
                    }
                    
                    Log.d("MainActivity", "Updating adapter with ${surveys.size} surveys...")
                    surveyAdapter.updateSurveys(surveys)
                    Log.d("MainActivity", "Adapter updated successfully")
                }
        } catch (e: Exception) {
            Log.e("MainActivity", "=== EXCEPTION IN LOAD SURVEYS ===")
            Log.e("MainActivity", "Exception: ${e.message}")
            Log.e("MainActivity", "Exception type: ${e.javaClass.simpleName}")
            Log.e("MainActivity", "Full exception: $e")
        }
    }
}