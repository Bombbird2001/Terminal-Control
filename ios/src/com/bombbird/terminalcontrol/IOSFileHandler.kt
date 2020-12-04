package com.bombbird.terminalcontrol

import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.utilities.files.ExternalFileHandler
import org.json.JSONObject

class IOSFileHandler: ExternalFileHandler {
    override fun openFileChooser(loadGameScreen: LoadGameScreen) {
        //No default implementation
    }

    override fun openFileSaver(save: JSONObject, loadGameScreen: LoadGameScreen) {
        //No default implementation
    }
}