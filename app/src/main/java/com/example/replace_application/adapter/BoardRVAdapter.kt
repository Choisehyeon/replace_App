package com.example.replace_application.adapter

import android.content.Context
import android.content.Intent
import android.net.PlatformVpnProfile
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.replace_application.BoardInsideActivity
import com.example.replace_application.MainActivity
import com.example.replace_application.R
import com.example.replace_application.model.BoardModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BoardRVAdapter(
    val context: Context, val dataList: List<BoardModel>,
    val keyList: List<String>
) : RecyclerView.Adapter<BoardRVAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(item: BoardModel, key: String, context: Context) {

            val title = itemView.findViewById<TextView>(R.id.contentTitle)
            title.text = item.title

            val date = itemView.findViewById<TextView>(R.id.contentDate)
            date.text = item.time

            val placeId = itemView.findViewById<TextView>(R.id.placeId)
            placeId.text = item.placeId

            val content = itemView.findViewById<TextView>(R.id.content)
            if (item.content.length >= 30) {
                content.text = item.content.substring(0, 30)
            } else {
                content.text = item.content
            }

            val contentImg = itemView.findViewById<ImageView>(R.id.contentImg)
            val imgNum = itemView.findViewById<TextView>(R.id.boardImgNum)

            getImageData(key, contentImg, context, imgNum)


            itemView.setOnClickListener {
                val intent = Intent(context, BoardInsideActivity::class.java)
                intent.putExtra("key", key)
                intent.putExtra("img_size", imgNum.text)
                intent.putExtra("placeId", item.placeId)
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

        }

        private fun getImageData(key: String, imageView: ImageView, context: Context, textView: TextView) {
            val list = Firebase.storage.reference.child(key + "/")
            Log.d("hello", list.toString())

            var imgCount = 0

            list.listAll()
                .addOnSuccessListener { listResult ->
                    for (item in listResult!!.items) {
                        imgCount++
                    }
                    textView.text = "+" + (imgCount-1).toString()
                }

            val storageReference = Firebase.storage.reference.child(key + "/0.png")

            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {

                    Glide.with(context)
                        .load(task.result)
                        .centerCrop()
                        .into(imageView)

                } else {
                    imageView.isVisible = false
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardRVAdapter.ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.board_content_item, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: BoardRVAdapter.ViewHolder, position: Int) {
        holder.bindItems(dataList[position], keyList[position], context)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}