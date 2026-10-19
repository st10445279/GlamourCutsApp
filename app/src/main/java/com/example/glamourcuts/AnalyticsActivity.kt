package com.example.glamourcuts

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AnalyticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        loadAnalytics()
    }

    private fun loadAnalytics() {
        lifecycleScope.launch {
            val bookings = AppDatabase.getDatabase(this@AnalyticsActivity).bookingDao().getAllBookings()
            
            val totalBookings = bookings.size
            var totalRevenue = 0
            
            for (booking in bookings) {
                if (booking.status == "Completed") {
                    // Extract numeric value from price string (e.g., "R350" -> 350)
                    val priceStr = booking.pricePaid.ifEmpty { 
                        // Fallback to service price if pricePaid is empty
                        "0"
                    }.replace("R", "").replace(",", "").trim()
                    
                    totalRevenue += priceStr.toIntOrNull() ?: 0
                }
            }

            findViewById<TextView>(R.id.tvTotalBookings).text = getString(R.string.total_bookings_label, totalBookings)
            findViewById<TextView>(R.id.tvTotalRevenue).text = getString(R.string.total_revenue_label, totalRevenue)
            
            val commissions = (totalRevenue * 0.20).toInt()
            findViewById<TextView>(R.id.tvTotalCommissions).text = getString(R.string.total_commissions_label, commissions)
        }
    }
}
