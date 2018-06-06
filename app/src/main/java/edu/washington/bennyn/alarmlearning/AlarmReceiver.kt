package edu.washington.bennyn.alarmlearning

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.orhanobut.hawk.Hawk

// Pop up a dialog whether or not you did your task
class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("CheckIntent", "got it!")
        val messsge = intent!!.getStringExtra("message")
        val mBuilder = NotificationCompat.Builder(context) //works on API 26 and lower
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("AlarmLearning")
                .setContentText(messsge)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationMngr = NotificationManagerCompat.from(context!!)
        notificationMngr.notify(1, mBuilder.build())
    }
}