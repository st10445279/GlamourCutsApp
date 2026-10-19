package com.example.glamourcuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(
    private var items: List<InventoryItem>,
    private val userRole: String?,
    private val onRestock: (InventoryItem) -> Unit,
    private val onDelete: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvStock: TextView = view.findViewById(R.id.tvItemStock)
        val tvAlert: TextView = view.findViewById(R.id.tvLowStockAlert)
        val btnRestock: android.widget.Button = view.findViewById(R.id.btnRestock)
        val btnDelete: android.widget.ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvStock.text = holder.itemView.context.getString(R.string.stock_label, item.quantity)
        
        if (item.quantity <= item.lowStockThreshold) {
            holder.tvAlert.visibility = View.VISIBLE
        } else {
            holder.tvAlert.visibility = View.GONE
        }

        holder.btnRestock.setOnClickListener { onRestock(item) }

        if (userRole == "Owner" || userRole == "Admin") {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDelete(item) }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<InventoryItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
