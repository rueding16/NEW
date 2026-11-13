package com.example.util

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

private const val TAG = "AppCheckConfig"

fun configureAppCheck(activity: Activity, intent: Intent?) {
    try {
        FirebaseApp.initializeApp(activity)
        val debugToken = intent?.getStringExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")
        if (!debugToken.isNullOrBlank()) {
            Log.d(TAG, "Supplied AppCheck debug token from intent")
        }
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        Log.d(TAG, "AppCheck configured successfully")
    } catch (e: Exception) {
        Log.w(TAG, "AppCheck configuration note: ${e.message}")
    }
}
