package com.example.remotedesktop

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // If you're using Firebase Analytics, you might want to enable it here
        // FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)

        // If you're using other Firebase services like Firestore, Realtime Database, etc.,
        // you can initialize them here as well
    }
}