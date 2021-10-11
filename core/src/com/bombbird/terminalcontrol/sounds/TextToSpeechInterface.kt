package com.bombbird.terminalcontrol.sounds

interface TextToSpeechInterface {
    fun sayText(text: String, voice: String) {
        //No default implementation
    }
    fun cancel() {
        //No default implementation
    }
    fun checkAndUpdateVoice(voice: String): String {
        //No default implementation
        return ""
    }
    fun loadVoices() {
        //No default implementation
    }
    fun quit() {
        //No default implementation
    }
}