package com.bombbird.terminalcontrol;

import android.widget.Toast;

public class ToastManager implements com.bombbird.terminalcontrol.utilities.ToastManager {
    private AndroidLauncher androidLauncher;

    public ToastManager(AndroidLauncher androidLauncher) {
        this.androidLauncher = androidLauncher;
    }

    @Override
    public void saveFail() {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "Failed to save game: Check your storage space or settings and try again.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void readStorageFail() {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "Failed to load saves: Check your storage space or settings and try again.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void jsonParseFail() {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "Failed to load saves: 1 or more saves may be corrupted.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public void initTTSFail() {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "Text-to-speech initialisation failed: Your device may not be compatible", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public void ttsLangNotSupported() {
        androidLauncher.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "TTS language is not supported", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}
