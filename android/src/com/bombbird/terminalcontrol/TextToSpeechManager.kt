package com.bombbird.terminalcontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.Voice
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.sounds.TextToSpeechInterface
import java.util.*

open class TextToSpeechManager : AndroidApplication(), OnInitListener, TextToSpeechInterface {
    companion object {
        const val ACT_CHECK_TTS_DATA = 1000
        const val ACT_INSTALL_TTS_DATA = 1001
    }

    private var tts: android.speech.tts.TextToSpeech? = null
    lateinit var toastManager: ToastManager

    private val voiceArray = Array<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toastManager = ToastManager(this as AndroidLauncher)
    }

    /** Performs relevant actions after receiving status for TTS data check   */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == ACT_CHECK_TTS_DATA) {
                if (resultCode == android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    //Data exists, so we instantiate the TTS engine
                    tts = android.speech.tts.TextToSpeech(this, this)
                } else {
                    //Data is missing, so we start the TTS installation process
                    val installIntent = Intent()
                    installIntent.action = android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                    startActivityForResult(installIntent, ACT_INSTALL_TTS_DATA)
                }
            } else if (requestCode == ACT_INSTALL_TTS_DATA) {
                val ttsIntent = Intent()
                ttsIntent.action = android.speech.tts.TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA)
            }
        } catch (e: ActivityNotFoundException) {
            toastManager.initTTSFail()
        }
    }

    /** Sets initial properties after initialisation of TTS is complete  */
    override fun onInit(status: Int) {
        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
            if (tts != null) {
                val result = tts?.setLanguage(Locale.ENGLISH)
                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                    toastManager.ttsLangNotSupported()
                } else {
                    Gdx.app.log("Text to Speech", "TTS initialized successfully")
                    tts?.setSpeechRate(1.7f)
                    loadVoices()
                }
            }
        } else {
            toastManager.initTTSFail()
            Gdx.app.log("Text to Speech", "TTS initialization failed")
        }
    }

    /** Stops, destroys TTS instance  */
    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    /** Says the text depending on API level */
    override fun sayText(text: String, voice: String) {
        if (tts == null) return
        tts?.voice = Voice(voice, Locale.ENGLISH, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null)
        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, null, null)
    }

    /** Stops all current and subsequent speeches */
    override fun cancel() {
        tts?.stop()
    }

    /** Checks if the voice is available, returns original voice if it is, else returns a random voice from all available voices */
    override fun checkAndUpdateVoice(voice: String): String {
        if (voiceArray.contains(voice)) return voice
        return voiceArray.random() ?: voice
    }

    /** Gets the names of all the applicable voices available on the device */
    override fun loadVoices() {
        try {
            if (tts?.voices?.isEmpty() != false) return
        } catch (e: Exception) {
            return
        }
        tts?.voices?.let {
            for (available in it) {
                if ("en" == available.name.substring(0, 2)) {
                    voiceArray.add(available.name)
                }
            }
        }
    }
}