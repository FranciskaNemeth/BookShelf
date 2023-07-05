package com.example.bookshelf.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.R
import com.example.bookshelf.adapter.BooksRecyclerViewAdapter
import com.example.bookshelf.adapter.OnBookItemClickListener
import com.example.bookshelf.database.DatabaseManager
import com.example.bookshelf.interfaces.GetBooksInterface
import com.example.bookshelf.model.Book
import com.example.bookshelf.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class MainFragment : Fragment(), OnBookItemClickListener {
    var allbooks : MutableList<Book> = ArrayList()
    var filteredbooks : MutableList<Book> = ArrayList()
    var favoritebooks : MutableList<Book> = ArrayList()
    var isShowingFavorites: Boolean = false
    lateinit var recyclerView : RecyclerView
    lateinit var searchView: SearchView
    private lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // in here you can do logic when backPress is clicked
                activity!!.finish()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.main_screen, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)

        val adapter = BooksRecyclerViewAdapter(filteredbooks, this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        DatabaseManager.getAllBooksData(currentUser.email.toString(), object : GetBooksInterface {
            override fun getBooks(books: MutableList<Book>) {
                this@MainFragment.filteredbooks.clear()
                this@MainFragment.filteredbooks.addAll(books)
                this@MainFragment.allbooks = books
                recyclerView.adapter!!.notifyDataSetChanged()
            }
        })


        val itemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(-0x7373730, -0x7373730)
        )
        drawable.setSize(1, 5)
        itemDecoration.setDrawable(drawable)
        recyclerView.addItemDecoration(itemDecoration)

        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterBooksBySearch(newText)
                }
                return false
            }
        })

        val addButton : FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        addButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_addFragment)
        }

        val buttonAll : Button = view.findViewById(R.id.buttonAll)
        buttonAll.setOnClickListener {
            isShowingFavorites = false
            filteredbooks.clear()
            filteredbooks.addAll(allbooks)
            recyclerView.adapter!!.notifyDataSetChanged()
        }

        val buttonFav : Button = view.findViewById(R.id.buttonFavorites)
        buttonFav.setOnClickListener {
            isShowingFavorites = true
            val favBooks = filterByFavorites()
            filteredbooks.clear()
            filteredbooks.addAll(favBooks)
            favoritebooks = favBooks
            recyclerView.adapter!!.notifyDataSetChanged()
            Log.d("FILTER", "Button clicked")
            Log.d("FILTER", "${favBooks.size}")
        }

        val buttonRecommend : Button = view.findViewById(R.id.buttonRecommend)
        buttonRecommend.setOnClickListener {
//            Thread {
//                runBlocking {
//                    launch {
//                        Recommender.getRecommendationFor(filterByFavorites())
//                    }
//                }
////                view.post {
////                    imageView.setImageBitmap(imageBitmap)
////                }
//            }.start()
            DatabaseManager.favoritebooks = filterByFavorites()
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_recommendFragment)
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
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_mainFragment_to_loginFragment) }
                }
            }

        }

    }

    override fun onResume() {
        if( !Utils.isNetworkAvailable(requireContext()) ) {
            val message = "Something went wrong! Please check your internet connection or try again later!"
            AlertDialogFragment().errorHandling(message,requireContext())
        }

        super.onResume()
    }

    override fun onItemClick(position: Int) {
        DatabaseManager.setSelectedBookData(filteredbooks[position])
        view?.let { Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_addFragment) }
    }

    override fun addOrRemoveFavorites(position: Int, shouldAdd: Boolean) {
        val book = allbooks[position]

        if (shouldAdd == true) {
            book.isFav = true
            DatabaseManager.updateBookData(currentUser.email.toString(), book)
        }
        else {
            book.isFav = false
            DatabaseManager.updateBookData(currentUser.email.toString(), book)
        }
    }

    fun filterByFavorites() : MutableList<Book> {
        val favBooks : MutableList<Book> = ArrayList()
        allbooks.forEach {
            Log.d("FILTER", "${it.title}, ${it.isFav}")
            if (it.isFav) {
                favBooks.add(it)
                Log.d("FILTER", it.title)
            }
        }
        return favBooks
    }

    fun filterBooksBySearch(searchText : String?) {
        val arrayList : ArrayList<Book> = ArrayList()
        lateinit var bookList: MutableList<Book>
        bookList = if (isShowingFavorites) {
            favoritebooks
        } else {
            allbooks
        }
        if (searchText != null && searchText.isNotEmpty() && bookList.isNotEmpty()) {
            bookList.forEach{
                if (it.title.lowercase(Locale.getDefault()).contains(searchText.lowercase(Locale.getDefault())) ||
                    it.author.lowercase(Locale.getDefault()).contains(searchText.lowercase(Locale.getDefault())) ||
                    it.genre.lowercase(Locale.getDefault()).contains(searchText.lowercase(Locale.getDefault()))) {
                        arrayList.add(it)
                }
            }
        }
        else {
            arrayList.addAll(bookList)
        }

        filteredbooks.clear()
        filteredbooks.addAll(arrayList)
        recyclerView.adapter!!.notifyDataSetChanged()
    }
}