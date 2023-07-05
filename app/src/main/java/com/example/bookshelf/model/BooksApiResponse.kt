package com.example.bookshelf.model

data class BooksApiResponse(
    val totalItems : Int,
    val items : MutableList<BooksApiBookData>?
)
