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
        Thread {
            Runtime.getRuntime().exec("${Gdx.files.external(FileLoader.mainDir + "/tts/balcon.exe").file().absolutePath} -s 2 -t \"$text\"${if (voices.contains(voice)) " -n \"$voice\"" else ""}")
        }.start()
    }

    /** Stops all current and subsequent speeches */
    override fun cancel() {
        Thread {
            Runtime.getRuntime().exec("${Gdx.files.external(FileLoader.mainDir + "/tts/balcon.exe").file().absolutePath} -k")
        }.start()
    }

    /** Checks if the voice is available, returns original voice if it is, else returns a random voice from all available voices */
    override fun checkAndUpdateVoice(voice: String): String {
        if (voices.contains(voice)) return voice
        return voices.random() ?: voice
    }

    /** First ensures that balcon is available on the device, then gets the names of all the applicable voices available on the device */
    override fun loadVoices() {
        if (!voices.isEmpty) return

        val enVoices = HashSet<String>()
        enVoices.add("James")
        enVoices.add("Catherine")
        enVoices.add("Richard")
        enVoices.add("Linda")
        enVoices.add("George")
        enVoices.add("Hazel")
        enVoices.add("Susan")
        enVoices.add("Ravi")
        enVoices.add("Heera")
        enVoices.add("Shaun")
        enVoices.add("David")
        enVoices.add("Mark")
        enVoices.add("Zira")

        Thread {
            val version = Gdx.files.internal("tts/version.txt")
            val currentVersion = Gdx.files.external(FileLoader.mainDir + "/tts/version.txt")
            if (!currentVersion.exists() || version.readString() != currentVersion.readString()) Gdx.files.internal("tts").copyTo(Gdx.files.external(FileLoader.mainDir))

            val process = Runtime.getRuntime().exec("${Gdx.files.external(FileLoader.mainDir + "/tts/balcon.exe").file().absolutePath} -l")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            process.waitFor()
            reader.forEachLine {
                val name = it.trim()
                if (name.isNotBlank() && !name.contains("SAPI") && enVoices.contains(name.split(" ")[1])) voices.add(name)
            }
            println(voices)
        }.start()
    }
}