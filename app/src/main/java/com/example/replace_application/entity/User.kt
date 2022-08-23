package com.example.replace_application.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val uid : String,
    @ColumnInfo(name="nickname") val nickName : String,
    @ColumnInfo(name="coupleId") val coupleId : String,
    @ColumnInfo(name="inviteCode") val inviteCode : String
)