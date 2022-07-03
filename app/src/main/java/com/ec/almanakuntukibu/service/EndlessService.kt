package com.ec.almanakuntukibu.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import com.ec.almanakuntukibu.enum.State
import com.ec.almanakuntukibu.tracker.ServiceTracker
import com.ec.almanakuntukibu.utils.NotificationUtils

class EndlessService: Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                "start" -> startService()
                "stop" -> stopService()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationUtils(this).getInfoNotifBuilder()
        startForeground(1, notification)
    }

    private fun startService() {
        if (isServiceStarted) {
            val notification = NotificationUtils(this).getInfoNotifBuilder()
            startForeground(1, notification)
            return
        }
        // Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        ServiceTracker().setServiceState(this, State.STARTED.name)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                acquire(10*60*1000L /*10 minutes*/)
            }
        }
    }

    private fun stopService() {
        // Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) { }
        isServiceStarted = false
        ServiceTracker().setServiceState(this, State.STOPPED.name)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val notification = NotificationUtils(this).getInfoNotifBuilder()
        startForeground(1, notification)
    }
}