package com.example.replace_application.utils

import com.google.firebase.auth.FirebaseAuth

class FBAuth {
    companion object {
        private lateinit var auth: FirebaseAuth

        fun getUid(): String {

            auth = FirebaseAuth.getInstance()

            return auth.currentUser?.uid.toString()
        }
    }
}