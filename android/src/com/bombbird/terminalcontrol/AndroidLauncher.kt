package com.bombbird.terminalcontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.bombbird.terminalcontrol.utilities.DiscordManager

class AndroidLauncher : TextToSpeechManager() {
    //private PlayGamesManager playGamesManager;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.numSamples = 0
        config.useAccelerometer = false
        config.useCompass = false
        initialize(TerminalControl(this, toastManager, object : DiscordManager {}), config)
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
}