package com.ec.almanakuntukibu.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.model.CycleModel
import com.ec.almanakuntukibu.controller.siklus.SiklusActivity
import com.ec.almanakuntukibu.utils.AlarmUtils
import java.text.SimpleDateFormat
import java.util.*


class CalendarAdapter(context: Context, days: ArrayList<Date>, private val current: Calendar, private val cycles: ArrayList<CycleModel>) :
    ArrayAdapter<Date>(context, R.layout.calendar_day, days) {

    private lateinit var linearLayout: View
    private lateinit var textView: TextView
    private lateinit var lnlSta: LinearLayout
    private lateinit var lnlEnd: LinearLayout
    private lateinit var txtSta: TextView
    private lateinit var txtEnd: TextView
    private var sta = Calendar.getInstance()
    private var end = Calendar.getInstance()
    private var alarm = 0

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val getDatePart = { pattern: String, date: Date -> SimpleDateFormat(pattern, Locale.UK).format(date).toInt() }
    private val lMonths = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    private val dpFormatter = { date: Date -> getDatePart("dd", date).toString() + " " + lMonths[getDatePart("MM", date)-1] + " " + getDatePart("yyyy", date) }
    private val dbFormatter = SimpleDateFormat("yyyyMMdd", Locale.UK)
    private val db = DBHelper(context, null)

    private val onStaSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        sta.set(year, month, day)
        txtSta.text = dpFormatter(sta.time)
    }

    private val onEndSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        end.set(year, month, day)
        txtEnd.text = dpFormatter(end.time)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val date = getItem(position)
        linearLayout = view ?: inflater.inflate(R.layout.calendar_day, parent, false)
        textView = linearLayout.findViewById(R.id.txtDate)

        if (!date!!.after(Date())) {
            val background = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, background, true)
            linearLayout.setBackgroundResource(background.resourceId)
            linearLayout.setOnClickListener {
                addCycleDialog(date)
            }
        } else {
            linearLayout.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Hanya bisa menambah siklus pada tanggal yang sudah berlalu dan tanggal hari ini.")
                builder.setPositiveButton("Ok") { _,_ -> }
                builder.show()
            }
        }

        loadCycles(date)

        textView.text = getDatePart("dd", date).toString()
        if (getDatePart("MM", date)-1 != current.get(Calendar.MONTH)) textView.alpha = 0.5F
        if (getDatePart("yyyyMMdd", date) == getDatePart("yyyyMMdd", Date())) textView.setTypeface(null, Typeface.BOLD)

        return linearLayout
    }

    @SuppressLint("Range")
    private fun loadCycles(date: Date) {
        if (alarm == 0) AlarmUtils(context).unsetAlarm()

        var count = 0
        var totalDiff = 0
        var prevCycle = Date()
        for ((i, cycle) in cycles.withIndex()) {
            val dateSta = dbFormatter.parse(cycle.sta.toString())
            val dateEnd = dbFormatter.parse(cycle.end.toString())
            val clndSta = Calendar.getInstance()
            val clndEnd = Calendar.getInstance()
            clndSta.set(getDatePart("yyyy", dateSta!!), getDatePart("MM", dateSta)-1, getDatePart("dd", dateSta))
            clndEnd.set(getDatePart("yyyy", dateEnd!!), getDatePart("MM", dateEnd)-1, getDatePart("dd", dateEnd))
            clndSta.add(Calendar.DATE, -1)
            clndEnd.add(Calendar.DATE, 1)

            if (cycle.sta <= dbFormatter.format(date).toInt() && cycle.end >= dbFormatter.format(date).toInt()) {
                textView.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_pink)
                textView.setTextColor(ContextCompat.getColor(context, R.color.ic_white))
                linearLayout.background = ContextCompat.getDrawable(context, android.R.color.transparent)
                linearLayout.setOnClickListener {
                    updCycleDialog(cycle.id, dbFormatter.parse(cycle.sta.toString())!!, dbFormatter.parse(cycle.end.toString())!!)
                }
            } else if (getDatePart("yyyyMMdd", clndSta.time) == getDatePart("yyyyMMdd", date) || getDatePart("yyyyMMdd", clndEnd.time) == getDatePart("yyyyMMdd", date)) {
                linearLayout.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Tidak dapat menambah siklus pada tanggal ini, terlalu dekat dengan siklus lainnya.")
                    builder.setPositiveButton("Ok") { _,_ -> }
                    builder.show()
                }
            }

            var avgDiff = 28
            if (i > 0 && i > cycles.size-3 && cycles.size-3 >= 0) {
                count++
                totalDiff += (kotlin.math.abs(dateSta.time - prevCycle.time) / (24 * 60 * 60 * 1000)).toInt()
                avgDiff = totalDiff / count
            }
            prevCycle = dateSta

            if (i+1 == cycles.size) {
                val nextCycle = Calendar.getInstance()
                nextCycle.set(getDatePart("yyyy", dateSta), getDatePart("MM", dateSta)-1, getDatePart("dd", dateSta), 7, 0)
                nextCycle.add(Calendar.DATE, avgDiff)
                if (alarm == 0) {
                    AlarmUtils(context).setAlarm(nextCycle, "0", "Jangan lupa untuk mendata siklus berikutnya. :)")
                    alarm = 1
                }
                if (dbFormatter.format(date) == dbFormatter.format(nextCycle.time)) {
                    textView.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_grey)
                    textView.setTextColor(ContextCompat.getColor(context, R.color.ic_white))
                    linearLayout.background = ContextCompat.getDrawable(context, android.R.color.transparent)
                }
            }
        }
    }

    private fun addCycleDialog(date: Date) {
        val dialogLayout = inflater.inflate(R.layout.dialog_pilih_tanggal, null)
        lnlSta = dialogLayout.findViewById(R.id.lnlSta)
        lnlEnd = dialogLayout.findViewById(R.id.lnlEnd)
        txtSta = dialogLayout.findViewById(R.id.txtSta)
        txtEnd = dialogLayout.findViewById(R.id.txtEnd)
        txtSta.text = dpFormatter(date)

        lnlSta.setOnClickListener { showDatePickerDialog(onStaSetListener, "sta") }
        sta.set(getDatePart("yyyy", date), getDatePart("MM", date)-1, getDatePart("dd", date))
        lnlEnd.visibility = View.GONE
        end.set(getDatePart("yyyy", date), getDatePart("MM", date)-1, getDatePart("dd", date))
        end.add(Calendar.DATE, 5)
        val tempEnd = Calendar.getInstance()
        tempEnd.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DATE))
        tempEnd.add(Calendar.DATE, 1)
        for (cycle in cycles) {
            if (cycle.sta <= dbFormatter.format(tempEnd.time).toInt() && cycle.sta >= dbFormatter.format(sta.time).toInt()) {
                val cycleSta = dbFormatter.parse(cycle.sta.toString())
                end.set(getDatePart("yyyy", cycleSta!!), getDatePart("MM", cycleSta)-1, getDatePart("dd", cycleSta))
                end.add(Calendar.DATE, -2)
            }
        }
        // txtEnd.text = dpFormatter.format(end.time)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tambah Siklus Menstruasi")
        builder.setView(dialogLayout)
        builder.setPositiveButton("Oke") { _,_ -> db.addCycle(dbFormatter.format(sta.time).toInt(), dbFormatter.format(end.time).toInt()); restartActivity() }
        builder.setNegativeButton("Batal") { _,_ -> }
        builder.show()
    }

    private fun updCycleDialog(cycleId: Int, cycleSta: Date, cycleEnd: Date) {
        val dialogLayout = inflater.inflate(R.layout.dialog_pilih_tanggal, null)
        lnlSta = dialogLayout.findViewById(R.id.lnlSta)
        lnlEnd = dialogLayout.findViewById(R.id.lnlEnd)
        txtSta = dialogLayout.findViewById(R.id.txtSta)
        txtEnd = dialogLayout.findViewById(R.id.txtEnd)
        txtSta.text = dpFormatter(cycleSta)
        txtEnd.text = dpFormatter(cycleEnd)

        lnlSta.setOnClickListener { showDatePickerDialog(onStaSetListener, "sta") }
        sta.set(getDatePart("yyyy", cycleSta), getDatePart("MM", cycleSta)-1, getDatePart("dd", cycleSta))
        lnlEnd.setOnClickListener { showDatePickerDialog(onEndSetListener, "end") }
        end.set(getDatePart("yyyy", cycleEnd), getDatePart("MM", cycleEnd)-1, getDatePart("dd", cycleEnd))

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ubah Siklus Menstruasi")
        builder.setView(dialogLayout)
        builder.setPositiveButton("Oke") { _,_ -> db.updCycle(cycleId, dbFormatter.format(sta.time).toInt(), dbFormatter.format(end.time).toInt()); restartActivity() }
        builder.setNeutralButton("Hapus") { _,_ -> db.delCycle(cycleId); restartActivity() }
        builder.setNegativeButton("Batal") { _,_ -> }
        builder.show()
    }

    private fun restartActivity() {
        context.sendBroadcast(Intent("finish sm"))
        val intent = Intent(context, SiklusActivity::class.java)
        intent.putExtra("current", dbFormatter.format(current.time))
        context.startActivity(intent)
    }

    private fun showDatePickerDialog(onDateSetListener: DatePickerDialog.OnDateSetListener, type: String) {
        val c = if (type == "sta") sta else end
        val datePickerDialog = DatePickerDialog(context, onDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE))
        if (type == "sta") {
            for (cycle in cycles) {
                if (cycle.end < dbFormatter.format(sta.time).toInt()) {
                    val cycleEnd = dbFormatter.parse(cycle.end.toString())
                    val minDate = Calendar.getInstance()
                    minDate.set(getDatePart("yyyy", cycleEnd!!), getDatePart("MM", cycleEnd) - 1, getDatePart("dd", cycleEnd))
                    minDate.add(Calendar.DATE, 2)
                    datePickerDialog.datePicker.minDate = minDate.timeInMillis
                } else {
                    break
                }
            }
            datePickerDialog.datePicker.maxDate = end.timeInMillis
        } else {
            datePickerDialog.datePicker.minDate = sta.timeInMillis
            for (cycle in cycles) {
                if (cycle.sta > dbFormatter.format(end.time).toInt()) {
                    val cycleSta = dbFormatter.parse(cycle.sta.toString())
                    val maxDate = Calendar.getInstance()
                    maxDate.set(getDatePart("yyyy", cycleSta!!), getDatePart("MM", cycleSta) - 1, getDatePart("dd", cycleSta))
                    maxDate.add(Calendar.DATE, -2)
                    datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
                    break
                }
            }
        }
        datePickerDialog.show()
    }
}