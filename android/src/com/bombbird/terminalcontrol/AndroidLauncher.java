package com.bombbird.terminalcontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends TextToSpeechManager {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		TerminalControl.ishtml = false;
		initialize(new TerminalControl(this), config);

		Intent ttsIntent = new Intent();
		ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		try {
			startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			Toast toast = Toast.makeText(getApplicationContext(), "Text-to-speech initialisation failed: Your device may not be compatible", Toast.LENGTH_LONG);
			toast.show();
		}
	}
}
