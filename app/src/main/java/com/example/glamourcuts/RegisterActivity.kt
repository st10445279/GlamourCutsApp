package com.example.glamourcuts

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUser = findViewById<EditText>(R.id.etRegUsername)
        val etPhone = findViewById<EditText>(R.id.etRegPhone)
        val etPass = findViewById<EditText>(R.id.etRegPassword)
        val toggleRole = findViewById<MaterialButtonToggleGroup>(R.id.toggleRegRole)
        val btnRegister = findViewById<Button>(R.id.btnDoRegister)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            val username = etUser.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPass.text.toString().trim()
            
            val selectedRole = if (toggleRole.checkedButtonId == R.id.btnRegStaff) "Stylist" else "Client"

            if (username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length < 5) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val dao = AppDatabase.getDatabase(this@RegisterActivity).bookingDao()
                    val existing = dao.getUserByUsername(username)

                    if (existing != null) {
                        Toast.makeText(this@RegisterActivity, "Username already taken", Toast.LENGTH_SHORT).show()
                    } else {
                        val hashedPassword = SecurityUtils.hashPassword(password)
                        val isConfirmed = selectedRole == "Client"
                        
                        dao.insertUser(
                            User(
                                username = username, 
                                password = hashedPassword, 
                                role = selectedRole, 
                                phone = phone,
                                isConfirmed = isConfirmed,
                            )
                        )
                        
                        val msg = if (isConfirmed) 
                            "Registration successful! Please login." 
                        else 
                            "Registration submitted! Waiting for Admin confirmation."
                            
                        Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_LONG).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvBack.setOnClickListener {
            finish() // Close and go back
        }
    }
}
