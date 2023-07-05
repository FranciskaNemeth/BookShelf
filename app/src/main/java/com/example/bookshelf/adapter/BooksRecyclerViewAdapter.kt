package com.example.bookshelf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookshelf.R
import com.example.bookshelf.model.Book
import com.example.bookshelf.utils.Utils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class BooksRecyclerViewAdapter(displayBooks: MutableList<Book>,
                               private val clickListener: OnBookItemClickListener
                               ): RecyclerView.Adapter<BooksRecyclerViewAdapter.ViewHolder>() {
    private val displayBooks: MutableList<Book>
    val storage = Firebase.storage

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.books_listitem, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = Utils.capitalizeFirstLetters(displayBooks[position].title)
        val author = Utils.capitalizeFirstLetters(displayBooks[position].author)

        holder.title.text = title
        holder.author.text = author
        holder.genre.text = displayBooks[position].genre


        val ref = storage.reference.child("images/" + displayBooks[position].imageURL + ".jpg")
        ref.downloadUrl.addOnSuccessListener { Uri ->
            val imgURL = Uri.toString()
            Glide.with(holder.image.context)
                .load(imgURL)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.image)
        }

        if (checkIsFavorite(displayBooks[position]) == false) {
            holder.favourite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            holder.favourite.setTag(R.drawable.ic_baseline_favorite_border_24)
        }
        else {
            holder.favourite.setImageResource(R.drawable.ic_baseline_favorite_24)
            holder.favourite.setTag(R.drawable.ic_baseline_favorite_24)
        }

        holder.favourite.setOnClickListener {
            if (holder.favourite.getTag() == R.drawable.ic_baseline_favorite_border_24) {
                holder.favourite.setImageResource(R.drawable.ic_baseline_favorite_24)
                holder.favourite.setTag(R.drawable.ic_baseline_favorite_24)
                clickListener.addOrRemoveFavorites(holder.adapterPosition, true)
            }
            else {
                holder.favourite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                holder.favourite.setTag(R.drawable.ic_baseline_favorite_border_24)
                clickListener.addOrRemoveFavorites(holder.adapterPosition, false)
            }
        }
    }

    override fun getItemCount(): Int {
        return displayBooks.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var title: TextView = itemView.findViewById(R.id.textViewTitle)
        var author : TextView = itemView.findViewById(R.id.textViewAuthor)
        var genre : TextView = itemView.findViewById(R.id.textViewGenre)
        var image: ImageView = itemView.findViewById(R.id.base_avatar)
        var favourite: ImageButton = itemView.findViewById(R.id.imageButton)

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

    fun checkIsFavorite(book: Book) : Boolean {
        return book.isFav
    }
}

interface OnBookItemClickListener {
    fun onItemClick(position: Int)

    fun addOrRemoveFavorites(position: Int, shouldAdd : Boolean)
}