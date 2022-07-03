package com.ec.almanakuntukibu.controller.nifas

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.adapter.NifasAdapter
import com.ec.almanakuntukibu.model.VisitModel
import com.ec.almanakuntukibu.tracker.AudioTracker
import com.ec.almanakuntukibu.utils.AlarmUtils
import com.ec.almanakuntukibu.utils.NotificationUtils
import java.util.*

class NifasActivity: BaseActivity() {
    private lateinit var rcvKunjungan: RecyclerView
    private lateinit var btn: Button
    private lateinit var btnAdd: ImageView
    private lateinit var edtPassword: EditText
    private lateinit var imgShow: ImageView
    private lateinit var imgHide: ImageView
    private lateinit var lnlDate: LinearLayout
    private lateinit var txtDate: TextView
    private lateinit var edtNotes: EditText
    private lateinit var chkStatus: CheckBox
    private lateinit var hl: Calendar
    private lateinit var _date: Calendar
    private var _year = 0
    private var _month = 0
    private var _day = 0

    private val db = DBHelper(this, null)

    private val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        _date.set(_year, _month, _day, hour, minute, 0)
        txtDate.text = dtFormatter(_date.time)
    }

    private val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        _year = year
        _month = month
        _day = day
        showTimePickerDialog(onTimeSetListener)
    }

    @SuppressLint("Range")
    private val calculateDay = { date: Date ->
        val differenceInSeconds = date.time/1000 - hl.timeInMillis/1000
        val secondsInADay = 24 * 60 * 60
        val differenceInDay = differenceInSeconds / secondsInADay
        (differenceInDay + 1).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nifas)

        val actionBar = supportActionBar
        actionBar!!.title = "Masa Nifas"
        actionBar.setDisplayHomeAsUpEnabled(true)

        rcvKunjungan = findViewById(R.id.rcvKunjungan)
        btn = findViewById(R.id.btn)
        btnAdd = findViewById(R.id.btnAdd)

        val broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(arg0: Context, intent: Intent) {
                val action = intent.action
                if (action == "finish km") {
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish km"))

        AudioTracker.getMediaPlayerInstance().stopAudio()
        NotificationUtils(this).getManager().cancel(2)

        loadKunjungan()

        btn.setOnClickListener {
            val dialogLayout =
                LayoutInflater.from(this).inflate(R.layout.dialog_isi_password, null)
            edtPassword = dialogLayout.findViewById(R.id.edtPassword)
            imgShow = dialogLayout.findViewById(R.id.imgShow)
            imgHide = dialogLayout.findViewById(R.id.imgHide)
            edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()

            imgShow.setOnClickListener {
                edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imgHide.visibility = View.VISIBLE
                imgShow.visibility = View.GONE
            }
            imgHide.setOnClickListener {
                edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                imgShow.visibility = View.VISIBLE
                imgHide.visibility = View.GONE
            }

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Oke") { _, _ -> finishMonitoring() }
            builder.setNegativeButton("Batal") { _, _ -> }
            builder.show()
        }
        btnAdd.setOnClickListener {
            val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_isi_password, null)
            edtPassword = dialogLayout.findViewById(R.id.edtPassword)
            imgShow = dialogLayout.findViewById(R.id.imgShow)
            imgHide = dialogLayout.findViewById(R.id.imgHide)
            edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()

            imgShow.setOnClickListener {
                edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imgHide.visibility = View.VISIBLE
                imgShow.visibility = View.GONE
            }
            imgHide.setOnClickListener {
                edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                imgShow.visibility = View.VISIBLE
                imgHide.visibility = View.GONE
            }

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Oke") { _,_ -> submitPassword(edtPassword.text.toString()) }
            builder.setNegativeButton("Batal") { _,_ -> }
            builder.show()
        }
    }

    @SuppressLint("Range")
    private fun loadHl() {
        val result = db.getUser()
        if (result != null) {
            if (result.moveToFirst() && result.getInt(result.getColumnIndex(DBHelper.user_hl)) != 0) {
                val tempDate = dbFormatter.parse(result.getInt(result.getColumnIndex(DBHelper.user_hl)).toString())
                hl = Calendar.getInstance()
                hl.set(getDatePart("yyyy", tempDate!!), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate), 0, 0, 0)
            }
        }
    }

    @SuppressLint("Range")
    private fun loadKunjungan() {
        rcvKunjungan.layoutManager = LinearLayoutManager(this)
        loadHl()
        val result = db.getVisits(2)
        val visits = ArrayList<VisitModel>()
        var count = 0
        val alarmTime = Calendar.getInstance()
        var alarmVisit = 0
        var alarmSet = false
        for (i in 1 until 43) {
            val visit = VisitModel()
            visit.now = i == calculateDay(Date())
            if (result != null) {
                if (result.moveToFirst()) {
                    do {
                        visit.id = result.getInt(result.getColumnIndex(DBHelper.visit_id))
                        visit.date = result.getInt(result.getColumnIndex(DBHelper.visit_date))
                        visit.time = result.getString(result.getColumnIndex(DBHelper.visit_time))
                        visit.notes = result.getString(result.getColumnIndex(DBHelper.visit_notes))
                        visit.status = result.getInt(result.getColumnIndex(DBHelper.visit_status)) != 0
                        val tempDate = dbFormatter.parse(visit.date.toString())
                        val tempTime = tmFormatter.parse(visit.time)
                        val notes = if (visit.notes.isNotEmpty()) "\n" + visit.notes else ""
                        if (i == calculateDay(tempDate!!)) {
                            val clnd = Calendar.getInstance()
                            clnd.set(getDatePart("yyyy", tempDate), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate), getDatePart("HH", tempTime!!), getDatePart("mm", tempTime), 0)
                            val text = dtFormatter(clnd.time)

                            if (!visit.status && clnd.timeInMillis > Date().time && !alarmSet) {
                                alarmTime.set(getDatePart("yyyy", tempDate), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate), getDatePart("HH", tempTime), getDatePart("mm", tempTime), 0)
                                alarmVisit = count+1
                                alarmSet = true
                            }

                            count++
                            visit.desc = "$text$notes"
                            break
                        }
                    } while (result.moveToNext())
                }
            }
            visits.add(visit)
        }
        if (alarmSet) AlarmUtils(this).setAlarm(alarmTime, "3", "Hari ini ada jadwal kunjungan masa nifas ke-$alarmVisit!")
        else AlarmUtils(this).unsetAlarm()
        rcvKunjungan.adapter = NifasAdapter(this, visits, hl)
    }

    private fun submitPassword(text: String) {
        if (text == "nganjukbangkit") {
            val max = Calendar.getInstance()
            max.set(hl.get(Calendar.YEAR), hl.get(Calendar.MONTH), hl.get(Calendar.DATE), 0, 0, 0)
            max.add(Calendar.DATE, 41)

            val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_buat_alarm, null)
            lnlDate = dialogLayout.findViewById(R.id.lnlDate)
            txtDate = dialogLayout.findViewById(R.id.txtDate)
            edtNotes = dialogLayout.findViewById(R.id.edtNotes)
            chkStatus = dialogLayout.findViewById(R.id.chkStatus)
            lnlDate.setOnClickListener { showDatePickerDialog(onDateSetListener, null, hl, max) }

            _date = Calendar.getInstance()
            txtDate.text = dtFormatter(_date.time)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Tambah Pengingat Kunjungan")
            builder.setView(dialogLayout)
            builder.setPositiveButton("Oke") { _,_ -> db.addVisit(2, dbFormatter.format(_date.time).toInt(), tmFormatter.format(_date.time), edtNotes.text.toString(), if (chkStatus.isChecked) 1 else 0); restartActivity() }
            builder.setNegativeButton("Batal") { _,_ -> }
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Password salah!")
            builder.setPositiveButton("Oke") { _,_ -> }
            builder.show()
        }
    }

    private fun finishMonitoring() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Jika pemantauan masa nifas sudah selesai maka alarm & data kunjungan masa nifas akan dihapus. Selesaikan?")
        builder.setPositiveButton("Oke") { _, _ -> db.updUser("hl", 0); AlarmUtils(this).unsetAlarm(); db.delAllVisits(2); finish(); sendBroadcast(Intent("finish hl")) }
        builder.setNegativeButton("Batal") { _, _ -> }
        builder.show()
    }

    private fun restartActivity() {
        sendBroadcast(Intent("finish km"))
        startActivity(Intent(this, NifasActivity::class.java))
    }
}