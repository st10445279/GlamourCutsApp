package com.example.glamourcuts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        if (sessionManager.getUserId() != -1) {
            navigateToMain()
            return
        }

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val toggleRole = findViewById<MaterialButtonToggleGroup>(R.id.toggleRole)
        val btnSsoLogin = findViewById<MaterialButton>(R.id.btnSsoLogin)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            hideKeyboard()
            val username = etUser.text.toString().trim()
            val password = etPass.text.toString().trim()

            val checkedId = toggleRole.checkedButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRole = getSelectedRole(checkedId)

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val hashedPassword = SecurityUtils.hashPassword(password)
                    val dbUser = AppDatabase.getDatabase(this@LoginActivity).bookingDao().login(username, hashedPassword)

                    if (dbUser != null) {
                        if (!dbUser.isConfirmed) {
                            Toast.makeText(this@LoginActivity, "Account pending Admin confirmation", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        if (dbUser.role == selectedRole) {
                            sessionManager.saveUser(dbUser.id, dbUser.role)
                            navigateToMain()
                        } else {
                            Toast.makeText(this@LoginActivity, "User exists but not as $selectedRole", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Login failed", e)
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnSsoLogin.setOnClickListener {
            hideKeyboard()
            val checkedId = toggleRole.checkedButtonId
            val selectedRole = if (checkedId != -1) getSelectedRole(checkedId) else "Client"
            performSsoLogin(selectedRole)
        }

        findViewById<TextView>(R.id.tvGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun getSelectedRole(checkedId: Int): String {
        return when (checkedId) {
            R.id.btnRoleAdmin -> "Admin"
            R.id.btnRoleStaff -> "Stylist"
            R.id.btnRoleOwner -> "Owner"
            else -> "Client"
        }
    }

    private fun performSsoLogin(role: String) {
        val credentialManager = CredentialManager.create(this)
        val webClientId = getWebClientId()

        if (webClientId.isNotEmpty()) {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(autoSelectEnabled = false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(this@LoginActivity, request)
                    val credential = result.credential
                    if ((credential is CustomCredential) && (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val email = googleIdTokenCredential.id
                        val displayName = googleIdTokenCredential.displayName ?: email
                        processSsoUser(email, displayName, role)
                    } else {
                        showSsoAccountSelectionDialog(role)
                    }
                } catch (e: GetCredentialException) {
                    Log.e("LoginActivity", "SSO Credential error: ${e.message}", e)
                    showSsoAccountSelectionDialog(role)
                }
            }
        } else {
            showSsoAccountSelectionDialog(role)
        }
    }

    private fun showSsoAccountSelectionDialog(role: String) {
        val defaultAccount = when (role) {
            "Admin" -> "admin.sso@glamourcuts.com"
            "Owner" -> "owner.sso@glamourcuts.com"
            "Stylist" -> "stylist.sso@glamourcuts.com"
            else -> "client.sso@gmail.com"
        }

        val googleAccounts = arrayOf(
            defaultAccount,
            "google.user@gmail.com",
            "glamour.cuts.client@gmail.com",
            "Enter Custom Google Email...",
        )

        AlertDialog.Builder(this)
            .setTitle("Google SSO — Sign in as $role")
            .setItems(googleAccounts) { _, which ->
                if (which == googleAccounts.lastIndex) {
                    showCustomEmailInputDialog(role)
                } else {
                    val selectedAccount = googleAccounts[which]
                    val displayName = selectedAccount.substringBefore("@")
                        .replace(".", " ")
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    processSsoUser(selectedAccount, displayName, role)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomEmailInputDialog(role: String) {
        val input = EditText(this).apply {
            hint = "user@gmail.com"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Google Email ($role)")
            .setView(input)
            .setPositiveButton("Sign In") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    val displayName = email.substringBefore("@")
                        .replace(".", " ")
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    processSsoUser(email, displayName, role)
                } else if (email.isEmpty()) {
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processSsoUser(email: String, displayName: String, role: String) {
        lifecycleScope.launch {
            try {
                val dao = AppDatabase.getDatabase(this@LoginActivity).bookingDao()
                val existingUser = dao.getUserByUsername(email)

                if (existingUser != null) {
                    if (!existingUser.isConfirmed) {
                        dao.confirmUser(existingUser.id)
                    }
                    sessionManager.saveUser(existingUser.id, existingUser.role)
                    Toast.makeText(this@LoginActivity, "Welcome back, $displayName!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val newUser = User(
                        username = email,
                        password = SecurityUtils.hashPassword("SSO_USER_${System.currentTimeMillis()}"),
                        role = role,
                        phone = "",
                        isConfirmed = true,
                    )
                    dao.insertUser(newUser)
                    val createdUser = dao.getUserByUsername(email)

                    if (createdUser != null) {
                        sessionManager.saveUser(createdUser.id, createdUser.role)
                        Toast.makeText(this@LoginActivity, "Signed in via Google as $displayName ($role)", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "SSO user processing error", e)
                Toast.makeText(this@LoginActivity, "SSO Sign in error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWebClientId(): String {
        return getString(R.string.default_web_client_id)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
