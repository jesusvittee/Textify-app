package com.textify.app.services.audio

import android.content.Context
import android.media.AudioManager as SystemAudioManager

class AudioManager(private val context: Context) {

    private val systemAudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as SystemAudioManager

    fun getCurrentVolume(): Int {
        return systemAudioManager.getStreamVolume(SystemAudioManager.STREAM_MUSIC)
    }

    fun setVolume(volume: Int) {
        systemAudioManager.setStreamVolume(
            SystemAudioManager.STREAM_MUSIC,
            volume,
            0
        )
    }

    fun isSpeakerOn(): Boolean {
        return systemAudioManager.isSpeakerphoneOn
    }

    fun toggleSpeaker(on: Boolean) {
        systemAudioManager.isSpeakerphoneOn = on
    }
}