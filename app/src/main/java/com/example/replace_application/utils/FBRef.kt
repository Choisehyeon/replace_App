package com.example.replace_application.utils
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {
    companion object {
        val database = Firebase.database

        val myUserRef = database.getReference("user_ref")
        val boardRef = database.getReference("board_ref")
    }

}