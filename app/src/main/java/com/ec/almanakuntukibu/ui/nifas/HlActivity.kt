package com.ec.almanakuntukibu.ui.nifas

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import java.text.SimpleDateFormat
import java.util.*

class HlActivity: BaseActivity() {
    private lateinit var btn: Button
    private lateinit var txtMonth: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtYear: TextView
    private lateinit var txtInfo: TextView

    private val db = DBHelper(this, null)

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadHl(2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hl)

        val actionBar = supportActionBar
        actionBar!!.title = "Masa Nifas"
        actionBar.setDisplayHomeAsUpEnabled(true)

        btn = findViewById(R.id.btn)
        txtMonth = findViewById(R.id.txtMonth)
        txtDate = findViewById(R.id.txtDate)
        txtYear = findViewById(R.id.txtYear)
        txtInfo = findViewById(R.id.txtInfo)

        loadHl(1)

        btn.setOnClickListener { startForResult.launch(Intent(this, NifasActivity::class.java)) }
    }

    @SuppressLint("Range")
    private fun loadHl(type: Int) {
        val result = db.getUser()
        if (result != null) {
            if (result.moveToFirst() && result.getInt(result.getColumnIndex(DBHelper.user_hl)) != 0) {
                val date = dbFormatter.parse(result.getInt(result.getColumnIndex(DBHelper.user_hl)).toString())
                txtMonth.text = sMonths[getDatePart("MM", date!!)-1]
                txtDate.text = SimpleDateFormat("dd", Locale.UK).format(date)
                txtYear.text = SimpleDateFormat("yyyy", Locale.UK).format(date)

                val hpht = Calendar.getInstance()
                hpht.set(getDatePart("yyyy", date), getDatePart("MM", date)-1, getDatePart("dd", date))
                hpht.add(Calendar.MONTH, -9)
                hpht.add(Calendar.DATE, -7)

                val dateDiff = kotlin.math.abs(Date().time - hpht.timeInMillis) / (24 * 60 * 60 * 1000)
                val day = dateDiff.toString()
                val info = "Sekarang anda berada di hari ke-$day masa nifas"
                txtInfo.text = info
            } else {
                if (type == 1)
                    startForResult.launch(Intent(this, HlFormActivity::class.java))
                else {
                    finish()
                }
            }
        }
    }
}