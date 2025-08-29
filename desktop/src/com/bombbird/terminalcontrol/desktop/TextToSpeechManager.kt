package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.bombbird.terminalcontrol.sounds.TextToSpeechInterface
import io.github.jonelo.jAdapterForNativeTTS.engines.SpeechEngine
import io.github.jonelo.jAdapterForNativeTTS.engines.SpeechEngineNative
import io.github.jonelo.jAdapterForNativeTTS.engines.VoicePreferences
import io.github.jonelo.jAdapterForNativeTTS.engines.exceptions.SpeechEngineCreationException
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class TextToSpeechManager : TextToSpeechInterface {
    private var speechEngine: SpeechEngine? = null
    private lateinit var voicePrefs: VoicePreferences
    private val voiceArray = Array<String>()
    private val voiceSet = ObjectSet<String>()
    private val speechQueue = LinkedBlockingQueue<SpeechQueueItem>()
    private val running = AtomicBoolean(true)

    private class SpeechQueueItem(val text: String, val voice: String)

    /**
     * Starts the speaking for the input [SpeechQueueItem]
     * @param item item to be spoken
     */
    private fun saySpeechItem(item: SpeechQueueItem) {
        speechEngine?.setVoice(item.voice)
        // waitFor blocks till the process is complete, so we won't have multiple speeches at once
        speechEngine?.say(item.text)?.waitFor()
    }

    /** Says the text */
    override fun sayText(text: String, voice: String) {
        speechQueue.offer(SpeechQueueItem(text, voice))
    }

    /** Stops all current and subsequent speeches */
    override fun cancel() {
        speechQueue.clear()
        speechEngine?.stopTalking()
    }

    /** Checks if the voice is available, returns original voice if it is, else returns a random voice from all available voices */
    override fun checkAndUpdateVoice(voice: String): String {
        if (voiceSet.contains(voice)) return voice
        return voiceArray.random()
    }

    override fun loadVoices() {
        try {
            speechEngine = SpeechEngineNative.getInstance()
        } catch (e: SpeechEngineCreationException) {
            e.printStackTrace()
            // onInitFail?.invoke()
            return
        }

        voicePrefs = VoicePreferences().apply {
            language = Locale.ENGLISH.language
        }
        voiceArray.clear()
        voiceSet.clear()
        speechEngine?.availableVoices?.let {
            for (voice in it) {
                if (voice.matches(voicePrefs)) {
                    voiceArray.add(voice.name)
                    voiceSet.add(voice.name)
                }
            }
        }

        if (voiceArray.isEmpty) {
            Gdx.app.log("DesktopTTSHandler", "No English voices found; all voices available: ${speechEngine?.availableVoices?.joinToString { it.name }}")
            // onVoiceDataMissing?.invoke()
        } else {
            Gdx.app.log("DesktopTTSHandler", "TTS initialized")
        }

        speechEngine?.setRate(20)
        thread(name = "Desktop TTS") {
            while (running.get()) {
                // Timeout to prevent this thread from being blocked forever even when app closes
                val item = speechQueue.poll(10, TimeUnit.SECONDS) ?: continue
                saySpeechItem(item)
            }
        }
    }

    /** Ends the maryTTS thread if it exists */
    override fun quit() {
        speechQueue.clear()
        running.set(false)
    }
}