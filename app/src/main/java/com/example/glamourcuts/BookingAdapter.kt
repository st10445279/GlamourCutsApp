package com.example.glamourcuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onCancel: (Booking) -> Unit,
    private val userRole: String = "Client",
    private val onStatusUpdate: ((Booking, String) -> Unit)? = null,
    private val onSendSMS: ((Booking) -> Unit)? = null
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvService: TextView = view.findViewById(R.id.tvItemService)
        val tvDateTime: TextView = view.findViewById(R.id.tvItemDateTime)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val btnCancel: Button = view.findViewById(R.id.btnItemCancel)
        val btnUpdate: Button = view.findViewById(R.id.btnItemUpdate)
        val btnRemind: Button = view.findViewById(R.id.btnItemRemind)
        val tvClientName: TextView = view.findViewById(R.id.tvItemClientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        holder.tvService.text = booking.service
        holder.tvDateTime.text = holder.itemView.context.getString(R.string.confirm_date_time, booking.date, booking.time)
        holder.tvStatus.text = holder.itemView.context.getString(R.string.status_label, booking.status)
        holder.tvClientName.text = holder.itemView.context.getString(R.string.client_name_label, "${booking.firstName} ${booking.lastName}")
        
        val isStaffView = userRole != "Client"
        holder.tvClientName.visibility = if (isStaffView) View.VISIBLE else View.GONE

        // Clients can cancel if confirmed
        holder.btnCancel.visibility = if (userRole == "Client" && booking.status == "Confirmed") View.VISIBLE else View.GONE
        
        // Rate button for clients when completed
        if (userRole == "Client" && booking.status == "Completed") {
            holder.btnRemind.visibility = View.VISIBLE
            holder.btnRemind.text = "Leave Review"
            holder.btnRemind.setOnClickListener { onSendSMS?.invoke(booking) }
        }

        // Admin/Stylist/Owner can update status
        if (isStaffView) {
            // SMS Remind is Admin/Owner only (User requested Admin specifically)
            if (userRole == "Admin") {
                holder.btnRemind.visibility = View.VISIBLE
                holder.btnRemind.text = "SMS"
                holder.btnRemind.setOnClickListener { onSendSMS?.invoke(booking) }
            } else if (userRole != "Client") {
                holder.btnRemind.visibility = View.GONE
            }

            // Update Status Logic
            if (userRole == "Admin" || userRole == "Owner") {
                holder.btnUpdate.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnUpdate.setOnClickListener { view ->
                    val popup = PopupMenu(view.context, view)
                    popup.menu.add("Confirmed")
                    popup.menu.add("Rejected")
                    popup.menu.add("Completed")
                    popup.menu.add("Cancelled")
                    popup.setOnMenuItemClickListener { item ->
                        onStatusUpdate?.invoke(booking, item.title.toString())
                        true
                    }
                    popup.show()
                }
            } else if (userRole == "Stylist" && booking.status == "Confirmed") {
                holder.btnUpdate.visibility = View.VISIBLE
                holder.btnUpdate.text = "Complete"
                holder.btnCancel.visibility = View.GONE
                holder.btnUpdate.setOnClickListener {
                    onStatusUpdate?.invoke(booking, "Completed")
                }
            } else {
                holder.btnUpdate.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE
            }
        } else {
            // Client View (already handled above)
            if (booking.status != "Completed") {
                holder.btnRemind.visibility = View.GONE
            }
            holder.btnUpdate.visibility = View.GONE
        }
        
        holder.btnCancel.setOnClickListener { onCancel(booking) }
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<Booking>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }
}
