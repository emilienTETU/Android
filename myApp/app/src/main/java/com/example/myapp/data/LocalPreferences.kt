package com.example.myapp.data

import android.content.Context
import android.content.SharedPreferences
import android.content.Intent.getIntent
import android.os.Bundle



class LocalPreferences private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences

    val currentSelectedDevice: String?
        get() = sharedPreferences.getString("selectedDevice", null)

    init {
        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
    }

    fun saveCurrentSelectedDevice(deviceName: String) {
        sharedPreferences.edit().putString("selectedDevice", deviceName).apply()
    }

    companion object {
        private var INSTANCE: LocalPreferences? = null

        fun getInstance(context: Context): LocalPreferences {
            if (INSTANCE == null) {
                INSTANCE = LocalPreferences(context)
            }
            return INSTANCE as LocalPreferences
        }
    }
}
