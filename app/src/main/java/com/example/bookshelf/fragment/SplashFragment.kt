package com.example.bookshelf.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.bookshelf.R
import com.example.bookshelf.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        /*Handler().postDelayed({
            //Navigation.findNavController(view).navigate(R.id.action_splashFragment_to_loginFragment)

        }, 5000) // 3000 is the delayed time in milliseconds.*/

        return inflater.inflate(R.layout.splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth : FirebaseAuth = Firebase.auth
        if(auth.currentUser != null) {
            Navigation.findNavController(view).navigate(R.id.action_splashFragment_to_mainFragment)
        }
        else {
            Navigation.findNavController(view).navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }

    override fun onResume() {
        if( !Utils.isNetworkAvailable(requireContext()) ) {
            AlertDialogFragment().errorHandling(requireContext())
        }

        super.onResume()
    }
}