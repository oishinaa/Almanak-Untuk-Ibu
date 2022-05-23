package com.ec.almanakuntukibu.tracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.ec.almanakuntukibu.enum.State
import com.ec.almanakuntukibu.service.EndlessService

class ServiceTracker {
    private val name = "SPYSERVICE_KEY"
    private val key = "SPYSERVICE_STATE"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(name, 0)
    }

    fun setServiceState(context: Context, state: String) {
        val sharedPrefs = getPreferences(context)
        sharedPrefs.edit().let {
            it.putString(key, state)
            it.apply()
        }
    }

    fun getServiceState(context: Context): State {
        val sharedPrefs = getPreferences(context)
        val value = sharedPrefs.getString(key, State.STOPPED.name)
        return State.valueOf(value!!)
    }

    fun actionOnService(context: Context, action: String) {
        if (getServiceState(context) == State.STOPPED && action == "stop") return
        Intent(context, EndlessService::class.java).also {
            it.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(it)
                return
            }
            context.startService(it)
        }
    }
}