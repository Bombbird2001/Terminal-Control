package com.bombbird.terminalcontrol.screens.selectgamescreen

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.helpmanual.HelpSectionScreen

class HelpScreen(game: TerminalControl, background: Image?) : SelectGameScreen(game, background) {
    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label  */
    override fun loadLabel() {
        //Set label params
        super.loadLabel()
        val headerLabel = Label("Help Manual", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)
    }

    /** Overrides loadScroll method in SelectGameScreen to load airport info into scrollPane  */
    override fun loadScroll() {
        super.loadScroll()
        //Load help sections
        val sections = arrayOf("Airports", "Aircraft instructions", "Scoring", "ILS, LDA", "Separation", "MVAs, restricted areas", "NTZ", "Wake turbulence", "Advanced trajectory", "Conflict prediction alerts")
        for (section in sections) {
            val button = TextButton(section, listButtonStyle)
            button.name = section
            button.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    if ("Airports" == button.name) {
                        game.screen = AirportHelpScreen(game, background)
                    } else {
                        game.screen = HelpSectionScreen(game, background, button.name)
                    }
                }
            })
            scrollTable.add(button).width(MainMenuScreen.BUTTON_WIDTH * 1.2f).height(MainMenuScreen.BUTTON_HEIGHT)
            scrollTable.row()
        }
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.setupFadeScrollBars(1f, 1.5f)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.6f
        scrollPane.y = 1620 * 0.2f
        scrollPane.width = MainMenuScreen.BUTTON_WIDTH * 1.2f
        scrollPane.height = 1620 * 0.6f
        stage.addActor(scrollPane)
    }
}