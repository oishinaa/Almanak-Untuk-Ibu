package com.ec.almanakuntukibu.controller.kehamilan

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import java.text.SimpleDateFormat
import java.util.*

class HplActivity: BaseActivity() {
    private lateinit var btn: Button
    private lateinit var txtMonth: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtYear: TextView
    private lateinit var txtInfo: TextView

    private val db = DBHelper(this, null)

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadHpl(2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hpl)

        val actionBar = supportActionBar
        actionBar!!.title = "Kehamilan"
        actionBar.setDisplayHomeAsUpEnabled(true)

        btn = findViewById(R.id.btn)
        txtMonth = findViewById(R.id.txtMonth)
        txtDate = findViewById(R.id.txtDate)
        txtYear = findViewById(R.id.txtYear)
        txtInfo = findViewById(R.id.txtInfo)

        val broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish hpl") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish hpl"))

        loadHpl(1)

        btn.setOnClickListener { startActivity(Intent(this, KehamilanActivity::class.java)) }
    }

    @SuppressLint("Range")
    private fun loadHpl(type: Int) {
        val result = db.getUser()
        if (result != null) {
            if (result.moveToFirst() && result.getInt(result.getColumnIndex(DBHelper.user_hpl)) != 0) {
                val date = dbFormatter.parse(result.getInt(result.getColumnIndex(DBHelper.user_hpl)).toString())
                txtMonth.text = sMonths[getDatePart("MM", date!!)-1]
                txtDate.text = SimpleDateFormat("dd", Locale.UK).format(date)
                txtYear.text = SimpleDateFormat("yyyy", Locale.UK).format(date)

                val hpht = Calendar.getInstance()
                hpht.set(getDatePart("yyyy", date), getDatePart("MM", date)-1, getDatePart("dd", date), 0, 0, 0)
                hpht.add(Calendar.MONTH, -9)
                hpht.add(Calendar.DATE, -7)

                val dateDiff = (Date().time/1000 - hpht.timeInMillis/1000) / (24 * 60 * 60)
                val week = ((dateDiff / 7) + 1).toString()
                val info = "Sekarang anda berada di minggu ke-$week kehamilan"
                txtInfo.text = info
            } else {
                if (type == 1)
                    startForResult.launch(Intent(this, HplFormActivity::class.java))
                else {
                    finish()
                }
            }
        }
    }
}