package com.example.replace_application.dao

import android.graphics.Bitmap
import androidx.room.*
import com.example.replace_application.entity.Board

@Dao
interface BoardDao {

    @Insert
    fun insertBoard(board: Board)

    @Delete
    fun deleteBoard(vararg board: Board)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg board: Board)

    @Query("select * from Board where board_uid == :uid and placeId == :place_id")
    fun boardData(uid: String, place_id: String): List<Board>

    @Query("select * from Board where id == :id")
    fun findById(id: Long): Board

}