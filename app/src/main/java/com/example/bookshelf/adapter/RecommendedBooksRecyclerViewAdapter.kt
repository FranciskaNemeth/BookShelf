package com.example.bookshelf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookshelf.R
import com.example.bookshelf.model.Book
import com.example.bookshelf.utils.Utils


class RecommendedBooksRecyclerViewAdapter(displayBooks: MutableList<Book>,
                                          private val clickListener: OnRecommendedBookItemClickListener
): RecyclerView.Adapter<RecommendedBooksRecyclerViewAdapter.ViewHolder>() {
    private val displayBooks: MutableList<Book>

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recommended_books_listitem, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = Utils.capitalizeFirstLetters(displayBooks[position].title)
        val author = Utils.capitalizeFirstLetters(displayBooks[position].author)

        holder.title.text = title
        holder.author.text = author
        holder.genre.text = displayBooks[position].genre.lowercase()

        Glide.with(holder.image.context)
            .load(displayBooks[position].imageURL)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return displayBooks.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var title: TextView = itemView.findViewById(R.id.textViewTitle)
        var author : TextView = itemView.findViewById(R.id.textViewAuthor)
        var genre : TextView = itemView.findViewById(R.id.textViewGenre)
        var image: ImageView = itemView.findViewById(R.id.base_avatar)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            clickListener.onItemClick(adapterPosition)

        }

    }

    init {
        this.displayBooks = displayBooks
    }
}

interface OnRecommendedBookItemClickListener {
    fun onItemClick(position: Int)
}