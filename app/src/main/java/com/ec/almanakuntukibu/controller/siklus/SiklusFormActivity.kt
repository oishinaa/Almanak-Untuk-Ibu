package com.ec.almanakuntukibu.controller.siklus

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.R
import java.util.*

class SiklusFormActivity: BaseActivity() {
    private lateinit var lnlTl: LinearLayout
    private lateinit var lnlHpht: LinearLayout
    private lateinit var txtTl: TextView
    private lateinit var txtHpht: TextView
    private var date = Calendar.getInstance()

    private val onTlSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        date.set(year, month, day)
        txtTl.text = dpFormatter(date.time)
    }
    private val onHphtSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        date.set(year, month, day)
        txtHpht.text = dpFormatter(date.time)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_siklus_form)

        lnlTl = findViewById(R.id.lnlTl)
        lnlHpht = findViewById(R.id.lnlHpht)
        txtTl = findViewById(R.id.txtTl)
        txtHpht = findViewById(R.id.txtHpht)

        val actionBar = supportActionBar
        actionBar!!.title = "Data Pengguna"
        actionBar.setDisplayHomeAsUpEnabled(true)

        lnlTl.setOnClickListener {
            showDatePickerDialog(onTlSetListener, null)
        }
        lnlHpht.setOnClickListener {
            showDatePickerDialog(onHphtSetListener, null)
        }
    }
}