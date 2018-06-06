package edu.washington.bennyn.alarmlearning

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.orhanobut.hawk.Hawk

class RealList : AppCompatActivity() {
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_list)

        val mapOfAlarms = Hawk.get<MutableMap<String, ArrayList<String>>>("mapOfAlarms")
        val categories = mapOfAlarms.keys.toTypedArray()
        val spinner = findViewById<Spinner>(R.id.categorySpinner)
        val categoryTitle = findViewById<TextView>(R.id.categoryTitle)

        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                categoryTitle.text = "Choose a category of alarms"
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                categoryTitle.text = categories[position]
                Hawk.put("currentCategory", categories[position])
                listView = findViewById(R.id.listView)
                listView.adapter = customAdapter(this@RealList)
            }
        }
    }

    private class customAdapter(context: Context): BaseAdapter() {
        private val myContext: Context
        val mapOfAlarms = Hawk.get<MutableMap<String, ArrayList<String>>>("mapOfAlarms")
        val alarms = mapOfAlarms[Hawk.get("currentCategory")]!!

        init {
            myContext = context
        }
        override fun getItem(position: Int): Any {
            return alarms[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return alarms.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(myContext)
            val row = inflater.inflate(R.layout.custom_row, parent, false)
            val rowName = row.findViewById<TextView>(R.id.rowName)
            rowName.text = alarms[position]
            return row
        }
    }
}
