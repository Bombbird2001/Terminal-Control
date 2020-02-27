package com.bombbird.terminalcontrol;

import android.system.ErrnoException;
import android.system.OsConstants;
import android.widget.Toast;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;

public class ToastManager implements com.bombbird.terminalcontrol.utilities.ToastManager {
    private AndroidLauncher androidLauncher;

    public ToastManager(AndroidLauncher androidLauncher) {
        this.androidLauncher = androidLauncher;
    }

    @Override
    public void saveFail(GdxRuntimeException e) {
        String error = "Failed to save game: Check your storage space or settings and try again.";
        Throwable nextE = e.getCause();
        if (nextE instanceof IOException && nextE.getCause() instanceof ErrnoException) {
            ErrnoException finalE = (ErrnoException) nextE.getCause();
            if (finalE.errno == OsConstants.ENOSPC) error = "Failed to save game: Your device has insufficient storage space.";
        }
        String finalError = error;
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), finalError, Toast.LENGTH_LONG);
            toast.show();
        });
    }

    @Override
    public void readStorageFail() {
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "Failed to load saves: Check your storage space or settings and try again.", Toast.LENGTH_LONG);
            toast.show();
        });
    }

    @Override
    public void jsonParseFail() {
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "Failed to load saves: 1 or more saves may be corrupted.", Toast.LENGTH_LONG);
            toast.show();
        });
    }

    public void initTTSFail() {
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "Text-to-speech initialisation failed: Your device may not be compatible", Toast.LENGTH_LONG);
            toast.show();
        });
    }

    public void ttsLangNotSupported() {
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), "TTS language is not supported", Toast.LENGTH_LONG);
            toast.show();
        });
    }
}
