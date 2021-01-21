package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.sounds.TextToSpeechInterface
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import marytts.LocalMaryInterface
import marytts.modules.synthesis.Voice
import marytts.util.data.audio.AppendableSequenceAudioInputStream
import marytts.util.data.audio.AudioPlayer
import org.apache.commons.lang3.SystemUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception

class TextToSpeechManager : TextToSpeechInterface {
    private var supportsSAPI = false
    private val voices = Array<String>()
    private var maryInterface: LocalMaryInterface? = null
    private var audioPlayer: AudioPlayer? = null
    private var maryAudioSequence: AppendableSequenceAudioInputStream? = null

    /** Says the text */
    override fun sayText(text: String, voice: String) {
        if (supportsSAPI) {
            Thread {
                Runtime.getRuntime().exec(
                    "${
                        Gdx.files.external(FileLoader.mainDir + "/tts/balcon.exe").file().absolutePath
                    } -s 2 -t \"$text\"${if (voices.contains(voice)) " -n \"$voice\"" else ""}"
                )
            }.start()
        } else {
            try {
                maryInterface?.voice = voice
                val audio = maryInterface?.generateAudio(text)
                if (audio != null) {
                    maryAudioSequence?.append(audio)
                    if (audioPlayer?.isAlive == false) audioPlayer?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Stops all current and subsequent speeches */
    override fun cancel() {
        if (supportsSAPI) {
            Thread {
                Runtime.getRuntime().exec("${Gdx.files.external(FileLoader.mainDir + "/tts/balcon.exe").file().absolutePath} -k")
            }.start()
        } else {
            audioPlayer?.cancel()
        }
    }

    /** Checks if the voice is available, returns original voice if it is, else returns a random voice from all available voices */
    override fun checkAndUpdateVoice(voice: String): String {
        if (voices.contains(voice)) return voice
        return voices.random() ?: voice
    }

    /** First ensures that balcon is available on the device, then gets the names of all the applicable voices available on the device */
    override fun loadVoices() {
        if (!voices.isEmpty) return

        if (SystemUtils.IS_OS_WINDOWS_10 || SystemUtils.IS_OS_WINDOWS_8 || SystemUtils.IS_OS_WINDOWS_7 || SystemUtils.IS_OS_WINDOWS_VISTA || SystemUtils.IS_OS_WINDOWS_XP) {
            val enVoices = Array<String>()
            enVoices.add("James", "Catherine", "Richard", "Linda")
            enVoices.add("George", "Hazel", "Susan", "Ravi")
            enVoices.add("Heera", "Shaun", "David", "Mark")
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
                supportsSAPI = true
                if (voices.isEmpty) {
                    //If user does not have any available voices installed, use MaryTTS
                    loadMary()
                }
            }.start()
        } else {
            //If user OS does not support Microsoft SAPI, use MaryTTS
            loadMary()
        }
    }

    private fun loadMary() {
        supportsSAPI = false
        for (voice in Voice.getAvailableVoices()) {
            voices.add(voice.name)
        }
        println(voices)
        if (voices.isEmpty) return
        maryInterface = LocalMaryInterface()
        maryAudioSequence = AppendableSequenceAudioInputStream(Voice.AF22050, null)
        audioPlayer = AudioPlayer(maryAudioSequence)
    }
}