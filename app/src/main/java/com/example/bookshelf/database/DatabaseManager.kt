package com.example.bookshelf.database

import android.graphics.Bitmap
import android.util.Log
import com.example.bookshelf.interfaces.GetBooksInterface
import com.example.bookshelf.interfaces.GetGenreInterface
import com.example.bookshelf.interfaces.GetUserInterface
import com.example.bookshelf.interfaces.UpdateBookDataInterface
import com.example.bookshelf.interfaces.UploadBookCoverImageInterface
import com.example.bookshelf.model.Book
import com.example.bookshelf.model.Genre
import com.example.bookshelf.model.User
import com.example.bookshelf.utils.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

object DatabaseManager {
    lateinit var user : User
    var book : Book? = null
    lateinit var genre : Genre
    lateinit var genres : MutableList<String>
    lateinit var allbooks : MutableList<Book>
    lateinit var favoritebooks : MutableList<Book>

    fun getUserData(email: String, getUserInterface: GetUserInterface? = null) {
        Firebase.firestore.collection("users").document(email)
            .get()
            .addOnSuccessListener { document ->
                Log.d("LOGIN", "${document.data}")
                user = mapToUser(document.data!!)
                getUserInterface?.getUser(user)
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    fun setUserdata(user: User) {
        Firebase.firestore.collection("users").document(user.emailAddress)
            .set(user.userToHashMapOf())
            .addOnSuccessListener { documentReference ->
                Log.w("USER", "User data saved")
            }
            .addOnFailureListener { e ->
                Log.w("USER", "Error adding document", e)
            }
    }

    private fun mapToUser(map: MutableMap<String, Any>) : User {
        return User(
            userName = map["userName"] as String,
            emailAddress = map["emailAddress"] as String,
        )
    }

    fun getSelectedBookData() : Book? {
        if (book != null) {
            return book
        }

        return null
    }

    fun setSelectedBookData(b: Book) {
        book = b
    }

    fun deleteSelectedBookData() {
        book = null
    }

    fun getAllBooksData(email: String, getBooksInterface : GetBooksInterface? = null) {
        Firebase.firestore.collection("books").document(email)
            .collection("allbooks")
            .get()
            .addOnSuccessListener { documents ->
                allbooks = ArrayList()
                for (doc in documents.documents) {
                    val b : Book = mapToBook(doc.data!!)
                    allbooks.add(b)
                }

                getBooksInterface?.getBooks(allbooks)
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    fun getFavoriteBooksData(email: String, getBooksInterface : GetBooksInterface? = null) {
        Firebase.firestore.collection("books").document(email)
            .collection("favoritebooks")
            .get()
            .addOnSuccessListener { documents ->
                favoritebooks = ArrayList()
                for (doc in documents.documents) {
                    val b : Book = mapToBook(doc.data!!)
                    favoritebooks.add(b)
                }

                getBooksInterface?.getBooks(favoritebooks)
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    fun updateBookData(email: String, b: Book, updateBookDataInterface: UpdateBookDataInterface? = null) {
        Firebase.firestore.collection("books").document(email)
            .collection("allbooks")
            .document(b.imageURL)
            .set(b.bookToHashMapOf())
            .addOnSuccessListener { _ ->
                updateBookDataInterface?.onSuccess()
            }
            .addOnFailureListener { e ->
                updateBookDataInterface?.onError(e.message)
            }
    }

    fun uploadBookCoverImage(bookCoverImage: Bitmap, imageUrl: String?, uploadBookCoverImageInterface: UploadBookCoverImageInterface? = null) {
        val randomString: String = imageUrl ?: Utils.generateRandUUID()

        val storageRef = Firebase.storage.reference.child("images/$randomString.jpg")

        val baos = ByteArrayOutputStream()
        bookCoverImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data).addOnSuccessListener {
            uploadBookCoverImageInterface?.onSuccess(randomString)
        }.addOnFailureListener {
            uploadBookCoverImageInterface?.onError(it.message)
        }
    }

    private fun mapToBook(map: MutableMap<String, Any>) : Book {
        return Book(
            imageURL = map["imageURL"] as String,
            title = map["title"] as String,
            author = map["author"] as String,
            genre = map["genre"] as String,
            description = map["description"] as String,
            isFav = map["isFav"] as Boolean
        )
    }

    fun getGenresData(getGenreInterface: GetGenreInterface? = null) {
        if (!this::genres.isInitialized) {
            Firebase.firestore.collection("genres")
                .get()
                .addOnSuccessListener { document ->
                    genres = ArrayList()
                    for (doc in document.documents) {
                        genre = mapToGenre(doc.data!!)
                        genres.add(genre.genre)
                    }
                    getGenreInterface?.getGenre(genres)
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "Error getting documents: ", exception)
                }
        }
        else{
            getGenreInterface?.getGenre(genres)
        }

    }

    private fun mapToGenre(map: MutableMap<String, Any>) : Genre {
        return Genre(
            genre = map["genre"].toString(),
        )
    }
}