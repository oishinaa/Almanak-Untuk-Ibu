package com.ec.almanakuntukibu.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ec.almanakuntukibu.utils.NotificationUtils
import com.ec.almanakuntukibu.tracker.AudioTracker
import com.ec.almanakuntukibu.tracker.ServiceTracker
import java.util.*
import kotlin.concurrent.timerTask

class SnoozeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AudioTracker.getMediaPlayerInstance().stopAudio()
        NotificationUtils(context).getManager().cancel(2)
        ServiceTracker().actionOnService(context, "start")
    }
}