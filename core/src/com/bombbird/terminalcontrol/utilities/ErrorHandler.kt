package com.bombbird.terminalcontrol.utilities

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.ui.Ui
import org.apache.commons.lang3.exception.ExceptionUtils

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
            ${ExceptionUtils.getStackTrace(e)}
            """.trimIndent()
        HttpRequests.sendError(error, 0)
        e.printStackTrace()
        if (!exit) return
        //Quit game
        TerminalControl.radarScreen?.metar?.isQuit = true
        TerminalControl.radarScreen?.dispose()
        Gdx.app.exit()
        if (Gdx.app.type == Application.ApplicationType.Android) throw RuntimeException(e)
    }

    fun sendStringError(e: Exception, str: String) {
        var error =
                """
                $versionInfo
                ${ExceptionUtils.getStackTrace(e)}
                """.trimIndent()
        error =
            """
            $str
            $error
            """.trimIndent()
        HttpRequests.sendError(error, 0)
        e.printStackTrace()
        //Quit game
        TerminalControl.radarScreen?.metar?.isQuit = true
        TerminalControl.radarScreen?.dispose()
        Gdx.app.exit()
        if (Gdx.app.type == Application.ApplicationType.Android) throw RuntimeException(e)
    }

    fun sendSaveErrorNoThrow(e: Exception, str: String) {
        var error =
            """
            $versionInfo
            No crash
            ${ExceptionUtils.getStackTrace(e)}
            """.trimIndent()
        error =
            """
            $str
            $error
            """.trimIndent()
        HttpRequests.sendError(error, 0)
        e.printStackTrace()
        //Don't throw runtime exception
    }

    fun sendRepeatableError(original: String, e: Exception, attempt: Int) {
        val error =
            """
            $versionInfo
            Try $attempt:
            $original
            ${ExceptionUtils.getStackTrace(e)}
            """.trimIndent()
        HttpRequests.sendError(error, 0)
        e.printStackTrace()
        println(original)
    }
}