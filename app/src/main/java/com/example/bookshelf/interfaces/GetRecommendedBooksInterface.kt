package com.example.bookshelf.interfaces

import com.example.bookshelf.model.Book

interface GetRecommendedBooksInterface {
    fun onSuccess(recommendedBooks: List<Book>)
    fun onError(reason: String?)
}