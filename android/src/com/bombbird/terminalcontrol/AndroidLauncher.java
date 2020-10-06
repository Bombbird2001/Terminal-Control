package com.bombbird.terminalcontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.bombbird.terminalcontrol.utilities.DiscordManager;

public class AndroidLauncher extends TextToSpeechManager {
    //private PlayGamesManager playGamesManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.numSamples = 0;
        config.useAccelerometer = false;
        config.useCompass = false;
        TerminalControl.ishtml = false;
        initialize(new TerminalControl(this, toastManager, new DiscordManager() {}), config);

        Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        try {
            startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            toastManager.initTTSFail();
        }

        //playGamesManager = new PlayGamesManager();
        //playGamesManager.gameSignIn(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //playGamesManager.onActivityResult(this, requestCode, resultCode, data);
    }
}
