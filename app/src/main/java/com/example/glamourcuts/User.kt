package com.example.glamourcuts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val role: String, // "Client", "Stylist", "Admin", "Owner"
    val phone: String = "",
    val isConfirmed: Boolean = true // Default true, will set false for new Stylists
)
