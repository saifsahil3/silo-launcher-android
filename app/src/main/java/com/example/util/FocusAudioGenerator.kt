package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusAudioGenerator {
    private var audioTrack: AudioTrack? = null
    @Volatile private var isPlaying = false
    private var job: Job? = null

    fun startAmbientSound(coroutineScope: CoroutineScope, trackType: Int = 0) {
        if (isPlaying) return
        isPlaying = true
        job = coroutineScope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4410)

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()
                val buffer = ShortArray(1024)
                var lastSample = 0f
                var phase = 0.0

                while (isPlaying && isActive) {
                    for (i in buffer.indices) {
                        when (trackType) {
                            0 -> {
                                // Rain & White Noise (Soft filtered noise)
                                val white = (Math.random() * 2.0 - 1.0).toFloat()
                                lastSample = (lastSample + (0.08f * white)) / 1.08f
                                buffer[i] = (lastSample * 3500f).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            1 -> {
                                // Deep Waves (Sine wave + subtle noise modulation)
                                phase += 2.0 * Math.PI * 180.0 / sampleRate
                                val tone = Math.sin(phase).toFloat()
                                val noise = (Math.random() * 2.0 - 1.0).toFloat()
                                lastSample = (lastSample + 0.05f * (tone + noise * 0.2f)) / 1.05f
                                buffer[i] = (lastSample * 4000f).toInt().coerceIn(-32768, 32767).toShort()
                            }
                            else -> {
                                // Gentle Wind (LFO modulated pink noise)
                                phase += 0.0005
                                val lfo = (Math.sin(phase) + 1.0) * 0.5
                                val white = (Math.random() * 2.0 - 1.0).toFloat()
                                lastSample = (lastSample + (0.06f * white)) / 1.06f
                                buffer[i] = (lastSample * 3000f * (0.4 + 0.6 * lfo)).toInt().coerceIn(-32768, 32767).toShort()
                            }
                        }
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        isPlaying = false
        job?.cancel()
        job = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        audioTrack = null
    }

    fun isCurrentlyPlaying(): Boolean = isPlaying
}
