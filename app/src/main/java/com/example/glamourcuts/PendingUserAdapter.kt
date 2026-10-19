package com.example.glamourcuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PendingUserAdapter(
    private var users: List<User>,
    private val onConfirm: (User) -> Unit
) : RecyclerView.Adapter<PendingUserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPendingUsername)
        val tvRole: TextView = view.findViewById(R.id.tvPendingRole)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirmUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.username
        holder.tvRole.text = user.role
        holder.btnConfirm.setOnClickListener { onConfirm(user) }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
