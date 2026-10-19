package com.example.glamourcuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private val reviews: List<UserFeedback>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ratingBar: RatingBar = view.findViewById(R.id.reviewRating)
        val tvComment: TextView = view.findViewById(R.id.tvReviewComment)
        val tvUser: TextView = view.findViewById(R.id.tvReviewUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        holder.ratingBar.rating = review.rating.toFloat()
        holder.tvComment.text = review.comment
        holder.tvUser.text = holder.itemView.context.getString(R.string.user_id_label, review.userId)
    }

    override fun getItemCount() = reviews.size
}
