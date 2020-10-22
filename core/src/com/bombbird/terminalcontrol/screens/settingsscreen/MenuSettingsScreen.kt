package com.bombbird.terminalcontrol.screens.settingsscreen

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class MenuSettingsScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    /** Loads the different buttons to go to different settings screens  */
    override fun loadButtons() {
        super.loadButtons()

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont16
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        val gameSettingsButton = TextButton("Default game settings", buttonStyle)
        gameSettingsButton.setSize(MainMenuScreen.BUTTON_WIDTH.toFloat(), MainMenuScreen.BUTTON_HEIGHT.toFloat())
        gameSettingsButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.65f)
        gameSettingsButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = CategorySelectScreen(game, background, null)
            }
        })
        stage.addActor(gameSettingsButton)
        val globalSettingsButton = TextButton("Global settings", buttonStyle)
        globalSettingsButton.setSize(MainMenuScreen.BUTTON_WIDTH.toFloat(), MainMenuScreen.BUTTON_HEIGHT.toFloat())
        globalSettingsButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.5f)
        globalSettingsButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = GlobalSettingsScreen(game, background)
            }
        })
        stage.addActor(globalSettingsButton)
    }

    override fun loadUI() {
        stage.addActor(background)
        super.loadUI()
        loadLabel("Settings")
        loadButtons()
    }
}