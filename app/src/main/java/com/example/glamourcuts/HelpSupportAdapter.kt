package com.example.glamourcuts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HelpSupportAdapter(
    private var articles: List<HelpArticle>
) : RecyclerView.Adapter<HelpSupportAdapter.HelpViewHolder>() {

    class HelpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvArticleCategory)
        val tvTitle: TextView = itemView.findViewById(R.id.tvArticleTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvArticleContent)
        val ivExpandIcon: ImageView = itemView.findViewById(R.id.ivExpandIcon)
        val llHeader: View = itemView.findViewById(R.id.llArticleHeader)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_article, parent, false)
        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val article = articles[position]
        holder.tvCategory.text = article.category
        holder.tvTitle.text = article.title
        holder.tvContent.text = article.content

        holder.tvContent.visibility = if (article.isExpanded) View.VISIBLE else View.GONE
        holder.ivExpandIcon.rotation = if (article.isExpanded) 180f else 0f

        holder.llHeader.setOnClickListener {
            article.isExpanded = !article.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateData(newArticles: List<HelpArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
