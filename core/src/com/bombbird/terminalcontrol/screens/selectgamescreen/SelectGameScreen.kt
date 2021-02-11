package com.bombbird.terminalcontrol.screens.selectgamescreen

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.SurveyAdsManager
import com.bombbird.terminalcontrol.utilities.files.FileLoader

open class SelectGameScreen(game: TerminalControl, val background: Image?) : BasicScreen(game, 2880, 1620) {
    val scrollTable: Table = Table()
    lateinit var backButton: TextButton

    //Styles
    lateinit var labelStyle: Label.LabelStyle
        private set
    lateinit var buttonStyle: TextButton.TextButtonStyle

    /** Loads the full UI of this screen  */
    open fun loadUI() {
        //Reset stage
        stage.clear()
        stage.addActor(background)
        loadLabel()
        loadButtons()
        loadScroll()
    }

    /** Loads the appropriate labelStyle, and is overridden to load a label with the appropriate text  */
    open fun loadLabel() {
        //Set label style
        labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
    }

    /** Loads the default button styles and back button  */
    open fun loadButtons() {
        //Set button textures
        buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        //Set back button params
        backButton = TextButton("<= Back", buttonStyle)
        backButton.width = MainMenuScreen.BUTTON_WIDTH
        backButton.height = MainMenuScreen.BUTTON_HEIGHT
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f)
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu
                game.screen = MainMenuScreen(game, background)
            }
        })
        stage.addActor(backButton)
    }

    /** Loads the contents of the scrollPane  */
    open fun loadScroll() {
        //No default implementation
    }

    /** Implements show method of screen  */
    override fun show() {
        loadUI()
    }

    /** Overrides render method to include detection of back button on android  */
    override fun render(delta: Float) {
        super.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            //On android, emulate backButton is pressed
            backButton.toggle()
        }
    }

    /** Gets the next saveID that is available for a new game save, returns -1 if an error occurs */
    fun getNextAvailableSaveSlot(): Int {
        val handle1: FileHandle
        when (Gdx.app.type) {
            Application.ApplicationType.Android -> handle1 = Gdx.files.local("saves/saves.saves")
            Application.ApplicationType.Desktop -> handle1 = Gdx.files.external(FileLoader.mainDir + "/saves/saves.saves")
            else -> {
                handle1 = Gdx.files.local("saves/saves.saves")
                Gdx.app.log("File load error", "Unknown platform " + Gdx.app.type.name + " used!")
            }
        }
        var slot = -1
        if (!handle1.exists()) handle1.writeString("", false)
        if (handle1.exists()) {
            slot = 0
            val saves = Array(handle1.readString().split(",".toRegex()).toTypedArray())
            while (saves.contains(slot.toString(), false)) {
                slot++
            }
        }
        return slot
    }

    /** Checks whether the airport is locked and requires ad/survey to unlock, returns false if no dialog needs to be shown, else true */
    fun showAdsSurvey(airport: String): Boolean {
        if (TerminalControl.full || Gdx.app.type != Application.ApplicationType.Android) return false //If is full version, or is not Android, return false
        if (!SurveyAdsManager.unlockableAirports.contains(airport)) return false //If airport does not need to be unlocked, or is not unlockable, return false
        if (SurveyAdsManager.remainingTime(airport) <= 0) {
            showSurveyAdDialog(airport)
            return true
        }
        return false
    }

    /** Shows the dialog that prompts the user to complete a survey (if available) or watch an ad */
    private fun showSurveyAdDialog(airport: String) {
        if (TerminalControl.playServicesInterface.isSurveyAvailable()) {
            object : CustomDialog("Unlock airport", "Complete a quick survey to unlock all airports for 3 hours?", "No", "Sure") {
                override fun result(resObj: Any?) {
                    if (resObj == DIALOG_POSITIVE) TerminalControl.playServicesInterface.showSurvey()
                    else if (resObj == DIALOG_NEGATIVE) object : CustomDialog("Unlock airport", "Watch an ad to unlock $airport for 1 hour?", "No", "Sure") {
                        override fun result(resObj: Any?) {
                            if (resObj == DIALOG_POSITIVE) showAdOrDialogOnFail(airport)
                        }
                    }.show(stage)
                }
            }.show(stage)
        } else object : CustomDialog("Unlock airport", "Watch an ad to unlock $airport for 1 hour?", "No", "Sure") {
            override fun result(resObj: Any?) {
                if (resObj == DIALOG_POSITIVE) showAdOrDialogOnFail(airport)
            }
        }.show(stage)
    }

    /** Launches an ad, or displays a dialog if the ad fails to show */
    private fun showAdOrDialogOnFail(airport: String) {
        if (!TerminalControl.playServicesInterface.showAd(airport)) CustomDialog("Ad", "Failed to load ad", "", "Ok").show(stage)
    }
}