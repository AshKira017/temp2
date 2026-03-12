package com.mylibrary.app.data

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String = "",
    val chapters: MutableList<Chapter> = mutableListOf()
)
