package com.example.bookshelf.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.bookshelf.R
import com.example.bookshelf.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordFragment : Fragment() {
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.forgot_password_screen, container, false)

        val emailEditText : TextInputEditText = view.findViewById(R.id.textInputEditTextEmail)

        val buttonSend : Button = view.findViewById(R.id.buttonSend)
        buttonSend.setOnClickListener {
            if (emailEditText.text.isNullOrBlank() || emailEditText.text.isNullOrEmpty())
            {
                Toast.makeText(requireActivity(), "Empty e-mail field!",
                    Toast.LENGTH_LONG).show()
            }
            else {
                if (Utils.isValidEmail(emailEditText.text.toString())) {
                    forgotPassword(emailEditText.text.toString())
                }
                else {
                    Toast.makeText(requireActivity(), "Incorrect or invalid e-mail!",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        return view


    }

    fun forgotPassword(email : String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireActivity(), "Ellenőrizze az e-mailjeit!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireActivity(), "Próbálja meg újra!", Toast.LENGTH_LONG).show()
            }
        }
    }

}