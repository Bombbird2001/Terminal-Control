package com.bombbird.terminalcontrol.screens.selectgamescreen

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.isTCHXAvailable
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.helpmanual.HelpSectionScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class AirportHelpScreen(game: TerminalControl, background: Image?) : SelectGameScreen(game, background) {
    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label  */
    override fun loadLabel() {
        //Set label params
        super.loadLabel()
        val headerLabel = Label("Airports", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH.toFloat()
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT.toFloat()
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)
    }

    /** Loads the default button styles and back button  */
    override fun loadButtons() {
        //Set button textures
        buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        //Set back button params
        backButton = TextButton("<= Back", buttonStyle)
        backButton.width = MainMenuScreen.BUTTON_WIDTH.toFloat()
        backButton.height = MainMenuScreen.BUTTON_HEIGHT.toFloat()
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f)
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu
                game.screen = HelpScreen(game, background)
            }
        })
        stage.addActor(backButton)
    }

    /** Overrides loadScroll method in SelectGameScreen to load airport info into scrollPane  */
    override fun loadScroll() {
        //Load help sections
        val airports = Array<String>()
        airports.add("TCTP", "TCWS")
        if (TerminalControl.full) {
            airports.add("TCTT", "TCHH", "TCBB", "TCBD")
            airports.add("TCMD", "TCPG")
        }
        if (isTCHXAvailable) airports.add("TCHX")
        for (arpt in airports) {
            val button = TextButton(arpt, buttonStyle)
            button.name = arpt
            button.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    game.screen = HelpSectionScreen(game, background, button.name)
                }
            })
            scrollTable.add(button).width(MainMenuScreen.BUTTON_WIDTH * 1.2f).height(MainMenuScreen.BUTTON_HEIGHT.toFloat())
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