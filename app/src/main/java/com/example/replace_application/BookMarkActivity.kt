package com.example.replace_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.replace_application.adapter.BoardRVAdapter
import com.example.replace_application.adapter.BookmarkRVAdapter
import com.example.replace_application.database.BookmarkDatabase
import com.example.replace_application.databinding.ActivityBookMarkBinding
import com.example.replace_application.entity.Bookmark
import com.example.replace_application.utils.FBAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BookMarkActivity : AppCompatActivity() {

    var bookmarkList = ArrayList<Bookmark>()
    private lateinit var rvAdapter: BookmarkRVAdapter
    private lateinit var binding : ActivityBookMarkBinding

    private lateinit var bookmarkdb : BookmarkDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_book_mark)

        bookmarkdb = BookmarkDatabase.getDatabase(this)
        getBookmarkData(FBAuth.getUid())

        Log.d("BookmarkActivity", bookmarkList.toString())

        rvAdapter = BookmarkRVAdapter(baseContext, bookmarkList)

        binding.bookmarkRV.adapter = rvAdapter
        binding.bookmarkRV.layoutManager = LinearLayoutManager(this)

    }


    private fun getBookmarkData(uid : String) {

        CoroutineScope(Dispatchers.Main).launch {

            bookmarkList.clear()

            val bookmark = CoroutineScope(Dispatchers.Default).async {
                bookmarkdb.bookmarkDao().getBookmark(uid)
            }.await()

            Log.d("BookmarkAc", bookmark.toString())

            CoroutineScope(Dispatchers.Main).async {
                for(i in 0..bookmark.size-1) {
                    bookmarkList.add(bookmark[i])
                }
            }.await()
            rvAdapter.notifyDataSetChanged()
        }

    }
}