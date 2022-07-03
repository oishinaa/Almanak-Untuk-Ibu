package com.ec.almanakuntukibu.tracker

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager


class AudioTracker {
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private var instance: AudioTracker? = null
        val getMediaPlayerInstance = {
            instance ?: AudioTracker().also { instance = it }
        }
    }

    fun startAudio(context: Context) {
        var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
        // alarmUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.message_tone_meloboom)

        stopAudio()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            mediaPlayer?.setDataSource(context, alarmUri)
        }
        mediaPlayer?.isLooping = true
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun stopAudio() {
        mediaPlayer?.stop()
    }
}