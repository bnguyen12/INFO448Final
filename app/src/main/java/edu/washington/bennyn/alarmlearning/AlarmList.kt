package edu.washington.bennyn.alarmlearning

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.orhanobut.hawk.Hawk

class AlarmList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_list)

        Hawk.init(this).build() //use Hawk to store all sorts of things easy in memory for later use
        val settings = getSharedPreferences("preferencesFile", 0)
        if (settings.getBoolean("firstCheck", true)) {
            Log.d("firstCheck", "This app is being opened for the first time")
            Hawk.put("tasksDone", 0)
            settings.edit().putBoolean("firstCheck", false).apply()
        } else {
            Log.d("firstCheck", "This app has data already present")
        }
    }
}
