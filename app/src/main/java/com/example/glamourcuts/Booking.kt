package com.example.glamourcuts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 0, // 0 for guest, or link to User.id
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val service: String,
    val date: String,
    val time: String,
    val status: String = "Waiting Confirmation", // Updated default status
    val pricePaid: String = ""
)
