package com.example.replace_application

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.replace_application.database.BoardDatabase
import com.example.replace_application.databinding.ActivityBoardEditBinding
import com.example.replace_application.entity.Board
import com.example.replace_application.utils.FBAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BoardEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardEditBinding
    private lateinit var writerUid: String
    private lateinit var db: BoardDatabase
    private lateinit var image: Uri
    private lateinit var img: Bitmap
    private var boardId : Long = 0
    private lateinit var editBoard : Board
    private var alertDialog: AlertDialog? = null

    private var isImageUpload = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_edit)

        db = BoardDatabase.getDatabase(this)

        boardId = intent.getLongExtra("boardId", 0)
        getBoardData(boardId)


        binding.editImg.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, 200)
            isImageUpload = true
        }


        binding.writeEditBtn.setOnClickListener {
            editBoardData(editBoard.id, editBoard.uid, editBoard.placeId)

            intent.putExtra("boardId", boardId)
            setResult(RESULT_OK, intent)
            finish()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 200) {
            binding.editImg.setImageURI(data?.data)
            image = data?.data!!
        }
    }

    private fun editBoardData(id : Long, uid : String, placeId : String) {

        if (binding.editTitle.text.equals("")) {
            Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_LONG).show()
        }

        if (binding.editContent.text.equals("")) {
            Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_LONG).show()
        }
        if (isImageUpload) {
            imageUpload()
            Log.d("BoardEdit", id.toString())
            val editBoard = Board(id, binding.editTitle.text.toString(), binding.editContent.text.toString(), FBAuth.getTime(), uid, placeId, img)

            CoroutineScope(Dispatchers.Default).async {
                db.boardDao().update(editBoard)
            }
        }
    }

    private fun imageUpload() {
        val imageUri = image
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri)
        img = bitmap
    }

    override fun onDestroy() {
        super.onDestroy()

        //다이얼로그가 띄워져 있는 상태(showing)인 경우 dismiss() 호출
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }

    fun getBoardData(id : Long){
        CoroutineScope(Dispatchers.Main).launch {
            val board = CoroutineScope(Dispatchers.Default).async {
                db.boardDao().findById(id)
            }.await()

            editBoard = board

            binding.editTitle.setText(board!!.title)
            binding.editContent.setText(board!!.content)
            binding.editImg.setImageBitmap(board.image)
        }
    }


}

