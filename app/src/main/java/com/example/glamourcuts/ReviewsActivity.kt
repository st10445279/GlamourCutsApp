package com.example.glamourcuts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ReviewsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        val rv = findViewById<RecyclerView>(R.id.rvReviews)
        rv.layoutManager = LinearLayoutManager(this)

        loadReviews()
    }

    private fun loadReviews() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ReviewsActivity)
            val reviews = db.bookingDao().getAllFeedback()
            findViewById<RecyclerView>(R.id.rvReviews).adapter = ReviewAdapter(reviews)
        }
    }
}
