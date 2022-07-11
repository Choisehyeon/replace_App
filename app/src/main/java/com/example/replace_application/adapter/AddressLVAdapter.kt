package com.example.replace_application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.replace_application.R
import com.example.replace_application.data.getfindaddress.Address
import com.example.replace_application.data.getfindaddress.Document
import com.example.replace_application.data.getfindaddress.RoadAddress

class AddressLVAdapter(val items: MutableList<Document>) : BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if(view == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.address_lv, parent, false)
        }

        val item : Document = items[position]

        view!!.findViewById<TextView>(R.id.place_item).text = item.address_name

        return view!!
    }
}