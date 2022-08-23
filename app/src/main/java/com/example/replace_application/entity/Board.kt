package com.example.replace_application.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Board (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long,
    @ColumnInfo(name="title") val title : String,
    @ColumnInfo(name="content") val content : String,
    @ColumnInfo(name="time") val time : String,
    @ColumnInfo(name="board_uid") val uid : String,
    @ColumnInfo(name="placeId") val placeId : String,
    @ColumnInfo(name="image") val image : Bitmap
)