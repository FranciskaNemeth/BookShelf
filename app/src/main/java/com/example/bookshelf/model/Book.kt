package com.example.bookshelf.model

data class Book (
    var imageURL : String,
    var title: String,
    var author: String,
    var genre: String,
    var description: String,
    var isFav : Boolean
) {
    fun bookToHashMapOf() : HashMap<String, Any?> {
        return hashMapOf(
            "imageURL" to this.imageURL,
            "title" to this.title,
            "author" to this.author,
            "genre" to this.genre,
            "description" to this.description,
            "isFav" to this.isFav
        )
    }
}