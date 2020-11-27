package com.bombbird.terminalcontrol.utilities.files

import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import org.json.JSONObject

interface ExternalFileHandler {
    fun openFileChooser(loadGameScreen: LoadGameScreen)

    fun notifyLoaded(strData: String, loadGameScreen: LoadGameScreen?) {
        loadGameScreen?.importSave(strData)
    }

    fun openFileSaver(save: JSONObject, loadGameScreen: LoadGameScreen)

    fun notifySaved(success: Boolean, loadGameScreen: LoadGameScreen?) {
        loadGameScreen?.showExportMsg(success)
    }

    fun notifyFormat(loadGameScreen: LoadGameScreen?) {
        loadGameScreen?.showFileTypeMsg()
    }
}