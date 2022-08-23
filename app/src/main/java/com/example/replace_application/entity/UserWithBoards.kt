package com.example.replace_application.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UserWithBoards (
    @Embedded val user: User,
    @Relation(
        parentColumn = "uid",
        entityColumn = "board_uid",
    )
    val boards : List<Board>
)