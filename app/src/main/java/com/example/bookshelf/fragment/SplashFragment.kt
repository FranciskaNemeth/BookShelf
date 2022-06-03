package com.example.bookshelf.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.bookshelf.MainActivity
import com.example.bookshelf.R

class SplashFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.splash_screen, container, false)

        Handler().postDelayed({
            Navigation.findNavController(view).navigate(R.id.action_splashFragment_to_loginFragment)
        }, 3000) // 3000 is the delayed time in milliseconds.

        return view
    }
}