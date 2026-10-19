package com.example.glamourcuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private var messages: List<Message>, private val currentUserId: Int) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = sdf.format(Date(msg.timestamp))

        if (holder is SentViewHolder) {
            holder.tvContent.text = msg.content
            holder.tvTime.text = timeStr
        } else if (holder is ReceivedViewHolder) {
            holder.tvSender.text = "${msg.senderName} (${msg.senderRole})"
            holder.tvContent.text = msg.content
            holder.tvTime.text = timeStr
        }
    }

    override fun getItemCount() = messages.size

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvMsgContent)
        val tvTime: TextView = view.findViewById(R.id.tvMsgTime)
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSender: TextView = view.findViewById(R.id.tvMsgSender)
        val tvContent: TextView = view.findViewById(R.id.tvMsgContent)
        val tvTime: TextView = view.findViewById(R.id.tvMsgTime)
    }

    fun updateData(newMessages: List<Message>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }
}
