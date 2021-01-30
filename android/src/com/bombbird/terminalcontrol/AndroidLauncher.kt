package com.bombbird.terminalcontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.utils.Base64Coder
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.utilities.DiscordManager
import com.bombbird.terminalcontrol.utilities.files.ExternalFileHandler
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class AndroidLauncher : AndroidTextToSpeechManager(), ExternalFileHandler {
    companion object {
        const val OPEN_SAVE_FILE = 9
        const val CREATE_SAVE_FILE = 10
        const val PLAY_SIGN_IN = 11
    }

    private var loadGameScreen: LoadGameScreen? = null
    private var save: JSONObject? = null
    private lateinit var playGamesManager: PlayGamesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.numSamples = 0
        config.useAccelerometer = false
        config.useCompass = false
        initialize(TerminalControl(this, toastManager, object : DiscordManager {}, this, AndroidBrowserOpener(this)), config)
        val ttsIntent = Intent()
        ttsIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        try {
            startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            toastManager.initTTSFail()
        }

        playGamesManager = PlayGamesManager()
        playGamesManager.gameSignIn(this)
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
        } else if (requestCode == PLAY_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result?.isSuccess == true) {
                // The signed in account is stored in the result.
                val signedInAccount = result.signInAccount
                val toast = Toast.makeText(this, "Login successful", Toast.LENGTH_LONG)
                toast.show()
            } else {
                val message = result?.status?.statusMessage
                Log.e("Play Sign-in", message ?: "")
                if (result?.status?.statusCode == ConnectionResult.SIGN_IN_REQUIRED) {
                    this.startActivityForResult(
                        GoogleSignIn.getClient(
                            this,
                            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
                        ).signInIntent, PLAY_SIGN_IN
                    )
                }
            }
        }
    }
}