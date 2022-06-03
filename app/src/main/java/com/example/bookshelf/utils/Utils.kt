package com.example.bookshelf.utils

import android.content.Context
import android.net.ConnectivityManager

object Utils {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}