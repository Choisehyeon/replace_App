package com.example.replace_application.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.replace_application.R
import com.example.replace_application.database.BookmarkDatabase
import com.example.replace_application.entity.Bookmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BookmarkRVAdapter(val context: Context, val bookmarkList : List<Bookmark>) : RecyclerView.Adapter<BookmarkRVAdapter.ViewHolder>() {
    private lateinit var db : BookmarkDatabase

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item : Bookmark, context : Context, db : BookmarkDatabase) {


            val placeName = itemView.findViewById<TextView>(R.id.placeName)
            placeName.text = item.placeName.toString()

            itemView.findViewById<ImageView>(R.id.bookmarkDelete).setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    CoroutineScope(Dispatchers.Default).async {
                        db.bookmarkDao().deleteBookmark(item)
                    }.await()
                    
                }
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookmarkRVAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bookmark_rv_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkRVAdapter.ViewHolder, position: Int) {
        db = BookmarkDatabase.getDatabase(context)
        holder.bindItems(bookmarkList[position], context, db)
    }

    override fun getItemCount(): Int {
        return bookmarkList.size
    }
}