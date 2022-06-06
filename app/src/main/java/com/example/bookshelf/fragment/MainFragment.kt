package com.example.bookshelf.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.R
import com.example.bookshelf.adapter.BooksRecyclerViewAdapter
import com.example.bookshelf.database.DatabaseManager
import com.example.bookshelf.model.Book
import com.example.bookshelf.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainFragment : Fragment() {
    var bookList : MutableList<Book> = ArrayList()
    lateinit var recyclerView : RecyclerView
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_screen, container, false)

        val book1 = Book("Book1", "Author", 1, "This is the book's description.")
        val book2 = Book("Book2", "Author", 2, "This is the book's description.")
        bookList.add(book1)
        bookList.add(book2)

        recyclerView = view.findViewById(R.id.recyclerView)
        val adapter = BooksRecyclerViewAdapter(bookList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val itemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(-0x7373730, -0x7373730)
        )
        drawable.setSize(1, 5)
        itemDecoration.setDrawable(drawable)
        recyclerView.addItemDecoration(itemDecoration)

        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()

        val addButton : FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        addButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_addFragment)
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        val logoutItem = menu.findItem(R.id.logout)
        if (logoutItem != null) {
            val logoutButton = logoutItem.actionView as AppCompatImageButton
            logoutButton.foreground = requireActivity().getDrawable(R.drawable.ic_baseline_logout_24)
            logoutButton.background.alpha = 0
            logoutButton.setOnClickListener{
                auth = Firebase.auth

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    FirebaseAuth.getInstance().signOut()
                    //finish()
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_mainFragment_to_loginFragment) }
                }
            }

        }

    }

    override fun onResume() {
        if( !Utils.isNetworkAvailable(requireContext()) ) {
            AlertDialogFragment().errorHandling(requireContext())
        }

        super.onResume()
    }
}