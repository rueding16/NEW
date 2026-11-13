package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("credipulse_session", Context.MODE_PRIVATE)

    fun saveSession(userId: Long, username: String, role: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROLE, role)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getSavedUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id != -1L) id else null
    }

    fun getSavedUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun saveFirebaseUid(uid: String) {
        prefs.edit().putString(KEY_FIREBASE_UID, uid).apply()
    }

    fun getSavedFirebaseUid(): String? = prefs.getString(KEY_FIREBASE_UID, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    companion object {
        private const val KEY_USER_ID = "saved_user_id"
        private const val KEY_USERNAME = "saved_username"
        private const val KEY_ROLE = "saved_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_FIREBASE_UID = "saved_firebase_uid"
    }
}
