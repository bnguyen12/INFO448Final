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
            val input = EditText(this)
            var message = "Do you want to text this number how many tasks you've done? You've completed "
            message += (Hawk.get("tasksDone") as Int).toString() + " tasks"
            input.inputType = InputType.TYPE_CLASS_PHONE
            val builder = AlertDialog.Builder(this)
            builder.setView(input)
            builder.setTitle("SMS Your Results")
            builder.setMessage(message)
            builder.setPositiveButton("Send", { dialog, which ->
                if (input.text.toString().length == 10) {
                    val smsMessage = "Hey, I've finished ${Hawk.get("tasksDone") as Int} tasks so far!"
                    SmsManager.getDefault().sendTextMessage(input.text.toString(), null,
                            smsMessage, null, null)
                } else {
                    Toast.makeText(this, "Not a valid number", Toast.LENGTH_SHORT).show()
                }
            })
            builder.setNegativeButton("Cancel", { dialog, which ->
                //do nothing
            })
            builder.show()
        }

        listBtn.setOnClickListener {
            val intent = Intent(this, RealList::class.java)
            startActivity(intent)
        }
    }
}
