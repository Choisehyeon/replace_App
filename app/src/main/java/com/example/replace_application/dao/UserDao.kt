package com.example.replace_application.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.replace_application.entity.User
import com.google.firebase.auth.UserInfo

@Dao
interface UserDao {

    @Insert
    fun insertUser(user : User)

    @Query("select inviteCode from User where uid = :key")
    fun getInviteCode(key : String?) : String
}