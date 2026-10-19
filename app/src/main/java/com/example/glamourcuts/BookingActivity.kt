package com.example.glamourcuts

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        loadServices()
        autoFillUserInfo()

        val times = arrayOf(
            getString(R.string.select_time),
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
        )
        val timeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, times)
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerTime).adapter = timeAdapter

        val btnPickDate = findViewById<Button>(R.id.btnPickDate)
        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                btnPickDate.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<Button>(R.id.btnConfirmBooking).setOnClickListener {
            submitBooking()
        }
    }

    private fun autoFillUserInfo() {
        lifecycleScope.launch {
            val session = SessionManager(this@BookingActivity)
            val userId = session.getUserId()
            if (userId != -1) {
                val user = AppDatabase.getDatabase(this@BookingActivity).bookingDao().getUserById(userId)
                user?.let {
                    findViewById<EditText>(R.id.etFname).setText(it.username)
                    findViewById<EditText>(R.id.etPhone).setText(it.phone)
                }
            }
        }
    }

    private fun loadServices() {
        lifecycleScope.launch {
            val dbServices = AppDatabase.getDatabase(this@BookingActivity).bookingDao().getAllServices()
            val serviceNames = mutableListOf(getString(R.string.select_service))
            serviceNames.addAll(dbServices.map { it.name })
            
            val adapter = ArrayAdapter(this@BookingActivity, android.R.layout.simple_spinner_item, serviceNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinner = findViewById<Spinner>(R.id.spinnerService)
            spinner.adapter = adapter

            // Pre-select if coming from Services screen
            val preSelected = intent.getStringExtra("PRE_SELECTED_SERVICE")
            if (preSelected != null) {
                val pos = serviceNames.indexOf(preSelected)
                if (pos != -1) spinner.setSelection(pos)
            }
        }
    }

    private fun submitBooking() {
        val firstName = findViewById<EditText>(R.id.etFname).text.toString().trim()
        val lastName  = findViewById<EditText>(R.id.etLname).text.toString().trim()
        val phone     = findViewById<EditText>(R.id.etPhone).text.toString().trim()
        val email     = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val service   = findViewById<Spinner>(R.id.spinnerService).selectedItem?.toString() ?: ""
        val time      = findViewById<Spinner>(R.id.spinnerTime).selectedItem?.toString() ?: ""

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.length < 5) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (service.isEmpty() || service == getString(R.string.select_service)) {
            Toast.makeText(this, "Please select a service", Toast.LENGTH_SHORT).show()
            return
        }
        if (time.isEmpty() || time == getString(R.string.select_time)) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Conflict detection and Save to local database
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getDatabase(this@BookingActivity).bookingDao()
                val existing = dao.getBookingAtTime(selectedDate, time)
                
                if (existing != null) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@BookingActivity, "This slot is already booked. Please choose another time.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                saveToLocalDatabase(firstName, lastName, phone, email, service, selectedDate, time)

                launch(Dispatchers.Main) {
                    // 2. Show successful message and navigate to confirmation
                    Toast.makeText(this@BookingActivity, "Booking confirmed and saved!", Toast.LENGTH_SHORT).show()
                    
                    // Show reminder immediately for demo purposes
                    NotificationHelper.showReminder(this@BookingActivity, service, time)

                    val intent = Intent(this@BookingActivity, ConfirmationActivity::class.java).apply {
                        putExtra("name", "$firstName $lastName")
                        putExtra("service", service)
                        putExtra("date", selectedDate)
                        putExtra("time", time)
                    }
                    startActivity(intent)
                    finish() // Close BookingActivity
                }

                // 3. Attempt to sync with server in the background
                syncWithServer(firstName, lastName, phone, email, service, selectedDate, time)
            } catch (e: Exception) {
                Log.e("BookingActivity", "Error submitting booking", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@BookingActivity, "Failed to save booking: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun syncWithServer(
        firstName: String, lastName: String, phone: String, 
        email: String, service: String, date: String, time: String
    ) {
        val url = "http://10.0.2.2/glamour-cuts/booking_process.php"
        val queue = Volley.newRequestQueue(applicationContext)

        val request = object : StringRequest(
            Method.POST, url,
            { _ -> Log.d("BookingActivity", "Server sync successful") },
            { _ -> Log.e("BookingActivity", "Server sync failed") }
        ) {
            override fun getParams(): Map<String, String> = mapOf(
                "fname"   to firstName,
                "lname"   to lastName,
                "phone"   to phone,
                "email"   to email,
                "service" to service,
                "date"    to date,
                "time"    to time
            )
        }
        queue.add(request)
    }

    private suspend fun saveToLocalDatabase(
        fname: String,
        lname: String,
        phone: String,
        email: String,
        service: String,
        date: String,
        time: String
    ) {
        val session = SessionManager(this)
        val booking = Booking(
            userId = session.getUserId(),
            firstName = fname,
            lastName = lname,
            phone = phone,
            email = email,
            service = service,
            date = date,
            time = time
        )
        AppDatabase.getDatabase(this@BookingActivity).bookingDao().insert(booking)
    }
}
