package com.example.replace_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.replace_application.databinding.ActivityTypeLinkBinding

class TypeLinkActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTypeLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_type_link)

        binding.exitBtn.setOnClickListener {
            finish()
        }

    }
}