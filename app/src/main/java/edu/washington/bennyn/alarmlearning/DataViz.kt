package edu.washington.bennyn.alarmlearning

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputType
import android.view.View
import android.widget.*
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

        val mapOfAlarms = Hawk.get<MutableMap<String, ArrayList<String>>>("mapOfAlarms")
        val categories = mapOfAlarms.keys.toTypedArray()
        val spinner = findViewById<Spinner>(R.id.categorySpinner)
        val categoryTitle = findViewById<TextView>(R.id.categoryTitle)

        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                categoryTitle.text = "Choose a category of alarms"
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { //do something when category is selected
                categoryTitle.text = categories[position]
                Hawk.put("currentCategory", categories[position])
            }
        }
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
