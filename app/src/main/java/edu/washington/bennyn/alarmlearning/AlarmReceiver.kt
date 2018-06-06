package edu.washington.bennyn.alarmlearning

import android.app.Notification
import android.app.PendingIntent
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
        val message = intent!!.getStringExtra("message")

        val noButton = Intent(context, UpdateReceiver::class.java)
        noButton.putExtra("answer", false)
        val noIntent = PendingIntent.getBroadcast(context, 0, noButton, PendingIntent.FLAG_UPDATE_CURRENT)

        val yesButton = Intent(context, UpdateReceiver::class.java)
        yesButton.putExtra("answer", true)
        val yesIntent = PendingIntent.getBroadcast(context, 1, yesButton, PendingIntent.FLAG_UPDATE_CURRENT)

        val mBuilder = NotificationCompat.Builder(context) //works on API 26 and lower
                .setSmallIcon(R.drawable.clock)
                .setContentTitle("AlarmLearning")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.alarm_off, "ignore", noIntent)
                .addAction(R.drawable.alarm_on, "finished", yesIntent)
        val notificationMngr = NotificationManagerCompat.from(context!!)
        notificationMngr.notify(0, mBuilder.build())
    }
}

// Updates whether or not the user did the action from the alarm
class UpdateReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        NotificationManagerCompat.from(context!!).cancel(0) //cancel the notification after button press
        val answer = intent!!.getBooleanExtra("answer", true)
        Log.e("answerReceived", answer.toString())

        Hawk.init(context).build()
        val tasksDone = Hawk.get<Int>("tasksDone")
        val tasksTotal = Hawk.get<Int>("tasksTotal")

        if (answer) {
            Hawk.put("tasksDone", tasksDone + 1)
        }

        Hawk.put("tasksTotal", tasksTotal + 1)
        Log.e("tasksDone", tasksDone.toString())
        Log.e("tasksTotal", tasksTotal.toString())
    }
}