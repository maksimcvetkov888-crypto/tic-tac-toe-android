package com.tictactoe.game.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundEffects(private val context: Context) {

    fun playMove(isX: Boolean) {
        if (isX) {
            SynthesizerDSP.playMoveX()
        } else {
            SynthesizerDSP.playMoveO()
        }
    }

    fun playImpact() {
        SynthesizerDSP.playImpactThud()
    }

    fun playWin() {
        SynthesizerDSP.playVictoryFanfare()
    }

    fun playDraw() {
        SynthesizerDSP.playDraw()
    }

    fun playClick() {
        SynthesizerDSP.playUiClick()
    }

    @Suppress("DEPRECATION")
    fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun vibrateImpact() {
        vibrate(35)
    }

    fun vibrateWin() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val pattern = longArrayOf(0, 80, 50, 120, 60, 200)
                val amplitudes = intArrayOf(0, 140, 0, 190, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            } else {
                vibrate(220)
            }
        } catch (_: Exception) {
            vibrate(150)
        }
    }

    fun release() {
        // No persistent resources to close
    }
}
