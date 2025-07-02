package com.example.to_do

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
        Timber.d("TodoApplication: Application started")
        
        // Initialize Firebase
        try {
            Timber.d("TodoApplication: Initializing Firebase...")
            val app = FirebaseApp.initializeApp(this)
            if (app != null) {
                Timber.d("TodoApplication: Firebase initialized with project ID: ${app.options.projectId}")
            } else {
                Timber.w("TodoApplication: Firebase app is null after initialization")
            }
            
            // Configure Firestore
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings
            
            Timber.d("TodoApplication: Firestore configured with persistence enabled")
        } catch (e: Exception) {
            Timber.e(e, "TodoApplication: Firebase initialization failed: ${e.message}")
            
            // Log more details about the error
            if (e.message?.contains("API key") == true || e.message?.contains("google-services.json") == true) {
                Timber.e("TodoApplication: Error might be related to missing or invalid google-services.json file")
            }
            
            if (e.message?.contains("project") == true) {
                Timber.e("TodoApplication: Error might be related to Firebase project configuration")
            }
        }
    }
}