package com.ec.almanakuntukibu.ui.kehamilan

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ec.almanakuntukibu.receiver.AlarmReceiver
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.adapter.KehamilanAdapter
import com.ec.almanakuntukibu.model.VisitModel
import java.util.*

class KehamilanActivity: BaseActivity() {
    private lateinit var rcvKunjungan: RecyclerView
    private lateinit var btn: Button
    private lateinit var btnAdd: ImageView
    private lateinit var txtPassword: EditText
    private lateinit var imgShow: ImageView
    private lateinit var imgHide: ImageView
    private lateinit var lnlDate: LinearLayout
    private lateinit var txtDate: TextView
    private lateinit var txtNotes: EditText
    private lateinit var hpht: Calendar
    private lateinit var date: Calendar
    private var _year = 0
    private var _month = 0
    private var _day = 0

    private val db = DBHelper(this, null)

    private val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        date.set(_year, _month, _day, hour, minute)
        txtDate.text = dtFormatter(date.time)
    }

    private val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        _year = year
        _month = month
        _day = day
        showTimePickerDialog(onTimeSetListener)
    }

    @SuppressLint("Range")
    private val calculateWeek = { date: Date ->
        ((((kotlin.math.abs(date.time - hpht.timeInMillis)) / (24 * 60 * 60 * 1000)) / 7) + 1).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kehamilan)

        val actionBar = supportActionBar
        actionBar!!.title = "Kehamilan"
        actionBar.setDisplayHomeAsUpEnabled(true)

        rcvKunjungan = findViewById(R.id.rcvKunjungan)
        btn = findViewById(R.id.btn)
        btnAdd = findViewById(R.id.btnAdd)

        val broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish"))

        loadKunjungan()

        btn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Jika pemantauan kehamilan sudah selesai maka alarm & data kunjungan kehamilan akan dihapus. Selesaikan?")
            builder.setPositiveButton("Oke") { _, _ -> db.updUser("hpl", 0); finish() }
            builder.setNegativeButton("Batal") { _, _ -> }
            builder.show()
        }
        btnAdd.setOnClickListener {
            val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_isi_password, null)
            txtPassword = dialogLayout.findViewById(R.id.txtPassword)
            imgShow = dialogLayout.findViewById(R.id.imgShow)
            imgHide = dialogLayout.findViewById(R.id.imgHide)
            txtPassword.transformationMethod = PasswordTransformationMethod.getInstance()

            imgShow.setOnClickListener {
                txtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imgHide.visibility = View.VISIBLE
                imgShow.visibility = View.GONE
            }
            imgHide.setOnClickListener {
                txtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                imgShow.visibility = View.VISIBLE
                imgHide.visibility = View.GONE
            }

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Oke") { _,_ -> submitPassword(txtPassword.text.toString()) }
            builder.setNegativeButton("Batal") { _,_ -> }
            builder.show()
        }
    }

    @SuppressLint("Range")
    private fun loadKunjungan() {
        rcvKunjungan.layoutManager = LinearLayoutManager(this)
        loadHpht()
        val result = db.getVisits(1)
        val visits = ArrayList<VisitModel>()
        var count = 0
        for (i in 1 until 46) {
            val visit = VisitModel()
            visit.now = i == calculateWeek(Date())
            if (result != null) {
                if (result.moveToFirst()) {
                    do {
                        visit.id = result.getInt(result.getColumnIndex(DBHelper.visit_id))
                        visit.date = result.getInt(result.getColumnIndex(DBHelper.visit_date))
                        visit.time = result.getString(result.getColumnIndex(DBHelper.visit_time))
                        visit.notes = result.getString(result.getColumnIndex(DBHelper.visit_notes))
                        visit.status = result.getInt(result.getColumnIndex(DBHelper.visit_status))
                        val tempDate = dbFormatter.parse(visit.date.toString())
                        val tempTime = tmFormatter.parse(visit.time)
                        val notes = visit.notes
                        if (i == calculateWeek(tempDate!!)) {
                            val clnd = Calendar.getInstance()
                            clnd.set(getDatePart("yyyy", tempDate), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate), getDatePart("HH", tempTime!!), getDatePart("mm", tempTime))
                            val text = dtFormatter(clnd.time)

                            count++
                            visit.desc = "K$count | $text | $notes"
                            break
                        }
                    } while (result.moveToNext())
                }
            }
            visits.add(visit)
        }
        rcvKunjungan.adapter = KehamilanAdapter(this, visits)
    }

    private fun submitPassword(text: String) {
        if (text == "bidannganjuk") {
            val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_buat_alarm, null)
            lnlDate = dialogLayout.findViewById(R.id.lnlDate)
            txtDate = dialogLayout.findViewById(R.id.txtDate)
            txtNotes = dialogLayout.findViewById(R.id.txtNotes)
            date = Calendar.getInstance()
            txtDate.text = dtFormatter(date.time)

            lnlDate.setOnClickListener { showDatePickerDialog(onDateSetListener) }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Tambah Pengingat Kunjungan")
            builder.setView(dialogLayout)
            builder.setPositiveButton("Oke") { _,_ -> db.addVisit(1, dbFormatter.format(date.time).toInt(), tmFormatter.format(date.time), txtNotes.text.toString(), 0); restartActivity() }
            builder.setNegativeButton("Batal") { _,_ -> }
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Password salah!")
            builder.setPositiveButton("Oke") { _,_ -> }
            builder.show()
        }
    }

    private fun setAlarm(date: Calendar) {
        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, (0..2147483647).random(), intent, PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.timeInMillis, pendingIntent)
        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show()
    }

    private fun restartActivity() {
        this.sendBroadcast(Intent("finish"))
        this.startActivity(Intent(this, KehamilanActivity::class.java))
    }

    @SuppressLint("Range")
    private fun loadHpht() {
        val result = db.getUser()
        if (result != null) {
            if (result.moveToFirst() && result.getInt(result.getColumnIndex(DBHelper.user_hpl)) != 0) {
                val tempDate = dbFormatter.parse(result.getInt(result.getColumnIndex(DBHelper.user_hpl)).toString())
                hpht = Calendar.getInstance()
                hpht.set(getDatePart("yyyy", tempDate!!), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate))
                hpht.add(Calendar.MONTH, -9)
                hpht.add(Calendar.DATE, -7)
            }
        }
    }
}