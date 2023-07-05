package com.example.bookshelf.api

import com.example.bookshelf.model.BooksApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("volumes")
    suspend fun getBooks(@Query("q") queryString : String, @Query("key") key : String) : BooksApiResponse
}