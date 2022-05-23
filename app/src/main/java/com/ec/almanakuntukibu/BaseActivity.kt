package com.ec.almanakuntukibu

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

open class BaseActivity: AppCompatActivity() {
    val sMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    val lMonths = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dbFormatter = SimpleDateFormat("yyyyMMdd", Locale.UK)
    val tmFormatter = SimpleDateFormat("HH:mm", Locale.UK)

    val getDatePart = { pattern: String, date: Date -> SimpleDateFormat(pattern, Locale.UK).format(date).toInt() }
    val dpFormatter = { date: Date -> getDatePart("dd", date).toString() + " " + lMonths[getDatePart("MM", date)-1] + " " + getDatePart("yyyy", date) }
    val dtFormatter = { date: Date -> dpFormatter(date) + " " + tmFormatter.format(date) }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showDatePickerDialog(onDateSetListener: DatePickerDialog.OnDateSetListener) {
        val c = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, onDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    fun showTimePickerDialog(onTimeSetListener: TimePickerDialog.OnTimeSetListener) {
        val c = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(this, onTimeSetListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
        timePickerDialog.show()
    }
}