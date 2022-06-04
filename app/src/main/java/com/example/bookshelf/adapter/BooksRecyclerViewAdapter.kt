package com.example.bookshelf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.R
import com.example.bookshelf.model.Book

class BooksRecyclerViewAdapter(dataSet: MutableList<Book>) : RecyclerView.Adapter<BooksRecyclerViewAdapter.ViewHolder>() {
    private val dataList: MutableList<Book>

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.books_listitem, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = dataList[position].title
        holder.author.text = dataList[position].author
        holder.genre.text = dataList[position].genre.toString()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var author : TextView
        var genre : TextView
        var parenLayout: ConstraintLayout

        init {
            title = itemView.findViewById(R.id.textViewTitle)
            author = itemView.findViewById(R.id.textViewAuthor)
            genre = itemView.findViewById(R.id.textViewGenre)
            parenLayout = itemView.findViewById(R.id.book_view_layout)
        }

    }

    init {
        this.dataList = dataSet
    }

}