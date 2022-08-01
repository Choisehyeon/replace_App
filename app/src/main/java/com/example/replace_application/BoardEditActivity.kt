package com.example.replace_application

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.replace_application.adapter.BoardImageRVAdapter
import com.example.replace_application.databinding.ActivityBoardEditBinding
import com.example.replace_application.model.BoardModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.lang.Exception

class BoardEditActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBoardEditBinding
    private lateinit var writerUid : String
    private var image = ArrayList<Uri>()
    lateinit var rvAdapter: BoardImageRVAdapter
    private var alertDialog: AlertDialog? = null

    private var isImageUpload = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_edit)

        binding.imagePlus.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, 200)
        }
        val key = intent.getStringExtra("key")
        val placeId = intent.getStringExtra("placeId")
        getBoardData(key!!, placeId!!)
        image = intent.getSerializableExtra("image_list") as ArrayList<Uri>

        binding.imageNum.text = image.size.toString()

        rvAdapter = BoardImageRVAdapter(this, image).apply {
            setOnStateInterface(object : BoardImageRVAdapter.OnSendStateInterface {
                override fun sendValue(size: Int) {
                    binding.imageNum.text = size.toString()
                    if (size < 10) {
                        binding.imagePlus.visibility = View.VISIBLE
                    }
                }
            })
        }

        binding.boardImageRv.adapter = rvAdapter
        binding.boardImageRv.layoutManager = LinearLayoutManager(this).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }

        binding.writeEditBtn.setOnClickListener {
            editBoardData(key!!, placeId!!)

            intent.putExtra("key", key)
            setResult(RESULT_OK, intent)

            alertDialog = AlertDialog.Builder(this)
                .setTitle("게시글 수정 중")
                .setMessage("잠시만 기다려주세요!")
                .show()

            Handler().postDelayed({
                finish()
            }, 4000)


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
    private fun editBoardData(key: String, placeId: String) {
        FBRef.boardRef.child(placeId)
            .child(key)
            .setValue(BoardModel(binding.editTitle.text.toString(), binding.editContent.text.toString(),
            FBAuth.getTime(), writerUid, placeId))

        if (isImageUpload) {
            Log.d("Board", image.size.toString())

            for (i in 0..(image.size - 1)) {
                imageUpload(key, i)

            }
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

    private fun getBoardData(key : String, placeId : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    Log.d("BoardEdit", dataSnapshot.toString())


                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                    binding.editTitle.setText(dataModel!!.title)
                    binding.editContent.setText(dataModel!!.content)
                    writerUid = dataModel.uid

                } catch (e : Exception) {
                    Log.d("BoardEdit", "삭제완료")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("BoardEdit", "loadPost: ", databaseError.toException())
            }
        }
        FBRef.boardRef.child(placeId).child(key).addValueEventListener(postListener)
    }


}

