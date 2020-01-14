package com.example.myapp.ui.scan

import android.Manifest
import androidx.appcompat.app.AppCompatActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.example.myapp.R
import com.example.myapp.data.model.Device
import com.example.myapp.ui.adapter.DeviceAdapter

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.annotation.TargetApi
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.example.myapp.data.LocalPreferences
import java.util.*
import kotlin.collections.ArrayList


class ScanActivity : AppCompatActivity() {

    // REQUEST Code de gestion
    private val REQUEST_LOCATION_CODE = 1235
    private val REQUEST_ENABLED_LOCATION_CODE = 1236
    private val SCAN_DURATION_MS = 10_000L
    private val REQUEST_ENABLE_BLE = 999

    // Gestion du bluetooth
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var currentBluetoothGatt: BluetoothGatt? = null // Connexion actuelle
    private var isScanning = false
    private val scanningHandler = Handler()

    // Partie adapter
    private var deviceAdapter: DeviceAdapter? = null
    private val deviceArrayList = ArrayList<Device>()

    // Filtre UUID
    private val DEVICE_UUID = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c")
    private val CHARACTERISTIC_TOGGLE_LED_UUID = UUID.fromString("59b6bf7f-44de-4184-81bd-a0e3b30c919b")

    private var selectedDevice: Device? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        //RecyclerView
        deviceAdapter = DeviceAdapter(this, deviceArrayList) //val deviceAdapter =  DeviceAdapter deviceAdapter
        val rvDevices = findViewById<ListView>(R.id.rvList)

        rvDevices.adapter = deviceAdapter
        rvDevices.isClickable = true
        rvDevices.setOnItemClickListener { parent, view, position, id -> listClick(position) }

