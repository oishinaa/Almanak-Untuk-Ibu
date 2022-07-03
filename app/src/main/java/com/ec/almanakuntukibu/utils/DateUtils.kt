package com.ec.almanakuntukibu.utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    val getDatePart = { pattern: String, date: Date -> SimpleDateFormat(pattern, Locale.UK).format(date).toInt() }
    val sMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    val dbFormatter = SimpleDateFormat("yyyyMMdd", Locale.UK)
    val dtFormatter = { date: Date -> dpFormatter(date) + " " + tmFormatter.format(date) }
    val tmFormatter = SimpleDateFormat("HH:mm", Locale.UK)
    val dpFormatter = { date: Date -> getDatePart("dd", date).toString() + " " + sMonths[getDatePart("MM", date)-1] + " " + getDatePart("yyyy", date) }
}