package com.ec.almanakuntukibu.controller.kehamilan

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import java.util.*

class HplFormActivity: BaseActivity() {
    private lateinit var btnSudahTauHpl: Button
    private lateinit var btnBelumTauHpl: Button
    private lateinit var lnlHpl: LinearLayout
    private lateinit var lnlHpht: LinearLayout
    private lateinit var txtHpl: TextView
    private lateinit var txtHpht: TextView
    private lateinit var btn: Button
    private var date = Calendar.getInstance()

    private val db = DBHelper(this, null)

    private val onHplSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        date.set(year, month, day)
        txtHpl.text = dpFormatter(date.time)
        btn.visibility = View.VISIBLE
    }

    private val onHphtSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        date.set(year, month, day)
        txtHpht.text = dpFormatter(date.time)
        btn.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hpl_form)

        val actionBar = supportActionBar
        actionBar!!.title = "Kehamilan"
        actionBar.setDisplayHomeAsUpEnabled(true)

        btnSudahTauHpl = findViewById(R.id.btnSudahTauHpl)
        btnBelumTauHpl = findViewById(R.id.btnBelumTauHpl)
        lnlHpl = findViewById(R.id.lnlHpl)
        lnlHpht = findViewById(R.id.lnlHpht)
        txtHpl = findViewById(R.id.txtHpl)
        txtHpht = findViewById(R.id.txtHpht)
        btn = findViewById(R.id.btn)

        loadHpht()

        btnSudahTauHpl.setOnClickListener { toggleBtnSudah() }
        btnBelumTauHpl.setOnClickListener { toggleBtnBelum() }
        lnlHpl.setOnClickListener { showDatePickerDialog(onHplSetListener, null) }
        lnlHpht.setOnClickListener { showDatePickerDialog(onHphtSetListener, date) }
        btn.setOnClickListener{ submit() }
    }

    @SuppressLint("Range")
    private fun loadHpht() {
        val result = db.getLastCycle()
        if (result != null) {
            if (result.moveToFirst()) {
                val tempDate = dbFormatter.parse(result.getInt(result.getColumnIndex(DBHelper.cycle_sta)).toString())
                date.set(getDatePart("yyyy", tempDate!!), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate))
                txtHpht.text = dpFormatter(date.time)
            }
        }
    }

    private fun toggleBtnSudah() {
        lnlHpht.visibility = View.GONE
        if (lnlHpl.visibility == View.VISIBLE) {
            lnlHpl.visibility = View.GONE
            btn.visibility = View.GONE
        } else {
            lnlHpl.visibility = View.VISIBLE
            btn.visibility = if (txtHpl.text.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun toggleBtnBelum() {
        lnlHpl.visibility = View.GONE
        if (lnlHpht.visibility == View.VISIBLE) {
            lnlHpht.visibility = View.GONE
            btn.visibility = View.GONE
        } else {
            lnlHpht.visibility = View.VISIBLE
            btn.visibility = if (txtHpht.text.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun submit() {
        if (lnlHpht.visibility == View.VISIBLE) {
            date.add(Calendar.DATE, 7)
            date.add(Calendar.MONTH, 9)
        }
        val result = db.getUser()
        if (result != null) {
            if (!result.moveToFirst()) db.addUser("hpl", dbFormatter.format(date.time).toInt())
            else db.updUser("hpl", dbFormatter.format(date.time).toInt())
        }
        addVisits()
    }

    private fun addVisits() {
        val weeks = arrayOf(13, 17, 21, 25, 31, 37)
        date.add(Calendar.MONTH, -9)
        date.add(Calendar.DATE, -7)
        for (w in weeks) {
            val visitDate = Calendar.getInstance()
            visitDate.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE))
            visitDate.add(Calendar.DATE, (w-1)*7)
            db.addVisit(1, dbFormatter.format(visitDate.time).toInt(), "07:00", "", 0)
        }
        finish()
    }
}