        findViewById<View>(R.id.buttonScan).setOnClickListener(this::scanClick)
        findViewById<View>(R.id.buttonDeconnection).setOnClickListener { l -> deconnection() } //l = lamda
        findViewById<View>(R.id.buttonLed).setOnClickListener { l -> toggleLed() }
    }

    //méthode static en kotlin -> Peut avoir qu'un companion par class
    companion object {
        val CHARACTERISTIC_NOTIFY_STATE = UUID.fromString("d75167c8-e6f9-4f0b-b688-09d96e195f00")
        fun StartActivity(context: Context): Intent {
            return Intent(context, ScanActivity::class.java)
        }
    }

    private fun scanClick(v: View) {
        checkPermissions()
    }

    private fun setupBLE() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.adapter
        }

        if (bluetoothManager == null || !bluetoothAdapter?.isEnabled!!) { // bluetooth is off
            showDialogBluetooth()
        } else {
            scanNearbyDevices() // start scanning by default
        }
    }

    private fun scanNearbyDevices() {
        if (isScanning) {
            return
        }

        isScanning = true
        scanningHandler.postDelayed(scanDevicesRunnable, SCAN_DURATION_MS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // for recent version of android
            val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            val scanFilters = ArrayList<ScanFilter>()

            // Filtre sur le scan
            // scanFilters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(DEVICE_UUID)).build()); // add service filters

            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanFilters, settings, bleLollipopScanCallback)
        } else {
            showDialogAndroidVersion()
        }
    }

    private val scanDevicesRunnable = {
        stopScan()
    }


    private fun stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdapter?.bluetoothLeScanner?.startScan(bleLollipopScanCallback)
        } else {
            showDialogAndroidVersion()
        }
        isScanning = false
    }

    private val bleLollipopScanCallback = object : ScanCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val bluetoothDevice = result.device
            if (bluetoothDevice.name != null) {
                val device = Device(bluetoothDevice.name, bluetoothDevice)
                if (!deviceArrayList.contains(device)) {
                    deviceArrayList.add(device)
                    if (deviceAdapter != null) {
                        deviceAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            if (errorCode != 1) {
                showDialogErrorScan(errorCode)
            }
        }
    }


    //Check permission
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        } else {
            checkForLocationEnabled()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkForLocationEnabled()
            } else {
                checkPermissions() // force permission
            }
        }
    }

    private fun checkForLocationEnabled() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (lm != null) {
            val gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!gps_enabled || !network_enabled) {
                showDialogLocalisation()
            } else {
                setupBLE()
            }
        } else {
            showDialogLocalisation()
        }
    }

    private fun listClick(position: Int) {
        var item = deviceAdapter?.getItem(position)
        selectedDevice = item
        Log.i("TEST", "" + item!!.name)
        LocalPreferences.getInstance(this).saveCurrentSelectedDevice(item.name)
        connectToCurrentDevice()
    }

    private fun connectToCurrentDevice() {
        if (selectedDevice != null) {
            Toast.makeText(this, getString(R.string.connection), Toast.LENGTH_SHORT).show()
            currentBluetoothGatt = selectedDevice!!.bluetoothDevice.connectGatt(this, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            runOnUiThread {
                //Toast.makeText(this@ScanActivity, "Services discovered with success", Toast.LENGTH_SHORT).show()
                setUiMode(true)
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            runOnUiThread {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> currentBluetoothGatt?.discoverServices() // start services
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        gatt.close()
                        setUiMode(false)
                    }
                }

            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt,characteristic)
            runOnUiThread{
                if (characteristic.getStringValue(0).equals(getString(R.string.on))){
                    findViewById<ImageView>(R.id.ledOff).visibility = View.GONE
                    findViewById<ImageView>(R.id.ledOn).visibility = View.VISIBLE
                }
                else{
                    findViewById<ImageView>(R.id.ledOff).visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.ledOn).visibility = View.GONE
                }
            }
        }
    }

    private fun deconnection() {
        discconnectFromCurrentDevice()
        findViewById<View>(R.id.buttonScan).visibility = View.VISIBLE
        // On cache le RecyclerView
        findViewById<View>(R.id.rvList).visibility = View.VISIBLE
        // On affiche le TextView qui indique le device sur lequel on est connecté
        val text: TextView = findViewById<TextView>(R.id.textConnection)
        text.visibility = View.GONE
        // On affiche le bouton déconnexion
        findViewById<View>(R.id.buttonDeconnection).visibility = View.GONE
        // On affiche le bouton permettant de changer l'état de la led
        findViewById<View>(R.id.buttonLed).visibility = View.GONE
        findViewById<ImageView>(R.id.ledOff).visibility = View.GONE
        findViewById<ImageView>(R.id.ledOn).visibility = View.GONE
    }

    private fun discconnectFromCurrentDevice() {
        if (currentBluetoothGatt != null) {
            currentBluetoothGatt!!.disconnect()
        }
    }


    private fun setUiMode(isConnected: Boolean) {
        if (isConnected) {
            //Enregistrement du Device
            LocalPreferences.getInstance(this).saveCurrentSelectedDevice(selectedDevice!!.name)
            // On vide l'apapter
            deviceArrayList.clear()
            deviceAdapter!!.notifyDataSetChanged()
            // On cache le bouton scan
            findViewById<View>(R.id.buttonScan).visibility = View.GONE
            // On cache le RecyclerView
            findViewById<View>(R.id.rvList).visibility = View.GONE
            // On affiche le TextView qui indique le device sur lequel on est connecté
            val text = findViewById<TextView>(R.id.textConnection)
            text.visibility = View.VISIBLE
            // On set la bonne valeur au TextView
            text.text = getString(R.string.connectedTo) + selectedDevice!!.name
            // On affiche le bouton déconnexion
            findViewById<View>(R.id.buttonDeconnection).visibility = View.VISIBLE
            // On affiche le bouton permettant de changer l'état de la led
            findViewById<View>(R.id.buttonLed).visibility = View.VISIBLE
            // On oublie pas de stopper le scan
            stopScan()

            enableListenBleNotify()
        } else {
            findViewById<View>(R.id.buttonScan).visibility = View.VISIBLE
            // On cache le RecyclerView
            findViewById<View>(R.id.rvList).visibility = View.VISIBLE
            // On affiche le TextView qui indique le device sur lequel on est connecté
            val text = findViewById<TextView>(R.id.textConnection)
            text.visibility = View.GONE
            findViewById<View>(R.id.buttonDeconnection).visibility = View.GONE
            // On affiche le bouton permettant de changer l'état de la led
            findViewById<View>(R.id.buttonLed).visibility = View.GONE
        }

    }

    private fun toggleLed() {
        if (currentBluetoothGatt == null) {
            //showDialogErrorConnection()
            return
        }

        val service = currentBluetoothGatt!!.getService(DEVICE_UUID)
        if (service == null) {
            showDialogUUIDUnkown()
            return
        }

        val toggleLed = service.getCharacteristic(CHARACTERISTIC_TOGGLE_LED_UUID)
        toggleLed.setValue("1")
        currentBluetoothGatt!!.writeCharacteristic(toggleLed)
    }

    private fun enableListenBleNotify() {
        if (currentBluetoothGatt == null) {
            //showDialogErrorConnection()
            return
        }

        val service = currentBluetoothGatt?.getService(DEVICE_UUID)
        if (service == null) {
            showDialogUUIDUnkown()
            return
        }

        Toast.makeText(this, getString(R.string.connected), Toast.LENGTH_SHORT).show()
        val notification =
                service.getCharacteristic(CHARACTERISTIC_NOTIFY_STATE) // Indique que le GATT Client va écouter les notifications sur le charactérisque
        currentBluetoothGatt?.setCharacteristicNotification(notification, true)
    }

    protected fun showDialogUUIDUnkown() {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title(getString(R.string.dialogScanUUIDTitle))
                .content(getString(R.string.dialogScanUUIDContent))
                .positiveText(getString(R.string.dialogScanUUIDPositive))
                .show()
    }

    protected fun showDialogAndroidVersion() {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title(getString(R.string.dialogScanVersionTitle))
                .content(getString(R.string.dialogScanVersionContent))
                .positiveText(getString(R.string.dialogScanVersionPositive))
                .show()
    }

    protected fun showDialogErrorScan(errorCode: Int) {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title(getString(R.string.dialogScanErrorScanTitle))
                .content(""+errorCode)
                .positiveText(getString(R.string.dialogScanErrorScanPositive))
                .negativeText(getString(R.string.dialogScanErrorScanNegative))
                .onNegative { a, b ->
                    checkPermissions()
                }
                .show()
    }

    protected fun showDialogLocalisation() {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title(getString(R.string.dialogScanLocalisationTitle))
                .content(getString(R.string.dialogScanLocalisationContent))
                .positiveText(getString(R.string.dialogScanLocalisationPositive))
                .negativeText(getString(R.string.dialogScanLocalisationNegative))
                .onNegative { a, b ->
                    showDialogLocalisationRefuse()
                }
                .onPositive { a, b ->
                    startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_ENABLED_LOCATION_CODE)
                }
                .show()
    }

    protected fun showDialogLocalisationRefuse() {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title(getString(R.string.dialogScanLocalisationRefuseTitle))
                .content(getString(R.string.dialogScanLocalisationRefuseContent))
                .positiveText(getString(R.string.dialogScanLocalisationPositive))
                .negativeText(getString(R.string.dialogScanLocalisationNegative))
                .onNegative { a, b ->
                   finish()
                }
                .onPositive { a, b ->
                    startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_ENABLED_LOCATION_CODE)
                }
                .show()
    }

    protected fun showDialogBluetooth() {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title(getString(R.string.dialogScanBluetoothTitle))
                .content(getString(R.string.dialogScanBluetoothContent))
                .positiveText(getString(R.string.dialogScanBluetoothPositive))
                .onPositive { a, b ->
                    startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLE)
                }
                .show()
    }
}


