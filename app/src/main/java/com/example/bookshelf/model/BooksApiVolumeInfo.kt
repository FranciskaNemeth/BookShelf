package com.example.bookshelf.model

data class BooksApiVolumeInfo (
    val title : String?,
    val authors : MutableList<String>?,
    val description: String?,
    val categories : MutableList<String>?,
    val imageLinks : BooksApiImageLinks?
    )
