package com.example.bookshelf.interfaces

interface UploadBookCoverImageInterface {
    fun onSuccess(imageUrl: String)
    fun onError(reason: String?)
}