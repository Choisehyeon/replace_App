package com.example.replace_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.replace_application.database.BoardDatabase
import com.example.replace_application.databinding.ActivityBoardInsideBinding
import com.example.replace_application.entity.Board
import com.example.replace_application.model.BoardModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class BoardInsideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardInsideBinding
    private lateinit var db: BoardDatabase
    var boardId: Long = 0
    lateinit var deleteBoard: Board
    lateinit var placeId : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        db = BoardDatabase.getDatabase(this)

        boardId = intent.getLongExtra("boardId", 0)
        Log.d("BoardInside", boardId.toString())

        getBoardData(boardId)


        binding.updateBtn.setOnClickListener {
            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("boardId", boardId)
            startActivity(intent)
        }

        binding.deleteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                CoroutineScope(Dispatchers.Default).async {
                    db.boardDao().deleteBoard(deleteBoard)
                    intent.putExtra("placeId", placeId)
                }.await()
                setResult(RESULT_OK, intent)
                finish()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            boardId = data?.getLongExtra("boardId", 0)!!
            Log.d("BoardInsideFin", boardId.toString())
            getBoardData(boardId)
        }
    }

    private fun getBoardData(boardId: Long) {

        CoroutineScope(Dispatchers.Main).launch {
            val board = CoroutineScope(Dispatchers.Default).async {
                db.boardDao().findById(boardId)

            }.await()

            deleteBoard = board
            placeId = board.placeId
            binding.boardTitle.text = board!!.title
            binding.boardContent.text = board!!.content
            binding.boardDate.text = board!!.time
            binding.boardImage.setImageBitmap(board.image)

            if (FBAuth.getUid().equals(board.uid)) {
                binding.updateBtn.isVisible = true
                binding.deleteBtn.isVisible = true
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        intent.putExtra("placeId", placeId)
        setResult(RESULT_OK, intent)
    }
}