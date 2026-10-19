package com.example.glamourcuts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MyBookingsActivity : AppCompatActivity() {
    private lateinit var adapter: BookingAdapter
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        db = AppDatabase.getDatabase(this)
        session = SessionManager(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rvMyBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadBookings()
    }

    private fun loadBookings() {
        lifecycleScope.launch {
            val userId = session.getUserId()
            val bookings = db.bookingDao().getBookingsForUser(userId)
            adapter = BookingAdapter(
                bookings = bookings,
                onCancel = { booking -> cancelBooking(booking) },
                userRole = "Client",
                onSendSMS = { _ ->
                    FeedbackDialog.show(this@MyBookingsActivity, userId)
                },
            )
            findViewById<RecyclerView>(R.id.rvMyBookings).adapter = adapter
        }
    }

    private fun cancelBooking(booking: Booking) {
        lifecycleScope.launch {
            db.bookingDao().updateBookingStatus(booking.id, "Cancelled")
            loadBookings()
        }
    }
}
