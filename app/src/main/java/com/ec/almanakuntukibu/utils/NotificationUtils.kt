package com.ec.almanakuntukibu.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.MainActivity
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.controller.kehamilan.KehamilanActivity
import com.ec.almanakuntukibu.controller.nifas.NifasActivity
import com.ec.almanakuntukibu.controller.siklus.SiklusActivity
import com.ec.almanakuntukibu.model.CycleModel
import com.ec.almanakuntukibu.receiver.SnoozeReceiver
import java.util.*

class NotificationUtils(base: Context): ContextWrapper(base) {
    private val channelId = "App Alert Notification ID"
    private val channelName = "App Alert Notification"
    private val db = DBHelper(this, null)
    private val flag = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

    private var manager: NotificationManager? = null

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

    @SuppressLint("Range")
    private fun getNextCycle(): Calendar {
        val cycles: ArrayList<CycleModel> = ArrayList(0)
        val result = db.getCycles()
        if (result != null) {
            if (result.moveToFirst()) {
                do {
                    val cycle = CycleModel()
                    cycle.id = result.getInt(result.getColumnIndex(DBHelper.cycle_id))
                    cycle.sta = result.getInt(result.getColumnIndex(DBHelper.cycle_sta))
                    cycle.end = result.getInt(result.getColumnIndex(DBHelper.cycle_end))
                    cycles.add(cycle)
                } while (result.moveToNext())
            }
        }

        val nextCycle = Calendar.getInstance()
        var count = 0
        var totalDiff = 0
        var prevCycle = Date()
        for ((i, cycle) in cycles.withIndex()) {
            val dateSta = DateUtils().dbFormatter.parse(cycle.sta.toString())

            var avgDiff = 28
            if (i > 0 && i > cycles.size-3 && cycles.size-3 >= 0) {
                count++
                totalDiff += ((dateSta!!.time/1000 - prevCycle.time/1000) / (24 * 60 * 60)).toInt()
                avgDiff = totalDiff / count
            }
            prevCycle = dateSta!!

            if (i+1 == cycles.size) {
                nextCycle.set(DateUtils().getDatePart("yyyy", dateSta), DateUtils().getDatePart("MM", dateSta)-1, DateUtils().getDatePart("dd", dateSta), 0, 0, 0)
                nextCycle.add(Calendar.DATE, avgDiff)
            }
        }

        return nextCycle
    }

    @SuppressLint("Range")
    fun getInfoNotifBuilder(): Notification {
        var alarm = ""
        var result = db.getAlarm()
        if (result != null) {
            if (result.moveToFirst()) {
                val alarmDate = result.getInt(result.getColumnIndex(DBHelper.alarm_date)).toString()
                val alarmTime = result.getString(result.getColumnIndex(DBHelper.alarm_time))

                if (alarmDate != "0") {
                    val tempDate = DateUtils().dbFormatter.parse(alarmDate)
                    val tempTime = DateUtils().tmFormatter.parse(alarmTime)

                    val clnd = Calendar.getInstance()
                    clnd.set(DateUtils().getDatePart("yyyy", tempDate!!), DateUtils().getDatePart("MM", tempDate)-1, DateUtils().getDatePart("dd", tempDate),
                        DateUtils().getDatePart("HH", tempTime!!), DateUtils().getDatePart("mm", tempTime), 0)
                    alarm = DateUtils().dtFormatter(clnd.time)
                }
            }
        }

        var title = if (alarm == "") "Alarm Belum Terpasang" else "Info"
        var content = if (alarm == "") "Silahkan isi data siklus menstruasi anda." else "Alarm berikutnya pada $alarm."
        var message = ""

        result = db.getUser()
        if (result != null) {
            if (result.moveToFirst()) {
                if (result.getInt(result.getColumnIndex(DBHelper.user_hpl)) != 0) {
                    if (alarm != "") title = "Info Kunjungan Kehamilan"
                    else content = "Mohon lengkapi data kunjungan kehamilan anda."
                } else if (result.getInt(result.getColumnIndex(DBHelper.user_hl)) != 0) {
                    if (alarm != "") title = "Info Kunjungan Masa Nifas"
                    else content = "Mohon lengkapi data kunjungan masa nifas anda."
                }
            }
        }

        if (title == "Info") {
            val nextCycle = getNextCycle()
            val differenceInSeconds = Calendar.getInstance().timeInMillis/1000 - nextCycle.timeInMillis/1000
            val secondsInADay = 24 * 60 * 60
            val differenceInDay = differenceInSeconds / secondsInADay

            if (differenceInDay >= 14) {
                message = "Alarm berikutnya pada $alarm.\nHarap periksakan diri ke puskesmas terdekat, haid anda sudah telat $differenceInDay hari dari perkiraan."
            }
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT }.let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        }

        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(message)

        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(if (message != "") bigText else null)
            .setSmallIcon(R.drawable.logo_tp_grey)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.logo_bg))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    fun getAlarmNotifBuilder(type: String, text: String): NotificationCompat.Builder {
        val intent: Intent
        val icon: Int
        var content = text
        var message = ""
        if (type == "1") {
            intent = Intent(this, SiklusActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            icon = R.drawable.ic_water_drop

            val nextCycle = getNextCycle()
            val differenceInSeconds = Calendar.getInstance().timeInMillis/1000 - nextCycle.timeInMillis/1000
            val secondsInADay = 24 * 60 * 60
            val differenceInDay = differenceInSeconds / secondsInADay

            if (differenceInDay >= 14) {
                content = "Harap periksakan diri ke puskesmas terdekat..."
                message = "Harap periksakan diri ke puskesmas terdekat, haid anda sudah telat $differenceInDay hari dari perkiraan."
            }
        } else if (type == "2") {
            intent = Intent(this, KehamilanActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            icon = R.drawable.ic_pregnancy
        } else if (type == "3") {
            intent = Intent(this, NifasActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            icon = R.drawable.ic_baby
        } else {
            intent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            icon = R.drawable.logo_tp_grey
        }

        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(message)

        val actionIntent = PendingIntent.getActivity(this, (1..2147483647).random(), intent, flag)
        val snoozeIntent = PendingIntent.getBroadcast(this, (1..2147483647).random(), Intent(this, SnoozeReceiver::class.java), flag)
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Alarm")
            .setContentText(content)
            .setStyle(if (message != "") bigText else null)
            .setSmallIcon(R.drawable.logo_tp_grey)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.logo_bg))
            .addAction(icon, "Buka aplikasi", actionIntent)
            .addAction(icon, "Ingatkan besok", snoozeIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
    }
}