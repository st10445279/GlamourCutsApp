package com.example.glamourcuts

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    private val languages = listOf(
        LanguageOption("English", "en"),
        LanguageOption("Afrikaans", "af"),
        LanguageOption("isiZulu", "zu"),
        LanguageOption("Spanish", "es"),
        LanguageOption("French", "fr"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sessionManager = SessionManager(this)

        val tvRoleBadge = findViewById<TextView>(R.id.tvRoleBadge)
        val etUsername = findViewById<EditText>(R.id.etSettingsUsername)
        val etPhone = findViewById<EditText>(R.id.etSettingsPhone)
        val etPassword = findViewById<EditText>(R.id.etSettingsPassword)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val btnApplyLanguage = findViewById<Button>(R.id.btnApplyLanguage)
        val btnBack = findViewById<Button>(R.id.btnSettingsBack)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages.map { it.displayName },
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val currentLangCode = sessionManager.getLanguage()
        val initialLangIndex = languages.indexOfFirst { it.code == currentLangCode }.takeIf { it >= 0 } ?: 0
        spinnerLanguage.setSelection(initialLangIndex)

        val userId = sessionManager.getUserId()
        if (userId != -1) {
            lifecycleScope.launch {
                val dao = AppDatabase.getDatabase(this@SettingsActivity).bookingDao()
                val user = dao.getUserById(userId)
                if (user != null) {
                    currentUser = user
                    tvRoleBadge.text = getString(R.string.role_format, user.role)
                    etUsername.setText(user.username)
                    etPhone.setText(user.phone)
                }
            }
        }

        btnSaveProfile.setOnClickListener {
            val updatedUser = currentUser ?: return@setOnClickListener
            val newUsername = etUsername.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()
            val newPassword = etPassword.text.toString().trim()

            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val dao = AppDatabase.getDatabase(this@SettingsActivity).bookingDao()
                val passwordToSave = if (newPassword.isNotEmpty()) {
                    SecurityUtils.hashPassword(newPassword)
                } else {
                    updatedUser.password
                }

                val userToUpdate = updatedUser.copy(
                    username = newUsername,
                    phone = newPhone,
                    password = passwordToSave,
                )

                dao.updateUser(userToUpdate)
                currentUser = userToUpdate
                sessionManager.saveUser(userToUpdate.id, userToUpdate.role)

                Toast.makeText(this@SettingsActivity, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
            }
        }

        btnApplyLanguage.setOnClickListener {
            val selectedIndex = spinnerLanguage.selectedItemPosition
            if (selectedIndex in languages.indices) {
                val selectedLang = languages[selectedIndex]
                sessionManager.saveLanguage(selectedLang.code)

                lifecycleScope.launch {
                    val dao = AppDatabase.getDatabase(this@SettingsActivity).bookingDao()
                    dao.saveSetting(AppSetting("APP_LANGUAGE", selectedLang.code))
                }

                val localeList = LocaleListCompat.forLanguageTags(selectedLang.code)
                AppCompatDelegate.setApplicationLocales(localeList)

                Toast.makeText(this, getString(R.string.language_updated), Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnOpenHelpCenter).setOnClickListener {
            startActivity(android.content.Intent(this, HelpSupportActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private data class LanguageOption(val displayName: String, val code: String)
}
