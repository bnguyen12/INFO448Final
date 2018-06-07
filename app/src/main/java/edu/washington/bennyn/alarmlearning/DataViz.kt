package edu.washington.bennyn.alarmlearning

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.orhanobut.hawk.Hawk

class DataViz : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_viz)
        val menu = findViewById<com.michaldrabik.tapbarmenulib.TapBarMenu>(R.id.tapBarMenu)
        menu.setOnClickListener {
            menu.toggle()
        }
        setBtnListeners()
    }

    private fun setBtnListeners() {
        val chartsButton = findViewById<ImageView>(R.id.chartsButton)
        val messageBtn = findViewById<ImageView>(R.id.messageButton)
        val listBtn = findViewById<ImageView>(R.id.listButton)
        val locationBtn = findViewById<ImageView>(R.id.locationButton)

        chartsButton.setOnClickListener {
            val intent = Intent(this, DataViz::class.java)
            startActivity(intent)
        }

        locationBtn.setOnClickListener {
            val intent = Intent(this, LocationAlarmSetter::class.java)
            startActivity(intent)
        }

        messageBtn.setOnClickListener {
            val intent = Intent(this, AlarmList::class.java)
            startActivity(intent)
        }

        listBtn.setOnClickListener {
            val intent = Intent(this, RealList::class.java)
            startActivity(intent)
        }
    }
}
