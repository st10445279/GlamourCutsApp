package com.example.glamourcuts

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("GlamourPrefs", Context.MODE_PRIVATE)

    fun saveUser(userId: Int, role: String) {
        prefs.edit().apply {
            putInt("USER_ID", userId)
            putString("USER_ROLE", role)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt("USER_ID", -1)
    fun getUserRole(): String? = prefs.getString("USER_ROLE", null)

    fun saveLanguage(languageCode: String) {
        prefs.edit().putString("APP_LANGUAGE", languageCode).apply()
    }

    fun getLanguage(): String = prefs.getString("APP_LANGUAGE", "en") ?: "en"

    fun logout() {
        prefs.edit().clear().apply()
    }
}
