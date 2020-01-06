package com.example.myapp.ui.main

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.View
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.example.myapp.ui.scan.ScanActivity
import com.example.myapp.R
import android.util.Log
import android.widget.Button
import com.example.myapp.data.LocalPreferences
import com.example.myapp.ui.Web.HttpActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Click sur les boutons
        findViewById<View>(R.id.button).setOnClickListener { l -> showDialog() } //l = lamda
        findViewById<View>(R.id.button2).setOnClickListener { l -> startActivity(ScanActivity.StartActivity(this)) }
    }

    protected fun showDialog() {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title("AIDE")
                .content("Deux choix possibles : \n- Connexion par Bluetooth \n- Connexion par HTTP\n\n Cela vous a t-il aider ?")
                .positiveText("OUI")
                .negativeText("NON")
                .onNegative { a, b ->
                    showDialog()
                    Toast.makeText(this, R.string.BoutonValide, Toast.LENGTH_LONG).show()
                }
                .show()
    }

    override fun onResume() {
        super.onResume()
        // Bouton d'action HTTP
        val actionBtn = findViewById<Button>(R.id.buttonHttp)
        val currentSelectedDevice = LocalPreferences.getInstance(this).currentSelectedDevice
        Log.i("DEVICE",currentSelectedDevice)
        if (currentSelectedDevice != null) {
            actionBtn.setEnabled(true);
            actionBtn.setOnClickListener { l -> startActivity(HttpActivity.StartActivity(this,"id"))}
        } else {
            actionBtn.setEnabled(false)
        }
    }
}
