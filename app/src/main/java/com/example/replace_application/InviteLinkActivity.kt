package com.example.replace_application

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.replace_application.databinding.ActivityInviteLinkBinding
import com.example.replace_application.model.UserModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class InviteLinkActivity : AppCompatActivity() {

    private val TAG = InviteLinkActivity::class.java.simpleName
    private lateinit var binding : ActivityInviteLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invite_link)


    }

}