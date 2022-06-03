package com.example.bookshelf.model

import com.example.bookshelf.utils.Utils

class User(userName: String, emailAddress: String) {

    var userName : String = userName
        set(value) {
            if (value.isNotEmpty()) {
                field = value
            }
            else {
                throw Exception("Wrong userName!")
            }
        }

    var emailAddress : String = emailAddress
        set(value) {
            if (value.isNotEmpty() && Utils.isValidEmail(value)) {
                field = value
            }
            else {
                throw Exception("Wrong emailAddress!")
            }
        }


}