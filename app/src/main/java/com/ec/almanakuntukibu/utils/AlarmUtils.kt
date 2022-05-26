package com.ec.almanakuntukibu.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.ec.almanakuntukibu.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class AlarmUtils(context: Context): ContextWrapper(context) {
    private val sMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    private val tmFormatter = SimpleDateFormat("HH:mm", Locale.UK)
    private val getDatePart = { pattern: String, date: Date -> SimpleDateFormat(pattern, Locale.UK).format(date).toInt() }
    private val dpFormatter = { date: Date -> getDatePart("dd", date).toString() + " " + sMonths[getDatePart("MM", date)-1] + " " + getDatePart("yyyy", date) }
    private val dtFormatter = { date: Date -> dpFormatter(date) + " " + tmFormatter.format(date) }
    private val flag = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

    fun setAlarm(date: Calendar, type: String, text: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("type", type)
        intent.putExtra("text", text)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flag)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, date.timeInMillis, 60*1000, pendingIntent)
        Toast.makeText(this, "Alarm akan berbunyi pada " + dtFormatter(date.time), Toast.LENGTH_LONG).show()
    }

    fun unsetAlarm() {
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flag)
        alarmManager.cancel(pendingIntent)
    }
}