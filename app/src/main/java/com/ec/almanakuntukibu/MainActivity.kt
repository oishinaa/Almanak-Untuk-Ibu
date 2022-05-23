package com.ec.almanakuntukibu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ec.almanakuntukibu.tracker.ServiceTracker
import com.ec.almanakuntukibu.ui.kehamilan.HplActivity
import com.ec.almanakuntukibu.ui.nifas.HlActivity
import com.ec.almanakuntukibu.ui.siklus.SiklusActivity
import com.google.android.material.button.MaterialButton

class MainActivity: AppCompatActivity() {
    private lateinit var btnSiklus: MaterialButton
    private lateinit var btnKehamilan: MaterialButton
    private lateinit var btnMasaNifas: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar = supportActionBar
        actionBar!!.title = "Menu Utama"

        btnSiklus = findViewById(R.id.btnSiklus)
        btnKehamilan = findViewById(R.id.btnKehamilan)
        btnMasaNifas = findViewById(R.id.btnMasaNifas)

        btnSiklus.setOnClickListener {
            startActivity(Intent(this, SiklusActivity::class.java))
        }
        btnKehamilan.setOnClickListener {
            startActivity(Intent(this, HplActivity::class.java))
        }
        btnMasaNifas.setOnClickListener {
            startActivity(Intent(this, HlActivity::class.java))
        }
    }
}