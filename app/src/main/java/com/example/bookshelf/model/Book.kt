package com.example.bookshelf.model

data class Book (
    var imageURL : String,
    var title: String,
    var author: String,
    var genre: String,
    var description: String,
    var isFav : Boolean,
    var isBorrowed : Boolean,
    var borrowedTo : String?,
    var shelf : Long?,
    var row : Long?
) {
    fun bookToHashMapOf() : HashMap<String, Any?> {
        return hashMapOf(
            "imageURL" to this.imageURL,
            "title" to this.title,
            "author" to this.author,
            "genre" to this.genre,
            "description" to this.description,
            "isFav" to this.isFav,
            "isBorrowed" to this.isBorrowed,
            "borrowedTo" to this.borrowedTo,
            "shelf" to this.shelf,
            "row" to this.row
        )
    }
}