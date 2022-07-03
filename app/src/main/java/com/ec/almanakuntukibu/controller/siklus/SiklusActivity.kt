package com.ec.almanakuntukibu.controller.siklus

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.adapter.SiklusAdapter
import com.ec.almanakuntukibu.model.CycleModel
import com.ec.almanakuntukibu.tracker.AudioTracker
import com.ec.almanakuntukibu.utils.NotificationUtils
import java.util.*
import kotlin.collections.ArrayList

class SiklusActivity: BaseActivity() {
    private lateinit var btnPrev: TextView
    private lateinit var txtHeader: TextView
    private lateinit var btnNext: TextView
    private lateinit var grdCalendar: GridView
    private lateinit var lnlSta: LinearLayout
    private lateinit var lnlEnd: LinearLayout
    private lateinit var txtSta: TextView
    private lateinit var txtEnd: TextView
    private lateinit var current: Calendar
    private var sta = Calendar.getInstance()
    private var end = Calendar.getInstance()

    private val hdFormatter = { lMonths[getDatePart("MM", (current.time))-1] + " " + getDatePart("yyyy", (current.time)).toString() }
    private val db = DBHelper(this, null)

    private val onStaSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        sta.set(year, month, day, 0, 0, 0)
        txtSta.text = dpFormatter(sta.time)
    }

    private val onEndSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        end.set(year, month, day, 0, 0, 0)
        txtEnd.text = dpFormatter(end.time)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_siklus)

        val actionBar = supportActionBar
        actionBar!!.title = "Siklus Menstruasi"
        actionBar.setDisplayHomeAsUpEnabled(true)

        btnPrev = findViewById(R.id.btnPrev)
        txtHeader = findViewById(R.id.txtHeader)
        btnNext = findViewById(R.id.btnNext)
        grdCalendar = findViewById(R.id.grdCalendar)

        val broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish sm") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish sm"))

        current = Calendar.getInstance()
        val date = intent.getStringExtra("current")?.let { dbFormatter.parse(it) }
        if (intent.getStringExtra("current") != null) {
            current.set(getDatePart("yyyy", date!!), getDatePart("MM", date)-1, 1, 0, 0, 0)
        }

        btnPrev.setOnClickListener {
            current.add(Calendar.MONTH, -1)
            txtHeader.text = hdFormatter()
            updateCalendar(1)
        }
        txtHeader.text = hdFormatter()
        btnNext.setOnClickListener {
            current.add(Calendar.MONTH, 1)
            txtHeader.text = hdFormatter()
            updateCalendar(1)
        }

        AudioTracker.getMediaPlayerInstance().stopAudio()
        NotificationUtils(this).getManager().cancel(2)
        updateCalendar(0)
    }

    @SuppressLint("Range")
    private fun updateCalendar(type: Int) {
        val tempDate = Calendar.getInstance()
        tempDate.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), 1, 0, 0, 0)
        tempDate.add(Calendar.DATE, if (tempDate.get(Calendar.DAY_OF_WEEK) == 1) -6 else -(tempDate.get(Calendar.DAY_OF_WEEK)-2))

        val cells: ArrayList<Date> = ArrayList()
        while (cells.size < 42) {
            cells.add(tempDate.time)
            tempDate.add(Calendar.DATE, 1)
        }

        val user = db.getUser()
        val res = if (user != null && user.moveToFirst()) user.getInt(user.getColumnIndex(DBHelper.user_res)) else 0
        val isPreg = if (user != null && user.moveToFirst()) user.getInt(user.getColumnIndex(DBHelper.user_hpl)) != 0 || user.getInt(user.getColumnIndex(DBHelper.user_hl)) != 0 else false

        val cycles: ArrayList<CycleModel> = ArrayList(0)
        var totalCyclesAfterRes = 0
        val result = db.getCycles()
        if (result != null) {
            if (result.moveToFirst()) {
                do {
                    val cycle = CycleModel()
                    cycle.id = result.getInt(result.getColumnIndex(DBHelper.cycle_id))
                    cycle.sta = result.getInt(result.getColumnIndex(DBHelper.cycle_sta))
                    cycle.end = result.getInt(result.getColumnIndex(DBHelper.cycle_end))
                    cycles.add(cycle)

                    if (res == 0 || cycle.sta > res) {
                        totalCyclesAfterRes++
                    }
                } while (result.moveToNext())
            } else if (type == 0) {
                addCycleDialog(Date())
            }
        }

        grdCalendar.adapter = SiklusAdapter(this, cells, current, cycles, totalCyclesAfterRes, isPreg)
    }

    private fun addCycleDialog(date: Date) {
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_pilih_tanggal, null)
        lnlSta = dialogLayout.findViewById(R.id.lnlSta)
        lnlEnd = dialogLayout.findViewById(R.id.lnlEnd)
        txtSta = dialogLayout.findViewById(R.id.txtSta)
        txtEnd = dialogLayout.findViewById(R.id.txtEnd)
        txtSta.text = dpFormatter(date)

        lnlSta.setOnClickListener { showDatePickerDialog(onStaSetListener, sta, null, null) }
        sta.set(getDatePart("yyyy", date), getDatePart("MM", date)-1, getDatePart("dd", date), 0, 0, 0)
        lnlEnd.setOnClickListener { showDatePickerDialog(onEndSetListener, end, null, null) }
        end.set(getDatePart("yyyy", date), getDatePart("MM", date)-1, getDatePart("dd", date), 0, 0, 0)
        end.add(Calendar.DATE, 5)
        txtEnd.text = dpFormatter(end.time)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tambah Siklus Menstruasi")
        builder.setView(dialogLayout)
        builder.setPositiveButton("Oke") { _,_ -> db.addCycle(dbFormatter.format(sta.time).toInt(), dbFormatter.format(end.time).toInt()); restartActivity(); }
        builder.setNegativeButton("Batal") { _,_ -> }
        builder.show()
    }

    private fun restartActivity() {
        finish()
        val intent = Intent(this, SiklusActivity::class.java)
        intent.putExtra("current", dbFormatter.format(sta.time))
        startActivity(intent)
    }
}