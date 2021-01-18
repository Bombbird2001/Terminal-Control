package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.sounds.TextToSpeechInterface
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import java.io.BufferedReader
import java.io.InputStreamReader

class TextToSpeechManager : TextToSpeechInterface {
    private val voices = Array<String>()

    /** Says the text */
    override fun sayText(text: String, voice: String) {
        //No default implementation
    }

    /** Stops all current and subsequent speeches */
    override fun cancel() {
        //No default implementation
    }

    /** Checks if the voice is available, returns original voice if it is, else returns a random voice from all available voices */
    override fun checkAndUpdateVoice(voice: String): String {
        if (voices.contains(voice)) return voice
        return voices.random() ?: voice
    }

    /** First ensures that balcon is available on the device, then gets the names of all the applicable voices available on the device */
    override fun loadVoices() {
        val version = Gdx.files.internal("tts/version.txt")
        val currentVersion = Gdx.files.external(FileLoader.mainDir + "/tts/version.txt")
        if (!currentVersion.exists() || version.readString() != currentVersion.readString()) Gdx.files.internal("tts").copyTo(Gdx.files.external(FileLoader.mainDir))

        val process = Runtime.getRuntime().exec("${Gdx.files.external(FileLoader.mainDir + "/tts/balcon.exe").file().absolutePath} -l")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        process.waitFor()
        reader.forEachLine {
            if (it.isNotBlank() && !it.contains("SAPI")) voices.add(it.trim())
        }
        println(voices)
    }
}