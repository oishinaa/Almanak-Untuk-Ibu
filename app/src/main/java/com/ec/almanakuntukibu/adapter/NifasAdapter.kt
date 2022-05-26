package com.ec.almanakuntukibu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.model.VisitModel
import com.ec.almanakuntukibu.controller.nifas.NifasActivity

class NifasAdapter(context: NifasActivity, private var items: ArrayList<VisitModel>) :
    RecyclerView.Adapter<NifasAdapter.ViewHolder>() {

    private var context: Context = context

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
        holder.txtBlankHeaderSta.visibility = View.GONE
        holder.txtHeader.visibility = View.GONE
        holder.txtNo.text = no.toString()
        holder.txtNo.background = if (item.now) ContextCompat.getDrawable(context, R.drawable.bg_circle_pink) else ContextCompat.getDrawable(context, android.R.color.transparent)
        holder.txtNo.setTextColor(ContextCompat.getColor(context, if (item.now) R.color.ic_white else R.color.dark))
        holder.txtKeterangan.text = item.notes
        holder.txtKeterangan.visibility = if (item.notes == "") View.GONE else View.VISIBLE
        holder.txtLineHeaderSta.visibility = View.GONE
        holder.lnlLine.visibility = if (no % 7 == 0 && no != items.size) View.VISIBLE else View.GONE
        holder.txtLineHeaderEnd.visibility = View.GONE
        holder.lnlBlankEnd.visibility = if (position+1 == items.size) View.VISIBLE else View.GONE
        holder.txtBlankHeaderEnd.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var lnlBlankSta: LinearLayout = view.findViewById(R.id.lnlBlankSta)
        var txtBlankHeaderSta: TextView = view.findViewById(R.id.txtBlankHeaderSta)
        var txtHeader: TextView = view.findViewById(R.id.txtHeader)
        var txtNo: TextView = view.findViewById(R.id.txtNo)
        var txtKeterangan: TextView = view.findViewById(R.id.txtKeterangan)
        var txtLineHeaderSta: TextView = view.findViewById(R.id.txtLineHeaderSta)
        var lnlLine: LinearLayout = view.findViewById(R.id.lnlLine)
        var txtLineHeaderEnd: TextView = view.findViewById(R.id.txtLineHeaderEnd)
        var lnlBlankEnd: LinearLayout = view.findViewById(R.id.lnlBlankEnd)
        var txtBlankHeaderEnd: TextView = view.findViewById(R.id.txtBlankHeaderEnd)
    }
}