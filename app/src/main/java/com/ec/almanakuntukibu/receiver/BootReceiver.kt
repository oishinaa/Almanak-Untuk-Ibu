package com.ec.almanakuntukibu.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ec.almanakuntukibu.tracker.ServiceTracker
import com.ec.almanakuntukibu.enum.State
import com.ec.almanakuntukibu.service.EndlessService

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && ServiceTracker().getServiceState(context) == State.STARTED) {
            Intent(context, EndlessService::class.java).also {
                it.action = "start"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                    return
                }
                context.startService(it)
            }
        }
    }
}