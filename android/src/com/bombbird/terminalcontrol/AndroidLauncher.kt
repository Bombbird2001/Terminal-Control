package com.bombbird.terminalcontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.utilities.DiscordManager
import com.bombbird.terminalcontrol.utilities.files.ExternalFileChooser
import java.io.ByteArrayOutputStream
import java.io.IOException

const val OPEN_SAVE_FILE = 9

class AndroidLauncher : TextToSpeechManager(), ExternalFileChooser {
    private var loadGameScreen: LoadGameScreen? = null
    //private PlayGamesManager playGamesManager;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.numSamples = 0
        config.useAccelerometer = false
        config.useCompass = false
        initialize(TerminalControl(this, toastManager, object : DiscordManager {}, this), config)
        val ttsIntent = Intent()
        ttsIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        try {
            startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            toastManager.initTTSFail()
        }

        //playGamesManager = new PlayGamesManager();
        //playGamesManager.gameSignIn(this);
    }

    override fun openFileChooser(loadGameScreen: LoadGameScreen) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }

        this.loadGameScreen = loadGameScreen
        startActivityForResult(intent, OPEN_SAVE_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_SAVE_FILE) {
            val uri = data.data
            if (uri == null) {
                notifyGame("", loadGameScreen)
                return
            }
            val inputStream = contentResolver.openInputStream(uri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            var i: Int?
            try {
                i = inputStream?.read()
                while (i != -1 && i != null) {
                    byteArrayOutputStream.write(i)
                    i = inputStream?.read()
                }
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val strData = byteArrayOutputStream.toString()
            notifyGame(strData, loadGameScreen)
            loadGameScreen = null
        }
    }
}