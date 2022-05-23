package com.ec.almanakuntukibu.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ec.almanakuntukibu.NotificationUtils

abstract class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationUtils = NotificationUtils(context)
        val notification = notificationUtils.getNotificationBuilder(intent.getStringExtra("text")!!).build()
        notificationUtils.getManager().notify(150, notification)
    }
}