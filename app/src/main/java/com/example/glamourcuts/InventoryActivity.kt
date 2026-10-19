package com.example.glamourcuts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class InventoryActivity : AppCompatActivity() {
    private lateinit var adapter: InventoryAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        db = AppDatabase.getDatabase(this)
        val session = SessionManager(this)
        val role = session.getUserRole()
        
        val recyclerView = findViewById<RecyclerView>(R.id.rvInventory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddItem)
        if (role == "Owner" || role == "Admin") {
            btnAdd.visibility = View.VISIBLE
            btnAdd.setOnClickListener {
                showAddItemDialog()
            }
        } else {
            btnAdd.visibility = View.GONE
        }

        loadInventory(role)
    }

    private fun loadInventory(role: String?) {
        lifecycleScope.launch {
            val items = db.bookingDao().getAllInventory()
            adapter = InventoryAdapter(items, role,
                onRestock = { item -> restock(item) },
                onDelete = { item -> deleteItem(item) }
            )
            findViewById<RecyclerView>(R.id.rvInventory).adapter = adapter
        }
    }

    private fun showAddItemDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_inventory, null)
        val etName = view.findViewById<EditText>(R.id.etItemName)
        val etQuantity = view.findViewById<EditText>(R.id.etItemQuantity)
        val etThreshold = view.findViewById<EditText>(R.id.etItemThreshold)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitItem)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val qtyStr = etQuantity.text.toString().trim()
            val thresholdStr = etThreshold.text.toString().trim()

            if (name.isEmpty() || qtyStr.isEmpty() || thresholdStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val qty = qtyStr.toIntOrNull()
            val threshold = thresholdStr.toIntOrNull()

            if (qty == null || qty < 0) {
                Toast.makeText(this, "Please enter a valid positive quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (threshold == null || threshold < 0) {
                Toast.makeText(this, "Please enter a valid threshold", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    db.bookingDao().insertInventoryItem(InventoryItem(name = name, quantity = qty, lowStockThreshold = threshold))
                    val role = SessionManager(this@InventoryActivity).getUserRole()
                    loadInventory(role)
                    dialog.dismiss()
                } catch (e: Exception) {
                    Toast.makeText(this@InventoryActivity, "Error saving item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun restock(item: InventoryItem) {
        lifecycleScope.launch {
            val updatedItem = item.copy(quantity = item.quantity + 10)
            db.bookingDao().updateInventoryItem(updatedItem)
            val role = SessionManager(this@InventoryActivity).getUserRole()
            loadInventory(role)
        }
    }

    private fun deleteItem(item: InventoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete ${item.name}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    db.bookingDao().deleteInventoryItem(item)
                    val role = SessionManager(this@InventoryActivity).getUserRole()
                    loadInventory(role)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
