package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.bombbird.terminalcontrol.TerminalControl
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
            Copyright © 2018-2020, Bombbird
            Version ${TerminalControl.versionName}, build ${TerminalControl.versionCode}
            """.trimIndent(), labelStyle)
        copyright.setPosition(918f, 1375f)
        stage.addActor(copyright)
        val licenses = Label("""
            Open source software/libraries used:
            
            libGDX - Apache License 2.0
            JSON In Java - JSON License
            OkHttp3 - Apache License 2.0
            Apache Commons Lang - Apache License 2.0
            Open Sans font - Apache License 2.0
            """.trimIndent(), labelStyle)
        licenses.setPosition(1440 - licenses.width / 2f, 825f)
        stage.addActor(licenses)
        val disclaimer = Label("While we make effort to ensure that this game is as realistic as possible, " +
            "please note that this game is not a completely accurate representation of real life air traffic control " +
            "and should not be used for purposes such as real life training. SID, STAR and other navigation data are fictitious " +
            "and should never be used for real life navigation. Names used are fictional, any resemblance to real world entities " +
            "is purely coincidental.", labelStyle)
        disclaimer.setWrap(true)
        disclaimer.width = 2400f
        disclaimer.setPosition(1465 - disclaimer.width / 2f, 460f)
        stage.addActor(disclaimer)
    }
}