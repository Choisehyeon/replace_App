package com.example.replace_application.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.replace_application.BoardWriteActivity
import com.example.replace_application.R

class BoardImageRVAdapter(val context : Context, var image : MutableList<Uri>) :RecyclerView.Adapter<BoardImageRVAdapter.ViewHolder> () {


    private var listener : OnSendStateInterface? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.board_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(image[position], position)
    }

    override fun getItemCount(): Int {
        return image.size
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item : Uri, position: Int) {
            itemView.findViewById<ImageView>(R.id.deleteImgBtn).setOnClickListener {
                Log.d("board", position.toString())
                image.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, image.size)

                if(listener ==  null) return@setOnClickListener
                listener?.sendValue(image.size)
            }

            val imageArea = itemView.findViewById<ImageView>(R.id.board_image)
            imageArea.setImageURI(item)
        }
    }

    fun setOnStateInterface(listener: OnSendStateInterface) {
        this.listener = listener
    }

    interface OnSendStateInterface {
        fun sendValue(size : Int)
    }
}

