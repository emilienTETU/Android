package com.example.myapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.example.myapp.R
import com.example.myapp.data.model.Device

import java.util.ArrayList

class DeviceAdapter(context: Context, maList: ArrayList<Device>) : ArrayAdapter<Device>(context, 0, maList) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view

        //Position de l'item selectionné
        val monElement = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }

        //Récupération du text
        val tvTitle = view!!.findViewById<TextView>(R.id.text)

        //Remplacement du text par le nom de l'element
        tvTitle.text = monElement!!.name

        return view
    }

}
