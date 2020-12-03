package com.bombbird.terminalcontrol.screens.informationscreen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class PrivacyScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel()
        loadButtons()
    }

    /** Loads labels for credits, disclaimers, etc  */
    fun loadLabel() {
        super.loadLabel("Data & Privacy Policy")
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE
        val privacy = Label("""
            Terminal Control${if (TerminalControl.full) "" else ": Lite"} does NOT collect your personal data.
            Other data collected are for the sole purpose of diagnosing errors, crashes and bugs.
            
            The following data are collected through Google, LLC's Google Play Console:
            Device model
            Device OS version
            Game version
            Date, time of error occurrence
            Crash logs
            
            The following data are sent to our server for diagnosis, if you enable sending of anonymous crash reports:
            Game version
            Date, time of error occurrence
            Crash logs
            Game save data (if you choose to send it)
            """.trimIndent(), labelStyle)
        privacy.setAlignment(Align.center)
        privacy.setPosition(1440f - privacy.width / 2, 1400f - privacy.height)
        stage.addActor(privacy)
    }

    /** Overridden to go back to info screen instead of main menu screen */
    override fun loadButtons() {
        super.loadButtons()
        removeDefaultBackButtonChangeListener()
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                //Back to info screen
                game.screen = InfoScreen(game, background)
            }
        })
    }
}