package com.example.replace_application.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.replace_application.entity.Bookmark

@Dao
interface BookmarkDao {

    @Insert
    fun insertBookmark(bookmark: Bookmark)

    @Delete
    fun deleteBookmark(bookmark: Bookmark)

    @Query("select * from bookmark where uid == :uid")
    fun getBookmark(uid : String) : List<Bookmark>
}