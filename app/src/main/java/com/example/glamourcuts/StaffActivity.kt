package com.example.glamourcuts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class StaffActivity : AppCompatActivity() {
    private lateinit var adapter: BookingAdapter
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff)

        db = AppDatabase.getDatabase(this)
        session = SessionManager(this)
        
        val role = session.getUserRole() ?: "Staff"
        val dashboardName = when (role) {
            "Admin" -> "Admin Control Center"
            "Owner" -> "Owner's Overview"
            "Stylist" -> "Stylist Worklist"
            else -> "Staff Dashboard"
        }
        findViewById<TextView>(R.id.tvStaffTitle).text = dashboardName

        val recyclerView = findViewById<RecyclerView>(R.id.rvStaffBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnStaffCommunication).setOnClickListener {
            startActivity(Intent(this, CommunicationActivity::class.java))
        }

        val btnApprove = findViewById<Button>(R.id.btnApproveUsers)
        if (role == "Admin" || role == "Owner") {
            btnApprove.visibility = android.view.View.VISIBLE
            btnApprove.setOnClickListener {
                startActivity(Intent(this, AdminUserApprovalActivity::class.java))
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        }

        loadAllBookings()
        setupMessageObserver()
    }

    private var lastMessageTime = System.currentTimeMillis()

    private fun setupMessageObserver() {
        lifecycleScope.launch {
            db.bookingDao().getLatestMessage().collect { message ->
                if (message != null && message.timestamp > lastMessageTime && message.senderId != session.getUserId()) {
                    NotificationHelper.showChatNotification(this@StaffActivity, message.senderName, message.content)
                    lastMessageTime = message.timestamp
                }
            }
        }
    }

    private fun loadAllBookings() {
        lifecycleScope.launch {
            val role = session.getUserRole() ?: "Client"
            val allBookings = db.bookingDao().getAllBookings()
            
            // Filter based on requirements
            val filteredBookings = when (role) {
                "Stylist" -> allBookings.filter { it.status == "Confirmed" }
                "Owner" -> allBookings // Owner sees all to track new (Waiting) and history
                "Admin" -> allBookings // Admin sees everything to manage them
                else -> emptyList()
            }

            adapter = BookingAdapter(
                filteredBookings, 
                onCancel = { booking -> updateStatus(booking, "Cancelled") },
                userRole = role,
                onStatusUpdate = { booking, status -> updateStatus(booking, status) },
                onSendSMS = { booking -> sendSMS(booking) }
            )
            findViewById<RecyclerView>(R.id.rvStaffBookings).adapter = adapter
        }
    }

    private fun sendSMS(booking: Booking) {
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                this.getSystemService(SmsManager::class.java)!!
            } else {
                SmsManager.getDefault()
            }
            
            val message = "Glamour Cuts Reminder: Your appointment for ${booking.service} is on ${booking.date} at ${booking.time}."
            
            if (booking.phone.isNotEmpty()) {
                smsManager.sendTextMessage(booking.phone, null, message, null, null)
                Toast.makeText(this, "SMS Sent to ${booking.firstName}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Client has no phone number recorded", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "SMS Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateStatus(booking: Booking, status: String) {
        lifecycleScope.launch {
            if (status == "Completed") {
                val services = db.bookingDao().getAllServices()
                val servicePrice = services.find { it.name == booking.service }?.price ?: "0"
                db.bookingDao().updatePricePaid(booking.id, servicePrice)
            }
            db.bookingDao().updateBookingStatus(booking.id, status)
            loadAllBookings()
        }
    }
}
