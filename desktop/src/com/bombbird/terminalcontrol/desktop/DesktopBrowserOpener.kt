package com.bombbird.terminalcontrol.desktop

import com.bombbird.terminalcontrol.utilities.BrowserInterface
import java.awt.Desktop
import java.net.URI

class DesktopBrowserOpener: BrowserInterface {
    override fun openBrowser(link: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(URI(link))
    }
}