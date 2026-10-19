package com.example.glamourcuts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderId: Int,
    val senderName: String,
    val senderRole: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
