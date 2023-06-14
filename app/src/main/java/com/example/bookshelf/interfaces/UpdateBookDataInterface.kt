package com.example.bookshelf.interfaces

interface UpdateBookDataInterface {
    fun onSuccess()
    fun onError(reason: String?)
}