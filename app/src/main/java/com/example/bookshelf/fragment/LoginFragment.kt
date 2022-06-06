package com.example.bookshelf.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.bookshelf.MainActivity
import com.example.bookshelf.R
import com.example.bookshelf.database.DatabaseManager
import com.example.bookshelf.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.login_screen, container, false)

        val emailEditText : TextInputEditText = view.findViewById(R.id.textInputEditTextEmail)
        val pwdEditText : TextInputEditText = view.findViewById(R.id.textInputEditTextPassword)

        val buttonLogin : Button = view.findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            if (emailEditText.text.isNullOrBlank() || emailEditText.text.isNullOrEmpty() ||
                pwdEditText.text.isNullOrBlank() || pwdEditText.text.isNullOrEmpty())
            {
                Toast.makeText(requireActivity(), "Empty user name, e-mail or password field!",
                    Toast.LENGTH_LONG).show()
            }
            else {
                if (Utils.isValidEmail(emailEditText.text.toString())) {
                    login(emailEditText.text.toString(), pwdEditText.text.toString())
                }
                else {
                    Toast.makeText(requireActivity(), "Incorrect e-mail or password!",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        val buttonForgotPassword : Button = view.findViewById(R.id.buttonForgotPassword)
        buttonForgotPassword.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        val buttonRegister : Button = view.findViewById(R.id.buttonRegister)
        buttonRegister.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    override fun onResume() {
        if( !Utils.isNetworkAvailable(requireContext()) ) {
            AlertDialogFragment().errorHandling(requireContext())
        }

        super.onResume()
    }

    fun login(email : String, password : String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    DatabaseManager.getUserData(email)
                    view?.let { Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_mainFragment) }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireActivity(), "Incorrect e-mail or password!",
                        Toast.LENGTH_LONG).show()
                }
            }
    }
}