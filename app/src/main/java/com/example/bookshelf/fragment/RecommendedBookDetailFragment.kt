package com.example.bookshelf.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.bookshelf.R
import com.example.bookshelf.database.DatabaseManager
import com.example.bookshelf.model.Book
import com.example.bookshelf.utils.Utils
import com.google.android.material.textfield.TextInputEditText


class RecommendedBookDetailFragment : Fragment() {
    lateinit var imageView : ImageView
    lateinit var textInputEditTextAuthor : TextInputEditText
    lateinit var textInputEditTextTitle: TextInputEditText
    lateinit var textInputEditTextGenre: TextInputEditText
    lateinit var textInputEditTextDescription: TextInputEditText
    lateinit var book : Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_recommended_book_detail, container, false)

        imageView = view.findViewById(R.id.imageView)
        textInputEditTextAuthor = view.findViewById(R.id.textInputEditTextAuthor)
        textInputEditTextTitle = view.findViewById(R.id.textInputEditTextTitle)
        textInputEditTextGenre = view.findViewById(R.id.textInputEditTextGenre)
        textInputEditTextDescription = view.findViewById(R.id.textInputEditTextDescription)

        val selectedBook = DatabaseManager.getSelectedBookData()

        if(selectedBook != null ) {
            book = selectedBook

            Glide.with(requireActivity())
                .load(book.imageURL)
                .placeholder(R.drawable.logo)
                .into(imageView)

            val title = Utils.capitalizeFirstLetters(book.title)
            val author = Utils.capitalizeFirstLetters(book.author)

            textInputEditTextAuthor.setText(author)
            textInputEditTextTitle.setText(title)
            textInputEditTextGenre.setText(book.genre)
            textInputEditTextDescription.setText(book.description)
        }

        imageView.setOnClickListener {
            showPicture()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DatabaseManager.deleteSelectedBookData()

        Log.d("DELETE", "delete selected book")
    }

    private fun showPicture() {
        val alertAdd = AlertDialog.Builder(requireContext())
        val factory = LayoutInflater.from(requireContext())
        val view: View = factory.inflate(R.layout.image_dialog, null)
        val img = view.findViewById<ImageView>(R.id.imageDialog)

        if(this::book.isInitialized) {
            Glide.with(requireActivity())
                .load(book.imageURL)
                .placeholder(R.drawable.logo)
                .into(img)
        }

        alertAdd.setView(view)
        alertAdd.setNeutralButton("Close") { _, _ ->

        }

        alertAdd.show()
    }
}