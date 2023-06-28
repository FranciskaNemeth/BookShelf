package com.example.bookshelf.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.R
import com.example.bookshelf.adapter.OnRecommendedBookItemClickListener
import com.example.bookshelf.adapter.RecommendedBooksRecyclerViewAdapter
import com.example.bookshelf.database.DatabaseManager
import com.example.bookshelf.interfaces.GetRecommendedBooksInterface
import com.example.bookshelf.model.Book
import com.example.bookshelf.recommendation.Recommender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecommendFragment : Fragment(), OnRecommendedBookItemClickListener {
    lateinit var recyclerView : RecyclerView
    private lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    var favBooks : MutableList<Book> = ArrayList()
    var recommendedBooks : MutableList<Book> = ArrayList()
    lateinit var loadingLayout : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        favBooks = DatabaseManager.favoritebooks
        hideLoading()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommend, container, false)

        loadingLayout = view.findViewById(R.id.loading_layout)
        recyclerView = view.findViewById(R.id.recyclerView)

        val adapter = RecommendedBooksRecyclerViewAdapter(recommendedBooks, this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.adapter!!.notifyDataSetChanged()

        val itemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(-0x7373730, -0x7373730)
        )
        drawable.setSize(1, 5)
        itemDecoration.setDrawable(drawable)
        recyclerView.addItemDecoration(itemDecoration)

        showLoading()
        Thread {
            runBlocking {
                launch {
                    Recommender.getRecommendationFor(favBooks, object : GetRecommendedBooksInterface {
                        override fun onSuccess(recommendedBooks: List<Book>) {
                            this@RecommendFragment.recommendedBooks.clear()
                            this@RecommendFragment.recommendedBooks.addAll(recommendedBooks)
                            view?.post {
                                hideLoading()
                                recyclerView.adapter!!.notifyDataSetChanged()
                            }
                        }

                        override fun onError(reason: String?) {
                            Log.e("ERROR", reason.toString())
                            view?.post {
                                hideLoading()
                                AlertDialogFragment().errorHandling(reason.toString(), requireContext())
                            }
                        }
                    })
                }
            }
        }.start()

        if (adapter.itemCount > 0 ) {
            hideLoading()
        }

        return view
    }

    override fun onItemClick(position: Int) {
        DatabaseManager.setSelectedBookData(recommendedBooks[position])
        view?.let { Navigation.findNavController(it).navigate(R.id.action_recommendFragment_to_recommendedBookDetailFragment) }
    }

    private fun showLoading() {
        if (this::loadingLayout.isInitialized) {
            loadingLayout.setVisibility(View.VISIBLE)
        }
    }

    private fun hideLoading() {
        if (this::loadingLayout.isInitialized) {
            loadingLayout.setVisibility(View.GONE)
        }
    }

}