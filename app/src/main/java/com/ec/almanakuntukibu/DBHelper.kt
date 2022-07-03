package com.ec.almanakuntukibu

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME = "Almanak"
        private const val DATABASE_VERSION = 1

        const val user = "user"
        const val user_id = "id"
        const val user_hpl = "hpl"
        const val user_hl = "hl"
        const val user_res = "res"

        const val cycles = "cycles"
        const val cycle_id = "id"
        const val cycle_sta = "sta"
        const val cycle_end = "end"

        const val visits = "visits"
        const val visit_id = "id"
        const val visit_type = "type"
        const val visit_date = "date"
        const val visit_time = "time"
        const val visit_notes = "notes"
        const val visit_status = "status"

        const val alarm = "alarm"
        const val alarm_id = "id"
        const val alarm_date = "date"
        const val alarm_time = "time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $user")
        val qUser = ("CREATE TABLE $user (" +
            "$user_id INTEGER PRIMARY KEY, " +
            "$user_hpl INTEGER, " +
            "$user_hl INTEGER, " +
            "$user_res INTEGER" + ")")
        db.execSQL(qUser)

        db.execSQL("DROP TABLE IF EXISTS $cycles")
        val qCycles = ("CREATE TABLE $cycles (" +
            "$cycle_id INTEGER PRIMARY KEY, " +
            "$cycle_sta INTEGER, " +
            "$cycle_end INTEGER" + ")")
        db.execSQL(qCycles)

        db.execSQL("DROP TABLE IF EXISTS $visits")
        val qVisits = ("CREATE TABLE $visits (" +
            "$visit_id INTEGER PRIMARY KEY, " +
            "$visit_type INTEGER, " +
            "$visit_date INTEGER, " +
            "$visit_time TEXT, " +
            "$visit_notes TEXT, " +
            "$visit_status INTEGER" + ")")
        db.execSQL(qVisits)

        db.execSQL("DROP TABLE IF EXISTS $alarm")
        val qAlarm = ("CREATE TABLE $alarm (" +
            "$alarm_id INTEGER PRIMARY KEY, " +
            "$alarm_date INTEGER, " +
            "$alarm_time TEXT" + ")")
        db.execSQL(qAlarm)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS $user")
        db.execSQL("DROP TABLE IF EXISTS $cycles")
        db.execSQL("DROP TABLE IF EXISTS $visits")
        onCreate(db)
    }

    fun getUser(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $user", null)
    }

    fun addUser(col: String, value: Int) {
        val db = this.readableDatabase
        val values = ContentValues()
        values.put(user_hpl, 0)
        values.put(user_hl, 0)
        values.put(user_res, 0)
        values.put(if (col == "hpl") user_hpl else if (col == "hl") user_hl else user_res, value)
        db.insert(user, null, values)
        db.close()
    }

    fun updUser(col: String, value: Int) {
        val db = this.readableDatabase
        val values = ContentValues()
        values.put(if (col == "hpl") user_hpl else if (col == "hl") user_hl else user_res, value)
        db.update(user, values, "$user_id = ?", Array(1) { "1" })
        db.close()
    }

    fun getAlarm(): Cursor? {
        val db = this.readableDatabase
          return db.rawQuery("SELECT * FROM $alarm", null)
    }

    fun addAlarm(date: Int, time: String) {
        val db = this.readableDatabase
        val values = ContentValues()
        values.put(alarm_date, date)
        values.put(alarm_time, time)
        db.insert(alarm, null, values)
        db.close()
    }

    fun updAlarm(date: Int, time: String) {
        val db = this.readableDatabase
        val values = ContentValues()
        values.put(alarm_date, date)
        values.put(alarm_time, time)
        db.update(alarm, values, "$alarm_id = ?", Array(1) { "1" })
        db.close()
    }

    fun getLastCycle(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $cycles ORDER BY $cycle_sta DESC LIMIT 1", null)
    }

    fun getCycles(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $cycles ORDER BY $cycle_sta", null)
    }

    fun addCycle(sta: Int, end: Int) {
        val values = ContentValues()
        values.put(cycle_sta, sta)
        values.put(cycle_end, end)

        val db = this.writableDatabase
        db.insert(cycles, null, values)
        db.close()
    }

    fun updCycle(id: Int, sta: Int, end: Int) {
        val values = ContentValues()
        values.put(cycle_sta, sta)
        values.put(cycle_end, end)

        val db = this.writableDatabase
        db.update(cycles, values, "$cycle_id = ?", Array(1) { id.toString() })
        db.close()
    }

    fun delCycle(id: Int) {
        val db = this.writableDatabase
        db.delete(cycles, "$cycle_id = ?", Array(1) { id.toString() })
        db.close()
    }

    fun getVisits(type: Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $visits WHERE $visit_type = $type ORDER BY $visit_date", null)
    }

    fun addVisit(type: Int, date: Int, time: String, notes: String, status: Int) {
        val values = ContentValues()
        values.put(visit_type, type)
        values.put(visit_date, date)
        values.put(visit_time, time)
        values.put(visit_notes, notes)
        values.put(visit_status, status)

        val db = this.writableDatabase
        db.insert(visits, null, values)
        db.close()
    }

    fun updVisit(id: Int, type: Int, date: Int, time: String, notes: String, status: Int) {
        val values = ContentValues()
        values.put(visit_type, type)
        values.put(visit_date, date)
        values.put(visit_time, time)
        values.put(visit_notes, notes)
        values.put(visit_status, status)

        val db = this.writableDatabase
        db.update(visits, values, "$visit_id = ?", Array(1) { id.toString() })
        db.close()
    }

    fun delVisit(id: Int) {
        val db = this.writableDatabase
        db.delete(visits, "$visit_id = ?", Array(1) { id.toString() })
        db.close()
    }

    fun delAllVisits(type: Int) {
        val db = this.writableDatabase
        db.delete(visits, "$visit_type = ?", Array(1) { type.toString() })
        db.close()
    }
}