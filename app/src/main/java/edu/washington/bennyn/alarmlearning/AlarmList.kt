package edu.washington.bennyn.alarmlearning

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.orhanobut.hawk.Hawk
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.telephony.SmsManager
import android.text.InputType
import android.widget.*
import kotlinx.android.synthetic.main.activity_alarm_list.*
import java.text.SimpleDateFormat
import java.util.*


class AlarmList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_list)

        val categoryView = findViewById<TextView>(R.id.categoryView)
        val beginTime = findViewById<TextView>(R.id.beginTime)
        val endTime = findViewById<TextView>(R.id.endTime)
        val alarmName = findViewById<TextView>(R.id.alarmNameView)
        val submitButton = findViewById<Button>(R.id.submitAlarm)
        val menu = findViewById<com.michaldrabik.tapbarmenulib.TapBarMenu>(R.id.tapBarMenu)
        val smsButton = findViewById<Button>(R.id.smsButton)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Set menu bar buttons
        setBtnListeners()

        // Check if this has been opened before. If so, populate a list of alarms
        Hawk.init(this).build() //use Hawk to store all sorts of things easy in memory for later use
        val settings = getSharedPreferences("preferencesFile", 0)
        if (settings.getBoolean("firstCheck", true)) {
            Log.d("firstCheck", "This app is being opened for the first time")
            Hawk.put("tasksDone", 0)
            Hawk.put("tasksTotal", 0)
            Hawk.put("mapOfAlarms", mutableMapOf<String, ArrayList<String>>()) // [category, [[alarm names]]
            Hawk.put("categoryStats", mutableMapOf<String, ArrayList<Int>>()) // [category, [tasks accomplished, tasks total]
            settings.edit().putBoolean("firstCheck", false).apply()
        } else {
            Log.d("firstCheck", "This app has data already present")
        }

        // Setup menu buttons
        menu.setOnClickListener {
            menu.toggle()
        }

        smsButton.setOnClickListener {
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

        //Fill in the beginning time for one of the views
        beginTime.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this,
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        var hour = hourOfDay % 12
                        if (hour == 0) {
                            hour = 12
                        }
                        var amPm = "PM"
                        if (hourOfDay < 12) {
                            amPm = "AM"
                        }
                        val timeInString = String.format(Locale.getDefault(), "%02d:%02d%s", hour, minute, amPm)
                        beginTime.text = timeInString
                    },
                    hour, minute, false)
            timePickerDialog.show()
        }

        //Fill in the end time for one of the views
        endTime.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this,
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        var hour = hourOfDay % 12
                        if (hour == 0) {
                            hour = 12
                        }
                        var amPm = "PM"
                        if (hourOfDay < 12) {
                            amPm = "AM"
                        }
                        val timeInString = String.format(Locale.getDefault(), "%02d:%02d%s", hour, minute, amPm)
                        endTime.text = timeInString
                    },
                    hour, minute, false)
            timePickerDialog.show()
        }

        // Submit the alarm and get input on whether or not it's a random alarm
        submitButton.setOnClickListener {
            if (!endTime.text.isBlank() && !beginTime.text.isBlank() && !alarmName.text.isBlank() && !categoryView.text.isBlank()) {
                val builder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                inflater.inflate(R.layout.custom_dialog, null)
                builder.setTitle("Is this a random alarm? If not I'll go by the beginning time you chose")
                builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which -> //If random
                    val intent = Intent(this, AlarmReceiver::class.java)
                    intent.putExtra("message", alarmName.text.toString())
                    intent.putExtra("categoryName", categoryView.text.toString())
                    val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    val begin = stringToTime(beginTime.text.toString())
                    val end = stringToTime(endTime.text.toString())
                    val randomTime = getRandomTime(begin, end)
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, randomTime.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                    alarmName.text = ""
                    beginTime.text = ""
                    endTime.text = ""
                })
                builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> //If not random
                    val intent = Intent(this, AlarmReceiver::class.java)
                    intent.putExtra("message", alarmName.text.toString())
                    intent.putExtra("categoryName", categoryView.text.toString())
                    val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    val time = beginTime.text.toString()
                    val calendar = stringToTime(time)
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                    alarmName.text = ""
                    beginTime.text = ""
                    endTime.text = ""
                    categoryView.text = ""
                })
                builder.create().show()

                // Create maps and arrays for a category and the alarms associated with it
                var map : MutableMap<String, ArrayList<String>> = Hawk.get("mapOfAlarms")
                val givenCategory = categoryView.text.toString()
                var categories = map.keys
                var categoryStats = Hawk.get<MutableMap<String, Array<Int>>>("categoryStats")
                // If the list of categories doesn't contain this cateogry, add it
                if (!categories.contains(givenCategory)) {
                    map.set(givenCategory, ArrayList<String>())
                    categoryStats.set(givenCategory, arrayOf(0, 0))
                    Hawk.put("categoryStats", categoryStats)
                }
                var listOfAlarms = map.get(givenCategory)
                if (!listOfAlarms!!.contains(alarmName.text.toString())) {
                    listOfAlarms!!.add(alarmName.text.toString())
                }
                map.set(givenCategory, listOfAlarms)
                Hawk.put("mapOfAlarms", map)
            }
        }
    }


    // Returns a random time between two calendar times
    private fun getRandomTime(begin: Calendar, end: Calendar): Calendar {
        val rnd = Random()
        val min = begin.timeInMillis
        val max = end.timeInMillis
        val randomNum = min + rnd.nextLong()%(max - min + 1)
        val res = Calendar.getInstance()
        res.timeInMillis = randomNum
        return res
    }

    // Returns calendar time given a string in this format: hh:mmaa
    private fun stringToTime(givenTime: String): Calendar {
        val time = givenTime
        val dateFormat = SimpleDateFormat("hh:mmaa")
        val alarmTime = dateFormat.parse(time)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.time = alarmTime
        return calendar
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
