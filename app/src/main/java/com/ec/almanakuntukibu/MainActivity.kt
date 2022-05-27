package com.ec.almanakuntukibu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.ec.almanakuntukibu.tracker.ServiceTracker
import com.ec.almanakuntukibu.controller.kehamilan.HplActivity
import com.ec.almanakuntukibu.controller.nifas.HlActivity
import com.ec.almanakuntukibu.controller.siklus.SiklusActivity
import com.google.android.material.button.MaterialButton

class MainActivity: BaseActivity() {
    private lateinit var btnSiklus: MaterialButton
    private lateinit var btnKehamilan: MaterialButton
    private lateinit var btnMasaNifas: MaterialButton
    private var db = DBHelper(this, null)

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadBtn()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar = supportActionBar
        actionBar!!.title = "Menu Utama"

        btnSiklus = findViewById(R.id.btnSiklus)
        btnKehamilan = findViewById(R.id.btnKehamilan)
        btnMasaNifas = findViewById(R.id.btnMasaNifas)

        loadBtn()
    }

    @SuppressLint("Range")
    private fun loadBtn() {
        btnSiklus.setOnClickListener {
            startActivity(Intent(this, SiklusActivity::class.java))
        }
        btnKehamilan.setOnClickListener {
            startForResult.launch(Intent(this, HplActivity::class.java))
        }
        btnMasaNifas.setOnClickListener {
            startForResult.launch(Intent(this, HlActivity::class.java))
        }

        val result = db.getUser()
        if (result != null) {
            if (result.moveToFirst()) {
                if (result.getInt(result.getColumnIndex(DBHelper.user_hpl)) == 0 && result.getInt(result.getColumnIndex(DBHelper.user_hl)) == 0) {
                    btnKehamilan.setBackgroundColor(ContextCompat.getColor(this, R.color.pink))
                    btnMasaNifas.setBackgroundColor(ContextCompat.getColor(this, R.color.pink))
                } else if (result.getInt(result.getColumnIndex(DBHelper.user_hpl)) != 0) {
                    btnMasaNifas.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))
                    btnMasaNifas.setOnClickListener {
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("Pemantauan kehamilan sedang berlangsung, anda dapat memantau masa nifas ketika kehamilan sudah selesai.")
                        builder.setPositiveButton("Ok") { _,_ -> }
                        builder.show()
                    }
                } else if (result.getInt(result.getColumnIndex(DBHelper.user_hl)) != 0) {
                    btnKehamilan.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))
                    btnKehamilan.setOnClickListener { }
                }
            }
        }
    }
}