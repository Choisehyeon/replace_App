package com.example.replace_application.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.replace_application.dao.BookmarkDao
import com.example.replace_application.entity.Bookmark

@Database(entities = [Bookmark::class], version = 6 )
abstract class BookmarkDatabase  : RoomDatabase() {

    abstract fun bookmarkDao() : BookmarkDao

    companion object {

        @Volatile
        private var INSTANCE : BookmarkDatabase? = null

        fun getDatabase(
            context : Context
        ) : BookmarkDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookmarkDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}