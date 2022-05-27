package com.ec.almanakuntukibu.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.widget.Toast
import com.ec.almanakuntukibu.*
import com.ec.almanakuntukibu.enum.State
import com.ec.almanakuntukibu.tracker.ServiceTracker
import java.text.SimpleDateFormat
import java.util.*

class EndlessService: Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private val getDatePart = { pattern: String, date: Date -> SimpleDateFormat(pattern, Locale.UK).format(date).toInt() }
    private val sMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    private val dbFormatter = SimpleDateFormat("yyyyMMdd", Locale.UK)
    private val dtFormatter = { date: Date -> dpFormatter(date) + " " + tmFormatter.format(date) }
    private val tmFormatter = SimpleDateFormat("HH:mm", Locale.UK)
    private val dpFormatter = { date: Date -> getDatePart("dd", date).toString() + " " + sMonths[getDatePart("MM", date)-1] + " " + getDatePart("yyyy", date) }

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
        val notification = createNotification()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    private fun startService() {
        if (isServiceStarted) return
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Toast.makeText(this, "" + alarmManager.nextAlarmClock?.toString(), Toast.LENGTH_SHORT).show()
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
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {

        }
        isServiceStarted = false
        ServiceTracker().setServiceState(this, State.STOPPED.name)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, EndlessService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
    }

    @SuppressLint("Range")
    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent,if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        var alarm = ""
        val db = DBHelper(this, null)
        val result = db.getAlarm()
        if (result != null) {
            if (result.moveToFirst()) {
                val tempDate = dbFormatter.parse(result.getInt(result.getColumnIndex(DBHelper.alarm_date)).toString())
                val tempTime = tmFormatter.parse(result.getString(result.getColumnIndex(DBHelper.alarm_time)))

                val clnd = Calendar.getInstance()
                clnd.set(getDatePart("yyyy", tempDate!!), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate), getDatePart("HH", tempTime!!), getDatePart("mm", tempTime))
                alarm = dtFormatter(clnd.time)
            }
        }

        return builder
            .setContentTitle("Info")
            .setContentText("Alarm berikutnya akan berbunyi pada $alarm")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}