package com.bombbird.terminalcontrol.utilities.errors

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Base64Coder
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.HttpRequests
import org.apache.commons.lang3.exception.ExceptionUtils
import org.json.JSONObject

object ErrorHandler {
    private val versionInfo: String
        get() {
            var type = "Unknown"
            if (Gdx.app.type == Application.ApplicationType.Android) {
                type = "Android"
            } else if (Gdx.app.type == Application.ApplicationType.Desktop) {
                type = "Desktop"
            }
            val arpt = TerminalControl.radarScreen?.mainName ?: "unknown airport"
            return """$type ${if (TerminalControl.full) "full" else "lite"} version ${TerminalControl.versionName}, build ${TerminalControl.versionCode}, $arpt"""
        }

    fun sendGenericError(e: Exception, exit: Boolean) {
        val error =
            """
            $versionInfo
            ${if (exit) "Crash" else "No crash"}
            """.trimIndent() + "\n${ExceptionUtils.getStackTrace(e)}"
        HttpRequests.sendError(error, 0)
        e.printStackTrace()
        if (!exit) return
        //Quit game
        TerminalControl.radarScreen?.metar?.isQuit = true
        TerminalControl.radarScreen?.dispose()
        Gdx.app.exit()
        if (Gdx.app.type == Application.ApplicationType.Android) throw RuntimeException(e)
    }

    fun sendSaveError(e: Exception, save: JSONObject, dialog: CustomDialog) {
        val saveError =
            """
            $versionInfo
            Save string: ${Base64Coder.encodeString(save.toString())}
            """.trimIndent() + "\n${ExceptionUtils.getStackTrace(e)}"
        HttpRequests.sendSaveError(saveError, 0, save, dialog)
        e.printStackTrace()
    }
}