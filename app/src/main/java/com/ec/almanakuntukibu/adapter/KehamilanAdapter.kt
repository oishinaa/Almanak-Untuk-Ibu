package com.ec.almanakuntukibu.adapter

import android.app.*
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ec.almanakuntukibu.DBHelper
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.model.VisitModel
import com.ec.almanakuntukibu.controller.kehamilan.KehamilanActivity
import java.text.SimpleDateFormat
import java.util.*

class KehamilanAdapter(context: KehamilanActivity, private var items: ArrayList<VisitModel>, private var hpht: Calendar) :
    RecyclerView.Adapter<KehamilanAdapter.ViewHolder>() {

    private lateinit var edtPassword: EditText
    private lateinit var imgShow: ImageView
    private lateinit var imgHide: ImageView
    private lateinit var lnlDate: LinearLayout
    private lateinit var txtDate: TextView
    private lateinit var edtNotes: EditText
    private lateinit var chkStatus: CheckBox
    private lateinit var date: Calendar
    private var _year = 0
    private var _month = 0
    private var _day = 0
    private var context: Context = context

    private val lMonths = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    private val dbFormatter = SimpleDateFormat("yyyyMMdd", Locale.UK)
    private val tmFormatter = SimpleDateFormat("HH:mm", Locale.UK)

    val getDatePart = { pattern: String, date: Date -> SimpleDateFormat(pattern, Locale.UK).format(date).toInt() }
    val dpFormatter = { date: Date -> getDatePart("dd", date).toString() + " " + lMonths[getDatePart("MM", date)-1] + " " + getDatePart("yyyy", date) }
    val dtFormatter = { date: Date -> dpFormatter(date) + " " + tmFormatter.format(date) }

    private val db = DBHelper(context, null)

    private val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        date.set(_year, _month, _day, hour, minute, 0)
        txtDate.text = dtFormatter(date.time)
    }

    private val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        _year = year
        _month = month
        _day = day
        showTimePickerDialog(onTimeSetListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.item_kunjungan, parent, false)
        return ViewHolder(v as LinearLayout)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item: VisitModel = items[position]
        val no = position + 1
        holder.lnlBlankSta.visibility = if (position == 0) View.VISIBLE else View.GONE
        holder.txtHeader.text = if (no == 1) "Trimester I" else if (no == 14) "Trimester II" else if (no == 27) "Trimester III" else ""
        holder.txtNo.text = no.toString()
        holder.txtNo.background = ContextCompat.getDrawable(context, if (item.now) R.drawable.bg_circle_pink else android.R.color.transparent)
        holder.txtNo.setTextColor(ContextCompat.getColor(context, if (item.now) R.color.ic_white else R.color.dark))
        holder.txtKeterangan.text = item.desc
        holder.txtKeterangan.visibility = if (item.desc == "") View.GONE else View.VISIBLE
        holder.lnlLine.visibility = if (no == 13 || no == 26) View.VISIBLE else View.GONE
        holder.lnlBlankEnd.visibility = if (position+1 == items.size) View.VISIBLE else View.GONE

        if (item.desc != "") {
            holder.txtKeterangan.setTextColor(ContextCompat.getColor(context, if (item.status) R.color.ic_white else R.color.dark))
            holder.txtKeterangan.background = ContextCompat.getDrawable(context, if (item.status) R.drawable.bg_corner_pink else R.drawable.bg_corner_grey)
            holder.txtKeterangan.setOnClickListener {
                val dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_isi_password, null)
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

                val builder = AlertDialog.Builder(context)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Oke") { _,_ -> submitPassword(edtPassword.text.toString(), item.id, item.date, item.time, item.notes, item.status) }
                builder.setNegativeButton("Batal") { _,_ -> }
                builder.show()
            }
        }
    }

    private fun submitPassword(text: String, id: Int, visit_date: Int, visit_time: String, visit_notes: String, visit_status: Boolean) {
        if (text == "nganjukbangkit") {
            val tempDate = dbFormatter.parse(visit_date.toString())
            val tempTime = tmFormatter.parse(visit_time)
            date = Calendar.getInstance()
            date.set(getDatePart("yyyy", tempDate!!), getDatePart("MM", tempDate)-1, getDatePart("dd", tempDate), getDatePart("HH", tempTime!!), getDatePart("mm", tempTime), 0)

            val dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_buat_alarm, null)
            lnlDate = dialogLayout.findViewById(R.id.lnlDate)
            txtDate = dialogLayout.findViewById(R.id.txtDate)
            edtNotes = dialogLayout.findViewById(R.id.edtNotes)
            chkStatus = dialogLayout.findViewById(R.id.chkStatus)
            lnlDate.setOnClickListener { showDatePickerDialog(onDateSetListener) }

            txtDate.text = dtFormatter(date.time)
            edtNotes.text = Editable.Factory.getInstance().newEditable(visit_notes)
            chkStatus.isChecked = visit_status

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Ubah Pengingat Kunjungan")
            builder.setView(dialogLayout)
            builder.setPositiveButton("Oke") { _,_ -> db.updVisit(id,1, dbFormatter.format(date.time).toInt(), tmFormatter.format(date.time), edtNotes.text.toString(), if (chkStatus.isChecked) 1 else 0); restartActivity() }
            builder.setNegativeButton("Batal") { _,_ -> }
            builder.setNeutralButton("Hapus") { _,_ -> db.delVisit(id); restartActivity() }
            builder.show()
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Password salah!")
            builder.setPositiveButton("Oke") { _,_ -> }
            builder.show()
        }
    }

    private fun restartActivity() {
        context.sendBroadcast(Intent("finish kk"))
        context.startActivity(Intent(context, KehamilanActivity::class.java))
    }

    private fun showDatePickerDialog(onDateSetListener: DatePickerDialog.OnDateSetListener) {
        val datePickerDialog = DatePickerDialog(context, onDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.minDate = hpht.timeInMillis
        val max = Calendar.getInstance()
        max.set(hpht.get(Calendar.YEAR), hpht.get(Calendar.MONTH), hpht.get(Calendar.DATE), 0, 0, 0)
        max.add(Calendar.DATE, 7*45-1)
        datePickerDialog.datePicker.maxDate = max.timeInMillis
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(onTimeSetListener: TimePickerDialog.OnTimeSetListener) {
        val timePickerDialog = TimePickerDialog(context, onTimeSetListener, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true)
        timePickerDialog.show()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var lnlBlankSta: LinearLayout = view.findViewById(R.id.lnlBlankSta)
        var txtHeader: TextView = view.findViewById(R.id.txtHeader)
        var txtNo: TextView = view.findViewById(R.id.txtNo)
        var txtKeterangan: TextView = view.findViewById(R.id.txtKeterangan)
        var lnlLine: LinearLayout = view.findViewById(R.id.lnlLine)
        var lnlBlankEnd: LinearLayout = view.findViewById(R.id.lnlBlankEnd)
    }
}