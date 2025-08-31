package com.bombbird.terminalcontrol

import android.system.ErrnoException
import android.system.OsConstants
import android.widget.Toast
import com.badlogic.gdx.utils.GdxRuntimeException
import com.bombbird.terminalcontrol.utilities.ToastManager
import java.io.IOException

class AndroidToastManager(private val androidLauncher: AndroidLauncher) : ToastManager {
    override fun saveFail(e: GdxRuntimeException) {
        var error = androidLauncher.resources.getString(R.string.Save_fail)
        val nextE = e.cause
        if (nextE is IOException && nextE.cause is ErrnoException) {
            val finalE = nextE.cause as ErrnoException?
            if (finalE?.errno == OsConstants.ENOSPC) error = androidLauncher.resources.getString(R.string.No_space)
        }
        val finalError = error
        androidLauncher.runOnUiThread {
            val toast = Toast.makeText(androidLauncher.applicationContext, finalError, Toast.LENGTH_LONG)
            toast.show()
        }
    }

    override fun readStorageFail() {
        androidLauncher.runOnUiThread {
            val toast = Toast.makeText(androidLauncher.applicationContext, androidLauncher.resources.getString(R.string.Load_fail), Toast.LENGTH_LONG)
            toast.show()
        }
    }

    override fun jsonParseFail() {
        androidLauncher.runOnUiThread {
            val toast = Toast.makeText(androidLauncher.applicationContext, androidLauncher.resources.getString(R.string.Save_corrupt), Toast.LENGTH_LONG)
            toast.show()
        }
    }

    fun initTTSFail() {
        androidLauncher.runOnUiThread {
            val toast = Toast.makeText(androidLauncher.applicationContext, androidLauncher.resources.getString(R.string.TTS_not_compatible), Toast.LENGTH_LONG)
            toast.show()
        }
    }

    fun ttsLangNotSupported() {
        androidLauncher.runOnUiThread {
            val toast = Toast.makeText(androidLauncher.applicationContext, androidLauncher.resources.getString(R.string.TTS_language_no_support), Toast.LENGTH_LONG)
            toast.show()
        }
    }
}