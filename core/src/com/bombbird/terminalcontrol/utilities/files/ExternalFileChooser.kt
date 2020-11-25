package com.bombbird.terminalcontrol.utilities.files

import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen

interface ExternalFileChooser {
    fun openFileChooser(loadGameScreen: LoadGameScreen)

    fun notifyGame(strData: String, loadGameScreen: LoadGameScreen?) {
        loadGameScreen?.importSave(strData)
    }
}