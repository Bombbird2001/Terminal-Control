package com.bombbird.terminalcontrol

import com.bombbird.terminalcontrol.sounds.TextToSpeechInterface

class TextToSpeechManager : TextToSpeechInterface {
    override fun sayText(text: String, voice: String) {
        //No default implementation
    }

    override fun cancel() {
        //No default implementation
    }

    override fun checkAndUpdateVoice(voice: String): String {
        //No default implementation
        return ""
    }

    override fun loadVoices() {
        //No default implementation
    }
}