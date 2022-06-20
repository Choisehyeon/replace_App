package com.example.replace_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.databinding.DataBindingUtil

import com.example.replace_application.databinding.ActivityJoinBinding
import com.example.replace_application.model.UserModel
import com.example.replace_application.utils.FBRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.StringBuilder
import kotlin.random.Random

class JoinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(binding.passwordAreaJoin.text.toString().equals(binding.confirmPass.text.toString())) {
                    binding.check.text = "일치"
                } else {
                    binding.check.text = "불일치"
                }
            }

        }

        findViewById<EditText>(R.id.confirm_pass).addTextChangedListener(textWatcher);


    }
    fun joinClick(view: View) {
        auth = Firebase.auth

        val nickname = binding.nickname.text.toString()

        auth.createUserWithEmailAndPassword(binding.emailAreaJoin.text.toString(), binding.passwordAreaJoin.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("JoinActivity", nickname.toString())
                    val code = auth.currentUser!!.uid.substring(0,16)

                    FBRef.myUserRef
                        .child(auth.currentUser!!.uid)
                        .setValue(UserModel(nickname,false, code))

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                } else {
                   Log.d("JoinActivity", "fail")
                }
            }

    }





}