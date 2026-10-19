package com.example.glamourcuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServiceAdapter(
    private val services: List<Service>,
    private val onBookClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivServiceImage)
        val tvIcon: TextView = view.findViewById(R.id.tvServiceIcon)
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvPrice: TextView = view.findViewById(R.id.tvServicePrice)
        val tvDuration: TextView = view.findViewById(R.id.tvServiceDuration)
        val btnBook: TextView = view.findViewById(R.id.tvBookNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = services[position]
        holder.tvIcon.text = service.description
        holder.tvName.text = service.name
        holder.tvPrice.text = service.price
        holder.tvDuration.text = service.duration

        val imageRes = when (service.name) {
            "Precision Cut" -> R.drawable.precision_cut
            "Colour & Highlights" -> R.drawable.color_highlights
            "Blowout & Style" -> R.drawable.blowout_style
            "Keratin Treatment" -> R.drawable.keratin_treatment
            "Scalp Treatment" -> R.drawable.scalp_treatment
            "Bridal Package" -> R.drawable.bridal_package
            else -> R.drawable.ic_launcher_background
        }
        holder.ivImage.setImageResource(imageRes)
        
        holder.btnBook.setOnClickListener { onBookClick(service) }
        holder.itemView.setOnClickListener { onBookClick(service) }
    }

    override fun getItemCount() = services.size
}
