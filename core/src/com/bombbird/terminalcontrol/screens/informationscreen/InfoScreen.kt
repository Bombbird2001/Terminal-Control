package com.bombbird.terminalcontrol.screens.informationscreen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class InfoScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel()
        loadButtons()
    }

    /** Loads labels for credits, disclaimers, etc  */
    fun loadLabel() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE
        val copyright = Label("""
            Terminal Control${if (TerminalControl.full) "" else ": Lite"}
            Copyright © 2018-2023, Bombbird
            All rights reserved
            Version ${TerminalControl.versionName}, build ${TerminalControl.versionCode}
            """.trimIndent(), labelStyle)
        copyright.setPosition(918f, 1300f)
        stage.addActor(copyright)
        val disclaimer = Label("While we make effort to ensure that this game is as realistic as possible, " +
            "please note that this game is not a completely accurate representation of real life air traffic control " +
            "and must not be used for purposes such as real life training. SID, STAR and other navigation data are fictitious " +
            "and must never be used for real life navigation. Names used are fictional, any resemblance to real world entities " +
            "is purely coincidental.", labelStyle)
        disclaimer.wrap = true
        disclaimer.width = 2400f
        disclaimer.setPosition(1465 - disclaimer.width / 2f, 460f)
        stage.addActor(disclaimer)
    }

    /** Also loads buttons to privacy policy, open source screen */
    override fun loadButtons() {
        super.loadButtons()

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        val openLicense = TextButton("Software & Licenses", buttonStyle)
        openLicense.setSize(MainMenuScreen.BUTTON_WIDTH / 1.5f, MainMenuScreen.BUTTON_HEIGHT)
        openLicense.setPosition(1440f - openLicense.width / 2, 1010f)
        openLicense.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                //Go to open source screen
                game.screen = OpenSourceScreen(game, background)
            }
        })
        stage.addActor(openLicense)

        val privacy = TextButton("Data & Privacy Policy", buttonStyle)
        privacy.setSize(MainMenuScreen.BUTTON_WIDTH / 1.5f, MainMenuScreen.BUTTON_HEIGHT)
        privacy.setPosition(1440f - privacy.width / 2, 760f)
        privacy.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                //Go to open source screen
                game.screen = PrivacyScreen(game, background)
            }
        })
        stage.addActor(privacy)
    }
}