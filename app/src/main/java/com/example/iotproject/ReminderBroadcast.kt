package com.example.iotproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcast: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        sendDrinkingReminder(context);
    }

    private fun sendDrinkingReminder(context: Context?) {
       var builder = context?.let {
            NotificationCompat.Builder(it, Control.CHANNEL_ID)
                .setContentTitle(context?.resources?.getString(R.string.notif_title))
                .setContentText(context?.resources?.getString(R.string.notif_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_baseline_local_drink_24)
                .setWhen(System.currentTimeMillis() + Control.NOTIFICATION_DELAY)
        };

        with(context?.let { NotificationManagerCompat.from(it) }) {
            builder?.build()?.let { this?.notify(1, it) }
        }
    }
}