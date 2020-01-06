package com.example.myapp.data.model

import android.bluetooth.BluetoothDevice

class Device(val name: String, val bluetoothDevice: BluetoothDevice){

    override fun equals(other: Any?): Boolean {
        if(other is Device && other.name == this.name){
            return true
        }
        return false
    }
}
