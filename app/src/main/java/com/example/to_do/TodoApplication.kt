package com.example.to_do


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // You can initialize other app-wide components here if needed
        // For example: logging libraries, crash reporting, etc.
    }
}