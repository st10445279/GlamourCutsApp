package com.example.glamourcuts

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ServicesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val recyclerView = findViewById<RecyclerView>(R.id.rvServices)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            val allServices = AppDatabase.getDatabase(this@ServicesActivity).bookingDao().getAllServices()
            // Filter to show only unique services by name
            val uniqueServices = allServices.distinctBy { it.name }
            
            recyclerView.adapter = ServiceAdapter(uniqueServices) { service ->
                val intent = Intent(this@ServicesActivity, BookingActivity::class.java)
                intent.putExtra("PRE_SELECTED_SERVICE", service.name)
                startActivity(intent)
            }
        }

        findViewById<View>(R.id.btnBookFromServices).setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }
    }
}
