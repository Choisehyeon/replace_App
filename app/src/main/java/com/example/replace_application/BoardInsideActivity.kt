package com.example.replace_application

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.example.replace_application.adapter.ViewPagerAdapter
import com.example.replace_application.databinding.ActivityBoardInsideBinding
import com.example.replace_application.model.BoardModel
import com.example.replace_application.utils.FBAuth
import com.example.replace_application.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import me.relex.circleindicator.CircleIndicator
import java.lang.Exception

class BoardInsideActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBoardInsideBinding
    internal lateinit var viewPager: ViewPager
    private val image = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        viewPager = binding.viewPager as ViewPager
        val key = intent.getStringExtra("key")
        val image_num = intent.getStringExtra("img_size")
        val placeId = intent.getStringExtra("placeId")
        val img_size = image_num!!.toInt() + 1
        Log.d("Board", img_size.toString())

        getBoardData(key!!, placeId!!)
        getImageUri(key!!)


        val adapter = ViewPagerAdapter(this, key!!, img_size)
        viewPager.adapter = adapter
        binding.indicator.setViewPager(viewPager)


        binding.updateBtn.setOnClickListener {
            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("key", key)
            intent.putExtra("placeId", placeId)
            intent.putExtra("image_list", image)
            startActivity(intent)
        }

        binding.deleteBtn.setOnClickListener {
            FBRef.boardRef.child(placeId).child(key).removeValue()
            finish()
        }



    }

    private fun getBoardData(key : String, placeId : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    Log.d("boardInside", dataSnapshot.toString())


                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                    binding.boardTitle.text = dataModel!!.title
                    binding.boardDate.text = dataModel.time
                    binding.boardContent.text = dataModel.content

                    val myUid = FBAuth.getUid()
                    val writerUid = dataModel.uid

                    if (myUid.equals(writerUid)) {
                        binding.updateBtn.isVisible = true
                        binding.deleteBtn.isVisible = true
                    } else {
                    }
                } catch (e : Exception) {
                    Log.d("boardInside", "삭제완료")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ContentListActivity", "loadPost: ", databaseError.toException())
            }
        }
        FBRef.boardRef.child(placeId).child(key).addValueEventListener(postListener)
    }

    private fun getImageUri(key : String) {
        val list = Firebase.storage.reference.child(key + "/")

// Download directly from StorageReference using Glide
// (See MyAppGlideModule for Loader registration)
        list.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult!!.items) {

                    item.downloadUrl.addOnSuccessListener { uri -> image.add(uri!!) }

                }
            }
    }
}