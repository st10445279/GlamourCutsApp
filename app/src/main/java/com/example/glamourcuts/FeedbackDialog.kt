package com.example.glamourcuts

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FeedbackDialog {
    fun show(context: Context, userId: Int) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_feedback, null)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = view.findViewById<EditText>(R.id.etComment)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<Button>(R.id.btnSubmitFeedback).setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val comment = etComment.text.toString().trim()

            if (rating <= 0) {
                Toast.makeText(context, "Please select a star rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AppDatabase.getDatabase(context).bookingDao().insertFeedback(
                        UserFeedback(userId = userId, rating = rating, comment = comment),
                    )
                } catch (_: Exception) {
                    // Ignore background database write errors
                }
            }
            Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
