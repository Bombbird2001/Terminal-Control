package com.bombbird.terminalcontrol.screens.informationscreen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class OpenSourceScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
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
        val licenses = Label("""
            Open source software/libraries used:
            
            libGDX - Apache License 2.0
            OkHttp3 - Apache License 2.0
            Apache Commons Lang - Apache License 2.0
            Open Sans font - Apache License 2.0
            JSON In Java - JSON License
            """.trimIndent(), labelStyle)
        licenses.setPosition(1440 - licenses.width / 2f, 700f)
        stage.addActor(licenses)
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