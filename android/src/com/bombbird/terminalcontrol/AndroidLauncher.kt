package com.bombbird.terminalcontrol

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.badlogic.gdx.Game
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.utils.Base64Coder
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.utilities.DiscordManager
import com.bombbird.terminalcontrol.utilities.files.ExternalFileHandler
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception

class AndroidLauncher : AndroidTextToSpeechManager(), ExternalFileHandler {
    companion object {
        const val OPEN_SAVE_FILE = 9
        const val CREATE_SAVE_FILE = 10
        const val PLAY_SIGN_IN = 11
        const val PLAY_SHOW_ACHIEVEMENTS = 12
        const val DRIVE_PERMISSION = 13
    }

    private var loadGameScreen: LoadGameScreen? = null
    private var save: JSONObject? = null
    lateinit var playGamesManager: PlayServicesManager
    lateinit var view: View
    lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.numSamples = 0
        config.useAccelerometer = false
        config.useCompass = false
        playGamesManager = PlayServicesManager(this)
        game = TerminalControl(this, toastManager, object : DiscordManager {}, this, AndroidBrowserOpener(this), playGamesManager)
        view = initializeForView(game, config)
        setAndroidView(view)
        val ttsIntent = Intent()
        ttsIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        try {
            startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            toastManager.initTTSFail()
        }

        val pref = getPreferences(Context.MODE_PRIVATE)
        if (!pref.getBoolean("declinePlaySignIn", false)) playGamesManager.gameSignIn()
    }

    @Suppress("DEPRECATION")
    private fun setAndroidView(view: View) {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        } catch (ex: Exception) {
            log("Terminal Control", "Content already displayed, cannot request FEATURE_NO_TITLE", ex)
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        setContentView(view, createLayoutParams())
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
        if (resultCode == RESULT_CANCELED || data == null) {
            if (requestCode == PLAY_SIGN_IN) {
                val pref = getPreferences(Context.MODE_PRIVATE)
                with (pref.edit()) {
                    putBoolean("declinePlaySignIn", true)
                    apply()
                }
            } else if (requestCode == DRIVE_PERMISSION) {
                playGamesManager.drivePermissionGranted = false
            }
            return
        }
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
                playGamesManager.signedInAccount = result.signInAccount
            } else {
                val message = result?.status?.statusMessage
                Log.e("Play Sign-in", message ?: "")
                if (result?.status?.statusCode == ConnectionResult.SIGN_IN_REQUIRED) {
                    this.startActivityForResult(
                        GoogleSignIn.getClient(this, playGamesManager.getSignInOptions()).signInIntent, PLAY_SIGN_IN
                    )
                }
            }
        } else if (requestCode == DRIVE_PERMISSION) {
            playGamesManager.drivePermissionGranted = true
            if (playGamesManager.save) playGamesManager.driveSaveGame() else playGamesManager.driveLoadGame()
        }
    }
}