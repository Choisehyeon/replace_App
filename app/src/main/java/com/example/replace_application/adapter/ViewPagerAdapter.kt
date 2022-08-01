package com.example.replace_application.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.replace_application.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ViewPagerAdapter(val context : Context, val key : String, val imgSize : Int) : PagerAdapter() {

    private var layoutInflater : LayoutInflater?= null

    override fun getCount(): Int {
        return imgSize
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater= context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = layoutInflater!!.inflate(R.layout.viewpager_activity, null)
        val image = v.findViewById<View>(R.id.viewPager_img) as ImageView


        if(position < imgSize) {
            getImageData(key, position, image, context)
            val vp = container as ViewPager
            vp.addView(v, 0)
        }


        return v
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val v = `object` as View
        vp.removeView(v)
    }

    private fun getImageData(key : String, position: Int ,imageView : ImageView, context: Context) {
        val storageReference = Firebase.storage.reference.child(key + "/" + position + ".png")
        Log.d("board", storageReference.toString())

        if(storageReference == null) {
            imageView.isVisible = false
        } else {
            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful) {

                    Glide.with(context)
                        .load(task.result)
                        .centerCrop()
                        .into(imageView)

                } else {
                    imageView.isVisible = false
                }
            })
        }
// ImageView in your Activity

// Download directly from StorageReference using Glide
// (See MyAppGlideModule for Loader registration)

    }
}