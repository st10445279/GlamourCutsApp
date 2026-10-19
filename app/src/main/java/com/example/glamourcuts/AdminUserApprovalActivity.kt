package com.example.glamourcuts

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AdminUserApprovalActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: PendingUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_approval)

        db = AppDatabase.getDatabase(this)
        val rv = findViewById<RecyclerView>(R.id.rvPendingUsers)
        rv.layoutManager = LinearLayoutManager(this)

        loadPendingUsers()
    }

    private fun loadPendingUsers() {
        lifecycleScope.launch {
            val pending = db.bookingDao().getPendingUsers()
            adapter = PendingUserAdapter(pending) { user ->
                confirmUser(user)
            }
            findViewById<RecyclerView>(R.id.rvPendingUsers).adapter = adapter
        }
    }

    private fun confirmUser(user: User) {
        lifecycleScope.launch {
            db.bookingDao().confirmUser(user.id)
            Toast.makeText(this@AdminUserApprovalActivity, "${user.username} confirmed!", Toast.LENGTH_SHORT).show()
            loadPendingUsers()
        }
    }
}
