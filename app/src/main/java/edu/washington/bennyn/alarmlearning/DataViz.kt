package edu.washington.bennyn.alarmlearning

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputType
import android.view.View
import android.widget.*
import com.orhanobut.hawk.Hawk
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate


class DataViz : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_viz)
        Hawk.init(this).build()
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
                val categoryName = categories[position]
                categoryTitle.text = categoryName
                Hawk.put("currentCategory", categories[position])

                val chart = findViewById<PieChart>(R.id.chart)
                val entries = ArrayList<PieEntry>()
                chart.holeRadius = 0f
                chart.transparentCircleRadius = 0f

                val categoryStats = Hawk.get<MutableMap<String, Array<Int>>>("categoryStats")
                val stats = categoryStats[categoryName]
                entries.add(PieEntry((stats!![0].toFloat() / stats!![1].toFloat()) * 100f, "Tasks Accomplished"))
                entries.add(PieEntry(((stats!![1].toFloat() - stats!![0].toFloat()) / stats!![1].toFloat()) * 100f, "Tasks Not Accomplished"))

                val set = PieDataSet(entries, categoryName)
                set.colors = ColorTemplate.JOYFUL_COLORS.toMutableList()
                val data = PieData(set)
                data.setValueTextSize(14f)
                data.setValueTextColor(Color.WHITE)
                chart.setData(data)
                chart.setMinimumWidth(500)
                chart.invalidate()
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
