package com.bombbird.terminalcontrol

import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration
import com.bombbird.terminalcontrol.utilities.BrowserInterface
import com.bombbird.terminalcontrol.utilities.DiscordManager
import com.bombbird.terminalcontrol.utilities.PlayServicesInterface
import com.bombbird.terminalcontrol.utilities.ToastManager
import com.bombbird.terminalcontrol.utilities.files.ExternalFileHandler
import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication

class IOSLauncher : IOSApplication.Delegate() {
    override fun createApplication(): IOSApplication {
        val config = IOSApplicationConfiguration()
        return IOSApplication(TerminalControl(
            TextToSpeechManager(), object : ToastManager {}, object : DiscordManager {}, object : ExternalFileHandler {}, object : BrowserInterface {}, object : PlayServicesInterface {}), config)
    }

    companion object {
        @JvmStatic
        fun main(argv: Array<String>) {
            val pool = NSAutoreleasePool()
            UIApplication.main<UIApplication, IOSLauncher>(argv, null, IOSLauncher::class.java)
            pool.close()
        }
    }
}