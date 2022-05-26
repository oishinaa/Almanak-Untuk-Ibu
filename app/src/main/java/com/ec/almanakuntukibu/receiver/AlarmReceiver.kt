package com.ec.almanakuntukibu.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ec.almanakuntukibu.utils.NotificationUtils
import com.ec.almanakuntukibu.tracker.AudioTracker
import java.util.*
import kotlin.concurrent.timerTask

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AudioTracker.getMediaPlayerInstance().startAudio(context)

        val type = intent.getStringExtra("type")
        val text = intent.getStringExtra("text")
        val notificationUtils = NotificationUtils(context)
        val notification = notificationUtils.getNotificationBuilder(type!!, text!!).build()
        notificationUtils.getManager().notify(2, notification)

        Timer().schedule(timerTask { AudioTracker.getMediaPlayerInstance().stopAudio() }, 30 * 1000)
    }
}