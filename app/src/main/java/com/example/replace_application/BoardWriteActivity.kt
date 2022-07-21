package com.example.replace_application

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.replace_application.adapter.BoardImageRVAdapter
import com.example.replace_application.databinding.ActivityBoardWriteBinding
import com.example.replace_application.model.BoardModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class BoardWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardWriteBinding
    private var imageCount = 0
    lateinit var rvAdapter: BoardImageRVAdapter
    var image = mutableListOf<Uri>()

    private var isImageUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_write)

        binding.imagePlus.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, 200)
        }
        rvAdapter = BoardImageRVAdapter(this, image).apply {
            setOnStateInterface(object : BoardImageRVAdapter.OnSendStateInterface {
                override fun sendValue(size: Int) {
                    binding.imageNum.text = size.toString()
                    imageCount = size
                    if(size < 10) {
                        binding.imagePlus.visibility = View.VISIBLE
                    }

                }

            })
        }
        binding.boardImageRv.adapter = rvAdapter
        binding.boardImageRv.layoutManager = LinearLayoutManager(this).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }

        binding.writeEnterBtn.setOnClickListener {
            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val time = FBAuth.getTime()
            val uid = FBAuth.getUid()

            val place= intent.getStringExtra("place").toString()
            val road = intent.getStringExtra("road").toString()


            if(title.equals("")) {
                Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_LONG).show()
            }
            if(content.equals("")) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_LONG).show()
            }
            if(!title.equals("") && !content.equals("")) {
                val key = FBRef.boardRef.push().key.toString()

                FBRef.boardRef
                    .child(key)
                    .setValue(BoardModel(title, content, time, uid, road, place))

                if(isImageUpload) {
                    imageUpload(key)
                }

            }

        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 200) {

            if (data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = data.clipData!!.itemCount

                if (count > 10 || image.size + count > 10) {
                    Toast.makeText(this, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
                    return
                }

                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    image.add(imageUri)
                }

            } else { // 단일 선택
                data?.data?.let { uri ->
                    val imageUri : Uri? = data?.data
                    if (imageUri != null) {
                        image.add(imageUri)
                    }
                }
            }

            if(image.size == 10) {
                binding.imagePlus.visibility = View.INVISIBLE
            } else {
                binding.imagePlus.visibility = View.VISIBLE
            }

            binding.imageNum.text = image.size.toString()
            rvAdapter.notifyDataSetChanged()

        }
    }

    private fun imageUpload(key: String) {

        val storage = Firebase.storage
        val storageRef = storage.reference


    }

}