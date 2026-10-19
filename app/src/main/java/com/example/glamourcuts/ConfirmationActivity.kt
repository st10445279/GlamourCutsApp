package com.example.glamourcuts

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class ConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val name    = intent.getStringExtra("name")    ?: "Guest"
        val service = intent.getStringExtra("service") ?: "Service"
        val date    = intent.getStringExtra("date")    ?: ""
        val time    = intent.getStringExtra("time")    ?: ""

        findViewById<TextView>(R.id.tvConfirmName).text = name
        findViewById<TextView>(R.id.tvConfirmService).text = service
        findViewById<TextView>(R.id.tvConfirmDateTime).text = getString(R.string.confirm_date_time, date, time)

        findViewById<Button>(R.id.btnAddToCalendar).setOnClickListener {
            addToCalendar(service, date, time)
        }

        findViewById<Button>(R.id.btnShare).setOnClickListener {
            shareBooking(service, date, time)
        }

        findViewById<Button>(R.id.btnViewBookings).setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnDone).setOnClickListener {
            finish()
        }
    }

    private fun shareBooking(service: String, date: String, time: String) {
        try {
            val shareText = getString(R.string.share_text, service, date, time)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Could not open share dialog: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun addToCalendar(service: String, date: String, time: String) {
        try {
            val dateTimeString = "$date $time"
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val startTime = try {
                format.parse(dateTimeString)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
            val endTime = startTime + 3600000 // 1 hour later

            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "Haircut: $service")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Appointment at Glamour Cuts")
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)

            startActivity(intent)
        } catch (_: Exception) {
            android.widget.Toast.makeText(this, "No calendar app found to handle this event", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
