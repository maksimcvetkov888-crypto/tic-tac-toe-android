package com.tictactoe.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Audio Synthesizer & DSP engine.
 * Generates pure mathematical audio waveforms via AudioTrack in real-time.
 */
object SynthesizerDSP {

    private const val SAMPLE_RATE = 44100
    private val audioExecutor = Executors.newSingleThreadExecutor()

    fun playMoveX() {
        audioExecutor.execute {
            // FM Synthesis: Crystalline Azure Chime (D5 carrier, fast decay)
            val durationMs = 120
            val samples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(samples)

            val carrierFreq = 587.33 // D5
            val modFreq = 1174.66   // D6
            val modIndex = 1.6

            for (i in 0 until samples) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = exp(-28.0 * t) // Rapid exponential decay
                val modulator = sin(2.0 * PI * modFreq * t) * modIndex
                val sample = sin(2.0 * PI * carrierFreq * t + modulator) * env
                buffer[i] = (sample * 16000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playMoveO() {
        audioExecutor.execute {
            // Warm Analog Subtractive Punch: Flame Orange (A3 -> low resonant pulse)
            val durationMs = 140
            val samples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(samples)

            val baseFreq = 220.0 // A3

            for (i in 0 until samples) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = exp(-20.0 * t)
                // Fundamental + rich 2nd and 3rd harmonics
                val s1 = sin(2.0 * PI * baseFreq * t)
                val s2 = sin(2.0 * PI * (baseFreq * 2.0) * t) * 0.45
                val s3 = sin(2.0 * PI * (baseFreq * 3.0) * t) * 0.2
                val combined = (s1 + s2 + s3) * env * 0.7
                buffer[i] = (combined * 17000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playImpactThud() {
        audioExecutor.execute {
            // Physical Mass Impact: 3D token striking obsidian stone pedestal
            val durationMs = 85
            val samples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = exp(-45.0 * t)
                // Pitch drop: 68 Hz down to 28 Hz
                val freq = 28.0 + 40.0 * exp(-35.0 * t)
                val sample = sin(2.0 * PI * freq * t) * env
                buffer[i] = (sample * 24000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playVictoryFanfare() {
        audioExecutor.execute {
            // Arpeggiated Polyphonic Cmaj9 Chord (C5, E5, G5, B5, D6)
            val noteDurationMs = 70
            val tailMs = 350
            val totalDurationMs = noteDurationMs * 5 + tailMs
            val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000f)).toInt()
            val buffer = ShortArray(totalSamples)

            val frequencies = doubleArrayOf(523.25, 659.25, 783.99, 987.77, 1174.66)

            for (n in frequencies.indices) {
                val freq = frequencies[n]
                val startSample = (n * (noteDurationMs / 1000f) * SAMPLE_RATE).toInt()

                for (i in startSample until totalSamples) {
                    val t = (i - startSample).toDouble() / SAMPLE_RATE
                    val env = exp(-6.5 * t)
                    val sample = sin(2.0 * PI * freq * t) * env * 0.28
                    val currentVal = buffer[i].toInt() + (sample * 30000).toInt()
                    buffer[i] = currentVal.coerceIn(-32767, 32767).toShort()
                }
            }
            playPcm(buffer)
        }
    }

    fun playDraw() {
        audioExecutor.execute {
            // Gentle descending chromatic resolution
            val durationMs = 280
            val samples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(samples)

            val notes = doubleArrayOf(440.0, 415.3, 392.0)
            val noteLen = samples / 3

            for (i in 0 until samples) {
                val noteIdx = (i / noteLen).coerceIn(0, 2)
                val freq = notes[noteIdx]
                val t = (i % noteLen).toDouble() / SAMPLE_RATE
                val env = exp(-12.0 * t)
                val sample = sin(2.0 * PI * freq * t) * env * 0.5
                buffer[i] = (sample * 16000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playUiClick() {
        audioExecutor.execute {
            // Subtle 15ms wood/glass mechanical click with zero DC offset
            val durationMs = 18
            val samples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / SAMPLE_RATE
                val window = 0.5 * (1.0 - kotlin.math.cos(2.0 * PI * i / samples)) // Hanning window
                val sample = sin(2.0 * PI * 1800.0 * t) * exp(-120.0 * t) * window
                buffer[i] = (sample * 14000).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        var track: AudioTrack? = null
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = maxOf(buffer.size * 2, minBuf)

            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size.toDouble() / SAMPLE_RATE * 1000).toLong() + 30)
        } catch (_: Exception) {
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {
            }
        }
    }
}
