package com.example.glamourcuts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val session = SessionManager(this)
        val role = session.getUserRole()

        val welcomeTitle = when (role) {
            "Admin" -> "Administrator Portal"
            "Owner" -> "Executive Suite"
            "Stylist" -> "Stylist Hub"
            else -> "Glamour Cuts"
        }
        findViewById<TextView>(R.id.tvWelcome).text = welcomeTitle

        val btnBook = findViewById<Button>(R.id.btnBookNow)
        val btnMyBookings = findViewById<Button>(R.id.btnMyBookings)
        val btnStaff = findViewById<Button>(R.id.btnStaffDashboard)
        val btnInventory = findViewById<Button>(R.id.btnInventory)
        val btnAnalytics = findViewById<Button>(R.id.btnAnalytics)
        val btnReviews = findViewById<Button>(R.id.btnViewReviews)
        val btnTeamChat = findViewById<Button>(R.id.btnTeamChat)

        btnStaff.text = when (role) {
            "Admin" -> "MANAGE BOOKINGS"
            "Owner" -> "SALON OVERVIEW"
            "Stylist" -> "MY WORKLIST"
            else -> "DASHBOARD"
        }

        when (role) {
            "Admin" -> {
                btnBook.visibility = View.VISIBLE
                btnMyBookings.visibility = View.VISIBLE
                btnStaff.visibility = View.VISIBLE
                btnInventory.visibility = View.VISIBLE
                btnAnalytics.visibility = View.GONE
                btnReviews.visibility = View.VISIBLE
                btnTeamChat.visibility = View.VISIBLE
            }
            "Owner" -> {
                btnBook.visibility = View.VISIBLE
                btnMyBookings.visibility = View.VISIBLE
                btnStaff.visibility = View.VISIBLE
                btnInventory.visibility = View.VISIBLE
                btnAnalytics.visibility = View.VISIBLE
                btnReviews.visibility = View.VISIBLE
                btnTeamChat.visibility = View.VISIBLE
            }
            "Stylist" -> {
                btnBook.visibility = View.GONE
                btnMyBookings.visibility = View.GONE
                btnStaff.visibility = View.VISIBLE
                btnInventory.visibility = View.GONE
                btnReviews.visibility = View.GONE
                btnTeamChat.visibility = View.VISIBLE
            }
            else -> {
                btnBook.visibility = View.VISIBLE
                btnMyBookings.visibility = View.VISIBLE
                btnStaff.visibility = View.GONE
                btnInventory.visibility = View.GONE
                btnReviews.visibility = View.GONE
                btnTeamChat.visibility = View.GONE
            }
        }

        btnBook.setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }

        findViewById<Button>(R.id.btnServices).setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }

        btnMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        btnStaff.setOnClickListener {
            startActivity(Intent(this, StaffActivity::class.java))
        }

        btnInventory.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        btnAnalytics.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }

        btnReviews.setOnClickListener {
            startActivity(Intent(this, ReviewsActivity::class.java))
        }

        btnTeamChat.setOnClickListener {
            startActivity(Intent(this, CommunicationActivity::class.java))
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnHelpSupport).setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        setupMessageObserver()
        checkFeedbackPrompt()
    }

    private var lastMessageTime = System.currentTimeMillis()

    private fun setupMessageObserver() {
        lifecycleScope.launch {
            AppDatabase.getDatabase(this@MainActivity).bookingDao().getLatestMessage().collect { message ->
                if (message != null && message.timestamp > lastMessageTime && message.senderId != SessionManager(this@MainActivity).getUserId()) {
                    NotificationHelper.showChatNotification(this@MainActivity, message.senderName, message.content)
                    lastMessageTime = message.timestamp
                }
            }
        }
    }

    private fun checkFeedbackPrompt() {
        val session = SessionManager(this)
        val userId = session.getUserId()
        if (userId == -1) return

        val prefs = getSharedPreferences("GlamourPrefs", MODE_PRIVATE)
        val appUsageCount = prefs.getInt("USAGE_COUNT", 0) + 1
        prefs.edit { putInt("USAGE_COUNT", appUsageCount) }

        // Prompt feedback on the 3rd app launch
        if (appUsageCount == 3) {
            FeedbackDialog.show(this, userId)
        }
    }
}
