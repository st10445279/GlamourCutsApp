package com.example.glamourcuts

data class HelpArticle(
    val id: Int,
    val category: String,
    val title: String,
    val content: String,
    var isExpanded: Boolean = false
)
