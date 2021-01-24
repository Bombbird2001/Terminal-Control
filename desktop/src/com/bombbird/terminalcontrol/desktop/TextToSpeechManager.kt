package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.sounds.TextToSpeechInterface
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import marytts.LocalMaryInterface
import marytts.modules.synthesis.Voice
import marytts.util.data.audio.AudioPlayer
import org.apache.commons.lang3.SystemUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class TextToSpeechManager : TextToSpeechInterface {
    private var supportsSAPI = false
    private val voices = Array<String>()
    private var maryInterface: LocalMaryInterface? = null
    private var blockingQueue: LinkedBlockingQueue<Runnable>? = null
    private var running = false
    private var currentAudioPlayer: AudioPlayer? = null
    private var maryThread: Thread? = null

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
             blockingQueue?.offer(Runnable {
                try {
                    maryInterface?.voice = voice
                    maryInterface?.audioEffects = "Volume(amount:1.0)+Rate(durScale:1.5)"
                    val audio = maryInterface?.generateAudio(text)
                    if (audio != null) {
                        currentAudioPlayer = AudioPlayer(audio)
                        currentAudioPlayer?.run() //Run on the same maryTTS thread, so next voice output will be generated after current speech is complete
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 3, TimeUnit.SECONDS)
        }
    }

    /** Stops all current and subsequent speeches */
    override fun cancel() {
        if (supportsSAPI) {
            Thread {
                Runtime.getRuntime().exec("${Gdx.files.external(FileLoader.mainDir + "/tts/balcon.exe").file().absolutePath} -k")
            }.start()
        } else {
            blockingQueue?.clear()
            currentAudioPlayer?.cancel()
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

        if (false && (SystemUtils.IS_OS_WINDOWS_10 || SystemUtils.IS_OS_WINDOWS_8 || SystemUtils.IS_OS_WINDOWS_7 || SystemUtils.IS_OS_WINDOWS_VISTA || SystemUtils.IS_OS_WINDOWS_XP)) {
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

    /** Ends the maryTTS thread if it exists */
    override fun quit() {
        running = false
        maryThread?.interrupt()
    }

    /** Initialize maryTTS */
    private fun loadMary() {
        supportsSAPI = false
        if (!voices.isEmpty) return
        maryInterface = LocalMaryInterface()
        for (voice in Voice.getAvailableVoices()) {
            voices.add(voice.name)
        }
        running = true
        blockingQueue = LinkedBlockingQueue(10)
        maryThread = Thread {
            //Start maryTTS thread to deal with sayText requests
            while (running) {
                try {
                    blockingQueue?.take()?.run()
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        maryThread?.start()
    }
}