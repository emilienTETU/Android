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
        findViewById<View>(R.id.buttonHelp).setOnClickListener { l -> showDialog() } //l = lamda
        findViewById<View>(R.id.buttonScan).setOnClickListener { l -> startActivity(ScanActivity.StartActivity(this)) }
    }

    protected fun showDialog() {
        val dialog = MaterialDialog.Builder(this)  //Création d'un dialog (pop-up)
                .title(getString(R.string.dialogHelpTitle))
                .content(getString(R.string.dialogHelpContent))
                .positiveText(getString(R.string.dialogHelpPositive))
                .negativeText(getString(R.string.dialogHelpNegative))
                .onNegative { a, b ->
                    showDialog()
                    Toast.makeText(this, R.string.dialogHelpBoutonInvalide, Toast.LENGTH_LONG).show()
                }
                .show()
    }

    override fun onResume() {
        super.onResume()
        // Bouton d'action HTTP
        val httpBtn = findViewById<Button>(R.id.buttonHttp)
        // Récupération du Device selectionné
        val currentSelectedDevice = LocalPreferences.getInstance(this).currentSelectedDevice
        // Si un device est déjà enregistré alors on active la bouton sinon il n'est pas actif
        if (currentSelectedDevice != null) {
            httpBtn.isEnabled = true
            httpBtn.setOnClickListener { l -> startActivity(HttpActivity.StartActivity(this,getString(R.string.id)))}
        } else {
            // TODO : POP up connexion a un device
            httpBtn.isEnabled = false
        }
    }
}
