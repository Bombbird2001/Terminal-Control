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
        String error = androidLauncher.getResources().getString(R.string.Save_fail);
        Throwable nextE = e.getCause();
        if (nextE instanceof IOException && nextE.getCause() instanceof ErrnoException) {
            ErrnoException finalE = (ErrnoException) nextE.getCause();
            if (finalE.errno == OsConstants.ENOSPC) error = androidLauncher.getResources().getString(R.string.No_space);
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
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), androidLauncher.getResources().getString(R.string.Load_fail), Toast.LENGTH_LONG);
            toast.show();
        });
    }

    @Override
    public void jsonParseFail() {
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), androidLauncher.getResources().getString(R.string.Save_corrupt), Toast.LENGTH_LONG);
            toast.show();
        });
    }

    public void initTTSFail() {
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), androidLauncher.getResources().getString(R.string.TTS_not_compatible), Toast.LENGTH_LONG);
            toast.show();
        });
    }

    public void ttsLangNotSupported() {
        androidLauncher.runOnUiThread(() -> {
            Toast toast = Toast.makeText(androidLauncher.getApplicationContext(), androidLauncher.getResources().getString(R.string.TTS_language_no_support), Toast.LENGTH_LONG);
            toast.show();
        });
    }
}
