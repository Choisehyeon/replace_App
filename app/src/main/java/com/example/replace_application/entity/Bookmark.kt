package com.example.replace_application.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long,
    @ColumnInfo(name = "uid")
    var uid : String,
    @ColumnInfo(name="placeId")
    var placeId : String,
    @ColumnInfo(name="placeName")
    var placeName : String,



)
