package com.bombbird.terminalcontrol.sounds

interface TextToSpeechInterface {
    fun sayText(text: String, voice: String)
    fun cancel()
    fun checkAndUpdateVoice(voice: String): String
    fun loadVoices()
}