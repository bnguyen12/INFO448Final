package edu.washington.bennyn.alarmlearning

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log

// Pop up a dialog whether or not you did your task
class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("CheckIntent", "got it!")
        showNotification(context)
    }

    private fun showNotification(context: Context?) {
        
    }
}