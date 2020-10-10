package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.ToastManager

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("Terminal Control")
        config.setWindowIcon("game/Icon48.png", "game/Icon32.png", "game/Icon16.png")
        config.setWindowedMode(1440, 810)
        config.setMaximized(true)
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0)
        TerminalControl.ishtml = false
        Lwjgl3Application(TerminalControl(TextToSpeechManager(), object : ToastManager {}, DiscordRPCManager()), config)
    }
}