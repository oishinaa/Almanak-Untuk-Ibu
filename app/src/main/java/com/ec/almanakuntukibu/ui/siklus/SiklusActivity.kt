package com.ec.almanakuntukibu.ui.siklus

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.GridView
import android.widget.TextView
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.adapter.CalendarAdapter
import com.ec.almanakuntukibu.model.CycleModel
import java.util.*
import kotlin.collections.ArrayList

class SiklusActivity: BaseActivity() {
    private lateinit var btnPrev: TextView
    private lateinit var txtHeader: TextView
    private lateinit var btnNext: TextView
    private lateinit var grdCalendar: GridView
    private lateinit var current: Calendar

    private val hdFormatter = { lMonths[getDatePart("MM", (current.time))-1] + " " + getDatePart("yyyy", (current.time)).toString() }
    private val db = DBHelper(this, null)

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
                if (action == "finish") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish"))

        /* val result = db.getCycles()
        if (result != null) {
            if (!result.moveToFirst()) {
                startActivity(Intent(this, SiklusFormActivity::class.java))
            }
        } */

        current = Calendar.getInstance()
        val date = intent.getStringExtra("current")?.let { dbFormatter.parse(it) }
        if (intent.getStringExtra("current") != null) {
            current.set(getDatePart("yyyy", date!!), getDatePart("MM", date)-1, 1)
        }

        btnPrev.setOnClickListener {
            current.add(Calendar.MONTH, -1)
            txtHeader.text = hdFormatter()
            updateCalendar()
        }
        txtHeader.text = hdFormatter()
        btnNext.setOnClickListener {
            current.add(Calendar.MONTH, 1)
            txtHeader.text = hdFormatter()
            updateCalendar()
        }

        updateCalendar()
    }

    @SuppressLint("Range")
    private fun updateCalendar() {
        val tempDate = Calendar.getInstance()
        tempDate.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), 1)
        tempDate.add(Calendar.DATE, if (tempDate.get(Calendar.DAY_OF_WEEK) == 1) -6 else -(tempDate.get(Calendar.DAY_OF_WEEK)-2))

        val cells: ArrayList<Date> = ArrayList()
        while (cells.size < 42) {
            cells.add(tempDate.time)
            tempDate.add(Calendar.DATE, 1)
        }

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

        grdCalendar.adapter = CalendarAdapter(this, cells, current, cycles)
    }
}