package com.example.glamourcuts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup

class HelpSupportActivity : AppCompatActivity() {

    private lateinit var adapter: HelpSupportAdapter
    private lateinit var allArticles: List<HelpArticle>
    private var selectedCategory: String = "All"
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        val sessionManager = SessionManager(this)
        val userRole = sessionManager.getUserRole()
        val userId = sessionManager.getUserId()

        allArticles = buildArticlesList()

        val rvArticles = findViewById<RecyclerView>(R.id.rvHelpArticles)
        rvArticles.layoutManager = LinearLayoutManager(this)
        adapter = HelpSupportAdapter(allArticles)
        rvArticles.adapter = adapter

        val etSearch = findViewById<EditText>(R.id.etHelpSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterArticles()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategories)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedCategory = when {
                checkedIds.contains(R.id.chipCategoryGettingStarted) -> "Getting Started"
                checkedIds.contains(R.id.chipCategoryBookings) -> "Bookings"
                checkedIds.contains(R.id.chipCategoryServices) -> "Services"
                checkedIds.contains(R.id.chipCategoryAccount) -> "Account & Security"
                checkedIds.contains(R.id.chipCategoryStaff) -> "Staff & Management"
                else -> "All"
            }
            filterArticles()
        }

        // Support Ticket Submission
        val etSubject = findViewById<EditText>(R.id.etSupportSubject)
        val etMessage = findViewById<EditText>(R.id.etSupportMessage)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitSupportTicket)

        btnSubmit.setOnClickListener {
            val subject = etSubject.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (subject.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            etSubject.text.clear()
            etMessage.text.clear()
            Toast.makeText(this, getString(R.string.ticket_submitted), Toast.LENGTH_LONG).show()
        }

        // Quick Contacts
        findViewById<Button>(R.id.btnEmailSupport).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@glamourcuts.com")
                putExtra(Intent.EXTRA_SUBJECT, "GlamourCuts Support Inquiry - User #$userId")
            }
            try {
                startActivity(Intent.createChooser(emailIntent, "Send Email Support"))
            } catch (_: Exception) {
                Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnCallSupport).setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:0123456789")
            }
            try {
                startActivity(callIntent)
            } catch (_: Exception) {
                Toast.makeText(this, "Unable to initiate phone call", Toast.LENGTH_SHORT).show()
            }
        }

        // Diagnostics
        val tvDiagnostics = findViewById<TextView>(R.id.tvDiagnosticsInfo)
        tvDiagnostics.text = "App Version: 1.0.0 | Role: $userRole | User ID: ${if (userId != -1) userId else "Guest"} | DB: Active"

        findViewById<Button>(R.id.btnHelpBack).setOnClickListener {
            finish()
        }
    }

    private fun filterArticles() {
        val filtered = allArticles.filter { article ->
            val matchesCategory = (selectedCategory == "All" || article.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isEmpty() ||
                    article.title.contains(searchQuery, ignoreCase = true) ||
                    article.content.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        adapter.updateData(filtered)
    }

    private fun buildArticlesList(): List<HelpArticle> {
        return listOf(
            HelpArticle(
                id = 1,
                category = "Getting Started",
                title = "How do I create an account and select my role?",
                content = "1. Open GlamourCuts and tap 'Don't have an account? Register here' on the Login screen.\n2. Choose your role: Client or Stylist.\n3. Enter your username, valid phone number, and secure password.\n4. Clients gain immediate access upon registration. Stylist registrations undergo quick Admin verification for approval."
            ),
            HelpArticle(
                id = 2,
                category = "Getting Started",
                title = "How do I change the application language?",
                content = "1. Go to Settings from the main home screen.\n2. Scroll down to the Language Settings section.\n3. Choose your preferred language (English, Afrikaans, isiZulu, Spanish, or French).\n4. Tap 'Apply Language'. The entire app interface will immediately update to your selected language."
            ),
            HelpArticle(
                id = 3,
                category = "Bookings",
                title = "How do I book a appointment?",
                content = "1. From the main screen, tap 'Book Now'.\n2. Select your desired hair treatment or service.\n3. Pick your preferred appointment date and time slot.\n4. Confirm your booking details. Your appointment will be scheduled instantly!"
            ),
            HelpArticle(
                id = 4,
                category = "Bookings",
                title = "How can I view, share, or add bookings to my calendar?",
                content = "1. Tap 'My Bookings' on the home screen to view your active and past appointments.\n2. On the Booking Confirmation screen, tap 'Add to Calendar' to save the appointment to your Google or device calendar.\n3. Tap 'Share Booking' to send confirmation details directly via SMS or messaging apps."
            ),
            HelpArticle(
                id = 5,
                category = "Bookings",
                title = "How do I leave a review or feedback?",
                content = "1. After your appointment is completed, go to 'My Bookings' and tap 'Leave Review'.\n2. Rate your experience (1 to 5 stars) and enter any feedback.\n3. Alternatively, you can view all public client reviews by tapping 'View Reviews' on the home dashboard."
            ),
            HelpArticle(
                id = 6,
                category = "Services",
                title = "How do I check available services, prices, and duration?",
                content = "1. Tap 'View Services' from the main menu.\n2. Browse through precision cuts, colour treatments, blowouts, keratin treatments, scalp care, and bridal packages.\n3. Each service clearly displays pricing (in ZAR / Rand) and expected duration."
            ),
            HelpArticle(
                id = 7,
                category = "Account & Security",
                title = "How do I update my profile details or password?",
                content = "1. Tap 'Settings' from the home screen.\n2. Under Account Settings, you can update your username or phone number.\n3. Type a new password in the password field if you wish to update it (or leave blank to keep your existing password).\n4. Tap 'Save Changes'."
            ),
            HelpArticle(
                id = 8,
                category = "Staff & Management",
                title = "How do Stylists view and manage their appointment worklist?",
                content = "1. Log in as a Stylist.\n2. Tap 'MY WORKLIST' on the home screen.\n3. View upcoming appointments assigned to you, mark appointments as 'Completed', or communicate directly with clients."
            ),
            HelpArticle(
                id = 9,
                category = "Staff & Management",
                title = "How does inventory tracking and stock alerts work?",
                content = "1. Admins and Owners can tap 'Inventory' on the home screen.\n2. View current stock levels for salon products (shampoo, dyes, treatments).\n3. Add new inventory items or tap 'Restock' when stock gets low."
            ),
            HelpArticle(
                id = 10,
                category = "Staff & Management",
                title = "How do Owners access business insights and analytics?",
                content = "1. Log in as Salon Owner.\n2. Tap 'Analytics' from the home screen.\n3. View real-time Total Revenue, total completed bookings, and stylist commission calculations."
            )
        )
    }
}
