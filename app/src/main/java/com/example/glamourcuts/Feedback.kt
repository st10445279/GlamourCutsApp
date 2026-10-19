package com.example.glamourcuts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedback")
data class UserFeedback(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
