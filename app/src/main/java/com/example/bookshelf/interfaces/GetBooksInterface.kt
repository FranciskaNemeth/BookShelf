package com.example.bookshelf.interfaces

import com.example.bookshelf.model.Book

interface GetBooksInterface {
    fun getBooks(books: MutableList<Book>)
}