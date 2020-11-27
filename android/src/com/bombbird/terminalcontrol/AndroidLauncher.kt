package com.bombbird.terminalcontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.utils.Base64Coder
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.utilities.DiscordManager
import com.bombbird.terminalcontrol.utilities.files.ExternalFileHandler
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

const val OPEN_SAVE_FILE = 9
const val CREATE_SAVE_FILE = 10

class AndroidLauncher : TextToSpeechManager(), ExternalFileHandler {
    private var loadGameScreen: LoadGameScreen? = null
    private var save: JSONObject? = null
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
            type = "*/*"
        }

        this.loadGameScreen = loadGameScreen
        startActivityForResult(intent, OPEN_SAVE_FILE)
    }

    override fun openFileSaver(save: JSONObject, loadGameScreen: LoadGameScreen) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, save.optString("MAIN_NAME", "") + ".tcsav")
            type = "*/*"
        }

        this.loadGameScreen = loadGameScreen
        this.save = save
        startActivityForResult(intent, CREATE_SAVE_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_CANCELED || data == null) return
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_SAVE_FILE) {
            val uri = data.data
            if (uri == null) {
                notifyLoaded("", loadGameScreen)
                loadGameScreen = null
                return
            } else if (uri.path?.endsWith(".tcsav") != true) {
                notifyFormat(loadGameScreen)
                loadGameScreen = null
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
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                inputStream?.close()
            }
            val strData = byteArrayOutputStream.toString()
            notifyLoaded(strData, loadGameScreen)
            loadGameScreen = null
        } else if (requestCode == CREATE_SAVE_FILE) {
            val uri = data.data
            if (uri == null) {
                notifySaved(false, loadGameScreen)
                loadGameScreen = null
                save = null
                return
            } else if (uri.path?.endsWith(".tcsav") != true) {
                notifyFormat(loadGameScreen)
                loadGameScreen = null
                save = null
                return
            }
            uri.path?.let {
                val fileOutputStream = contentResolver.openOutputStream(uri)
                try {
                    fileOutputStream?.write(Base64Coder.encodeString(save.toString()).encodeToByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    fileOutputStream?.close()
                }
                notifySaved(true, loadGameScreen)
            } ?: run {
                notifySaved(false, loadGameScreen)
            }
            loadGameScreen = null
            save = null
        }
    }
}