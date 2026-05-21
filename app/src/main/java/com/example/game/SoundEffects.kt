package com.example.game

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundEffects {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Failed to initialize ToneGenerator", e)
        }
    }

    @Synchronized
    fun playBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing beep", e)
        }
    }

    @Synchronized
    fun playExplosion() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing explosion", e)
        }
    }

    @Synchronized
    fun playPowerUp() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 150)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing powerup", e)
        }
    }

    @Synchronized
    fun playWarning() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONGESTION, 250)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing warning", e)
        }
    }

    @Synchronized
    fun playHit() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing hit", e)
        }
    }

    @Synchronized
    fun playEmp() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RADIO_ACK, 300)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing EMP", e)
        }
    }
}
