package com.example.glamourcuts

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class CommunicationActivity : AppCompatActivity() {
    private lateinit var adapter: MessageAdapter
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication)

        db = AppDatabase.getDatabase(this)
        session = SessionManager(this)

        val rv = findViewById<RecyclerView>(R.id.rvMessages)
        rv.layoutManager = LinearLayoutManager(this)
        
        val role = session.getUserRole() ?: ""
        val inputArea = findViewById<View>(R.id.cardMessageInput)
        
        // Ensure Stylists, Admins and Owners can send messages
        if (role == "Stylist" || role == "Admin" || role == "Owner") {
            inputArea.visibility = View.VISIBLE
        } else {
            inputArea.visibility = View.GONE
        }

        loadMessages()

        findViewById<View>(R.id.btnSendMessage).setOnClickListener {
            val content = findViewById<EditText>(R.id.etMessage).text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val currentUserId = session.getUserId()
            val messages = db.bookingDao().getAllMessages()
            adapter = MessageAdapter(messages, currentUserId)
            findViewById<RecyclerView>(R.id.rvMessages).adapter = adapter
            if (messages.isNotEmpty()) {
                findViewById<RecyclerView>(R.id.rvMessages).scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun sendMessage(content: String) {
        lifecycleScope.launch {
            val userId = session.getUserId()
            val user = db.bookingDao().getUserById(userId)
            
            val msg = Message(
                senderId = userId,
                senderName = user?.username ?: "User $userId", 
                senderRole = user?.role ?: "Staff",
                content = content
            )
            db.bookingDao().insertMessage(msg)
            findViewById<EditText>(R.id.etMessage).text.clear()
            loadMessages()
        }
    }
}
