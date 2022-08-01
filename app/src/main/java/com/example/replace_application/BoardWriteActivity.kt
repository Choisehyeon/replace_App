package com.example.replace_application

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.replace_application.adapter.BoardImageRVAdapter
import com.example.replace_application.databinding.ActivityBoardWriteBinding
import com.example.replace_application.model.BoardModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class BoardWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardWriteBinding
    private var imageCount = 0
    lateinit var rvAdapter: BoardImageRVAdapter
    var image = ArrayList<Uri>()
    private var count = 0
    private var alertDialog: AlertDialog? = null

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
                    if (size < 10) {
                        binding.imagePlus.visibility = View.VISIBLE
                    }

                }

            })
        }

        binding.writeEnterBtn.setOnClickListener {
            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()

            val time = FBAuth.getTime()
            val uid = FBAuth.getUid()


            if (title.equals("")) {
                Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_LONG).show()
            }
            if (content.equals("")) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_LONG).show()
            }
            if (image.size == 0) {
                Toast.makeText(this, "이미지를 한 장 이상 넣어주세요", Toast.LENGTH_LONG).show()
            }
            if (!title.equals("") && !content.equals("") && image.size != 0) {
                val placeId = intent.getStringExtra("placeId")
                val key = FBRef.boardRef.child(placeId!!).push().key.toString()

                if (placeId != null) {
                    FBRef.boardRef
                        .child(placeId!!)
                        .child(key)
                        .setValue(BoardModel(title, content, time, uid, placeId))
                }

                if (isImageUpload) {
                    Log.d("Board", image.size.toString())

                    for (i in 0..(image.size - 1)) {
                        imageUpload(key, i)

                    }
                }
                intent.putExtra("key", key)
                setResult(RESULT_OK, intent)

                alertDialog = AlertDialog.Builder(this)
                    .setTitle("게시글 저장 중")
                    .setMessage("잠시만 기다려주세요!")
                    .show()

                Handler().postDelayed({
                    finish()
                }, 4000)

            }
        }

        binding.boardImageRv.adapter = rvAdapter
        binding.boardImageRv.layoutManager = LinearLayoutManager(this).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
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
                    Log.d("board", imageUri.toString())
                    image.add(imageUri)
                }
                isImageUpload = true

            } else { // 단일 선택
                data?.data?.let { uri ->
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        image.add(imageUri)
                    }
                    isImageUpload = true
                }
            }

            if (image.size == 10) {
                binding.imagePlus.visibility = View.INVISIBLE
            } else {
                binding.imagePlus.visibility = View.VISIBLE
            }

            binding.imageNum.text = image.size.toString()
            rvAdapter.notifyDataSetChanged()


        }
    }

    private fun imageUpload(key: String, num: Int) {

        val storage = Firebase.storage
        val storageRef = storage.reference

        val mountainsRef = storageRef.child(key + "/" + num.toString() + ".png")

        /*val imageView = binding.boardImageRv.get(num).findViewById<ImageView>(R.id.board_image)
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()*/
        val imageUri = image[num]
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
        }


    }

    override fun onDestroy() {
        super.onDestroy()

        //다이얼로그가 띄워져 있는 상태(showing)인 경우 dismiss() 호출
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }
}