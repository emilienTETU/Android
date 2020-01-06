package com.example.myapp.ui.Web

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.myapp.R
import com.example.myapp.remote.LedStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.myapp.data.model.ApiService
import com.example.myapp.data.LocalPreferences

class HttpActivity : AppCompatActivity() {

    private val apiService = ApiService.Builder.instance
    private val ledStatus = LedStatus()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val currentSelectedDevice = LocalPreferences.getInstance(this).currentSelectedDevice
        if (currentSelectedDevice == null) {
            Toast.makeText(this, "Aucun périphérique connu", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            ledStatus.setIdentifier(currentSelectedDevice)

            val refresh = findViewById<ImageButton>(R.id.refresh)
            val btnNetwork = findViewById<Button>(R.id.changeStateLed)

            val text: TextView = findViewById<TextView>(R.id.textConnection)
            text.setText("isConnected to "+LocalPreferences.getInstance(this).currentSelectedDevice)

            refresh.setOnClickListener({ l -> refreshLedState() })
            btnNetwork.setOnClickListener({ l -> toggleWithNetwork() })
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLedState()
    }

    companion object { //méthode static en java
        private val IDENTIFIANT_ID = "IDENTIFIANT_ID" //TODO : a quoi sert l'identifiant

        fun StartActivity(context: Context, identifiant:String): Intent {
            val intent = Intent(context, HttpActivity::class.java)
            intent.putExtra(IDENTIFIANT_ID,identifiant)
            return intent
        }
    }

    fun getIdentifiant(): String? {
        //TODO : demander au prof
        val b = Intent.getIntent(null).extras
        return b?.getString(IDENTIFIANT_ID, null)
    }

    fun refreshLedState() {
        apiService.readStatus(ledStatus.getIdentifier()).enqueue(object : Callback<LedStatus> {
            override fun onResponse(call: Call<LedStatus>, ledStatusResponse: Response<LedStatus>) {
                runOnUiThread {
                    var newStatus = ledStatus.getStatus()
                    if (ledStatusResponse.body() != null) {
                        newStatus = ledStatusResponse.body()!!.getStatus()
                    }
                    ledStatus.setStatus(newStatus)
                    if(ledStatus.getStatus() == true){
                        findViewById<ImageView>(R.id.ledOff).visibility = View.GONE
                        findViewById<ImageView>(R.id.ledOn).visibility = View.VISIBLE
                    }
                    else{
                        findViewById<ImageView>(R.id.ledOff).visibility = View.VISIBLE
                        findViewById<ImageView>(R.id.ledOn).visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<LedStatus>, t: Throwable) {
                t.printStackTrace()
                runOnUiThread { Toast.makeText(this@HttpActivity, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    fun toggleWithNetwork(){
        ledStatus.reverseStatus()
        apiService.writeStatus(this.ledStatus).enqueue(object : Callback<LedStatus> {
            override fun onResponse(call: Call<LedStatus>, ledStatusResponse: Response<LedStatus>) {
                runOnUiThread {
                    var newStatus = ledStatus.getStatus()
                    if (ledStatusResponse.body() != null) {
                        newStatus = ledStatusResponse.body()!!.getStatus()
                    }
                    ledStatus.setStatus(newStatus)

                    if(ledStatus.getStatus() == true){
                        findViewById<ImageView>(R.id.ledOff).visibility = View.GONE
                        findViewById<ImageView>(R.id.ledOn).visibility = View.VISIBLE
                    }
                    else{
                        findViewById<ImageView>(R.id.ledOff).visibility = View.VISIBLE
                        findViewById<ImageView>(R.id.ledOn).visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<LedStatus>, t: Throwable) {
                t.printStackTrace()
                runOnUiThread { Toast.makeText(this@HttpActivity, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show() }
            }
        })
    }
}
