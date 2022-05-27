package com.ec.almanakuntukibu.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.controller.kehamilan.KehamilanActivity
import com.ec.almanakuntukibu.controller.siklus.SiklusActivity
import com.ec.almanakuntukibu.receiver.SnoozeReceiver

class NotificationUtils(base: Context): ContextWrapper(base) {
    private val channelId = "App Alert Notification ID"
    private val channelName = "App Alert Notification"
    private var manager: NotificationManager? = null
    private val flag = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }

    // Create channel for Android version 26+
    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannels() {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.enableVibration(true)
        getManager().createNotificationChannel(channel)
    }

    // Get Manager
    fun getManager(): NotificationManager {
        if (manager == null) manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager as NotificationManager
    }

    fun getNotificationBuilder(type: String, text: String): NotificationCompat.Builder {
        val intent: Intent
        val icon: Int
        if (type == "0") {
            intent = Intent(this, SiklusActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            icon = R.drawable.ic_water_drop
        } else {
            intent = Intent(this, KehamilanActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            icon = R.drawable.ic_pregnancy
        }

        val actionIntent = PendingIntent.getActivity(this, (1..2147483647).random(), intent, flag)
        val snoozeIntent = PendingIntent.getBroadcast(this, (1..2147483647).random(), Intent(this, SnoozeReceiver::class.java), flag)
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Alarm")
            .setContentText(text)
            .setSmallIcon(R.drawable.logo_tp_notext)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.logo_bg_notext))
            .addAction(icon, "Buka aplikasi", actionIntent)
            .addAction(icon, "Ingatkan besok", snoozeIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setSound(null)
    }
}