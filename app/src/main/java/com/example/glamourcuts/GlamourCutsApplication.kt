package com.example.glamourcuts

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class GlamourCutsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val sessionManager = SessionManager(this)
        val savedLang = sessionManager.getLanguage()
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) {
            val localeList = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}
