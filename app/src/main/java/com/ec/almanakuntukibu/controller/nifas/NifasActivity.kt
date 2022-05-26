package com.ec.almanakuntukibu.controller.nifas

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ec.almanakuntukibu.BaseActivity
import com.ec.almanakuntukibu.R
import com.ec.almanakuntukibu.adapter.NifasAdapter
import com.ec.almanakuntukibu.model.VisitModel

class NifasActivity: BaseActivity() {
    private lateinit var rcvKunjungan: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nifas)

        val actionBar = supportActionBar
        actionBar!!.title = "Masa Nifas"
        actionBar.setDisplayHomeAsUpEnabled(true)

        rcvKunjungan = findViewById(R.id.rcvKunjungan)

        loadKunjungan()
    }

    private fun loadKunjungan() {
        rcvKunjungan.layoutManager = LinearLayoutManager(this)
        val listKunjungan = ArrayList<VisitModel>()
        for (i in 1 until 43) {
            val kunjunganModel = VisitModel()
            kunjunganModel.now = i == 6
            if (i == 6 || i == 16 || i == 20 || i == 24 || i == 33 || i == 42) {
                kunjunganModel.notes = "Sen, 7 Agu 2022 14:30"
            }
            listKunjungan.add(kunjunganModel)
        }
        rcvKunjungan.adapter = NifasAdapter(this, listKunjungan)
    }
}