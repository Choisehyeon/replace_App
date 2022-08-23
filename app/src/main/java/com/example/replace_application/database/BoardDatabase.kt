package com.example.replace_application.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.replace_application.Converters
import com.example.replace_application.dao.BoardDao
import com.example.replace_application.dao.UserDao
import com.example.replace_application.entity.Board

@Database(entities = [Board::class], version = 5)
@TypeConverters(Converters::class)
abstract class BoardDatabase : RoomDatabase() {

    abstract fun boardDao() : BoardDao

    companion object {

        @Volatile
        private var INSTANCE : BoardDatabase? = null

        fun getDatabase(
            context : Context
        ) : BoardDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BoardDatabase::class.java,
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