package com.example.bookshelf.database

import android.util.Log
import com.example.bookshelf.interfaces.GetUserInterface
import com.example.bookshelf.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object DatabaseManager {
    lateinit var user : User

    fun getUserData(email: String, getUserInterface: GetUserInterface? = null) {
        Firebase.firestore.collection("users").document(email)
            .get()
            .addOnSuccessListener { document ->
                user = mapToUser(document.data!!)
                getUserInterface?.getUser(user)
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    private fun mapToUser(map: MutableMap<String, Any>) : User {
        return User(
            userName = map["userName"] as String,
            emailAddress = map["emailAddress"] as String,
        )
    }
}