package com.example.bookshelf.interfaces

import com.example.bookshelf.model.Genre

interface GetGenreInterface {
    fun getGenre(genres: MutableList<String>)
}
