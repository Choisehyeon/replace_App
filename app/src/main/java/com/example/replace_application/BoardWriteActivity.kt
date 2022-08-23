package com.example.replace_application

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.replace_application.database.BoardDatabase
import com.example.replace_application.databinding.ActivityBoardWriteBinding
import com.example.replace_application.entity.Board
import com.example.replace_application.utils.FBAuth
import kotlinx.coroutines.*

class BoardWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardWriteBinding
    private var count = 0
    private var alertDialog: AlertDialog? = null
    private var image : Uri? = null
    private lateinit var boardImg : Bitmap
    private var isImageUpload = false
    private lateinit var db : BoardDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_write)

        db = BoardDatabase.getDatabase(this)

        binding.imagePlus.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 200)

            isImageUpload = true
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

            if (image == null) {
                Toast.makeText(this, "이미지를 넣어주세요.", Toast.LENGTH_LONG).show()
            }

            if (!title.equals("") && !content.equals("") && image != null) {
                val placeId = intent.getStringExtra("placeId")
                ///val key = FBRef.boardRef.child(placeId!!).push().key.toString()

               /* if (placeId != null) {
                    FBRef.boardRef
                        .child(placeId!!)
                        .child(key)
                        .setValue(BoardModel(title, content, time, uid, placeId))
                }
*/

                imageUpload()


                CoroutineScope(Dispatchers.Main).launch {
                    CoroutineScope(Dispatchers.Default).async{
                        db.boardDao().insertBoard(Board(0,title,content,time, uid, placeId!!,
                            boardImg
                        ))
                        intent.putExtra("placeId", placeId)
                    }.await()
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 200) {
            binding.imagePlus.setImageURI(data?.data)
            image = data?.data!!
        }
    }

    private fun imageUpload() {

      /*  val storage = Firebase.storage
        val storageRef = storage.reference

        val mountainsRef = storageRef.child(key + "/" + num.toString() + ".png")
*/
        /*val imageView = binding.boardImageRv.get(num).findViewById<ImageView>(R.id.board_image)
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()*/
        val imageUri = image
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri)
        boardImg = bitmap


        /*val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
        }
*/

    }

    override fun onDestroy() {
        super.onDestroy()

        //다이얼로그가 띄워져 있는 상태(showing)인 경우 dismiss() 호출
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }
}