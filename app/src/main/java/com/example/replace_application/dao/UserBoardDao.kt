package com.example.replace_application.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.replace_application.entity.UserWithBoards

@Dao
interface UserBoardDao {

    @Transaction
    @Query("SELECT * FROM User")
    fun getUserWithBoards() : List<UserWithBoards>

}