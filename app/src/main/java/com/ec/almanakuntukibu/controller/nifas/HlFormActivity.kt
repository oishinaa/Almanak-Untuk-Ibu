package com.ec.almanakuntukibu.controller.nifas

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

class HlFormActivity: BaseActivity() {
    private lateinit var lnlHl: LinearLayout
    private lateinit var txtHl: TextView
    private lateinit var btn: Button
    private var date = Calendar.getInstance()

    private val db = DBHelper(this, null)

    private val onHlSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        date.set(year, month, day)
        txtHl.text = dpFormatter(date.time)
        btn.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hl_form)

        val actionBar = supportActionBar
        actionBar!!.title = "Masa Nifas"
        actionBar.setDisplayHomeAsUpEnabled(true)

        lnlHl = findViewById(R.id.lnlHl)
        txtHl = findViewById(R.id.txtHl)
        btn = findViewById(R.id.btn)

        lnlHl.setOnClickListener { showDatePickerDialog(onHlSetListener, null) }
        btn.setOnClickListener{ submit() }
    }

    private fun submit() {
        val result = db.getUser()
        if (result != null) {
            if (!result.moveToFirst()) db.addUser("hl", dbFormatter.format(date.time).toInt())
            else db.updUser("hl", dbFormatter.format(date.time).toInt())
        }
        finish()
    }
}