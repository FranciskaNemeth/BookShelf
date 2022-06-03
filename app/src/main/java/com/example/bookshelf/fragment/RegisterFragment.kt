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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.register_screen, container, false)

        val emailEditText : TextInputEditText = view.findViewById(R.id.textInputEditTextEmail)
        val pwdEditText : TextInputEditText = view.findViewById(R.id.textInputEditTextPassword)
        val userNameEditText : TextInputEditText = view.findViewById(R.id.textInputEditTextUserName)

        val buttonRegister : Button = view.findViewById(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            if (emailEditText.text.isNullOrBlank() || emailEditText.text.isNullOrEmpty() ||
                pwdEditText.text.isNullOrBlank() || pwdEditText.text.isNullOrEmpty() ||
                userNameEditText.text.isNullOrBlank() || userNameEditText.text.isNullOrEmpty())
            {
                Toast.makeText(requireActivity(), "Empty user name, e-mail or password field!",
                    Toast.LENGTH_LONG).show()
            }
            else {
                if (Utils.isValidEmail(emailEditText.text.toString())) {
                    register(emailEditText.text.toString(), pwdEditText.text.toString())
                }
                else {
                    Toast.makeText(requireActivity(), "Incorrect e-mail or password!",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        return view
    }

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(OnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        requireActivity(), "Successfully Singed Up!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireActivity(), "Singed Up Failed!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}