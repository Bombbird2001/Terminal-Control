package com.bombbird.terminalcontrol.screens.settingsscreen

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.PauseScreen
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.*
import com.bombbird.terminalcontrol.utilities.Fonts

class CategorySelectScreen(game: TerminalControl, background: Image?, val radarScreen: RadarScreen?) : StandardUIScreen(game, background) {
    /** Loads the different buttons to go to different settings screens  */
    override fun loadButtons() {
        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont16
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        val displayButton = TextButton("Display", buttonStyle)
        displayButton.setSize(MainMenuScreen.BUTTON_WIDTH.toFloat(), MainMenuScreen.BUTTON_HEIGHT.toFloat())
        displayButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH - 100, 1620 * 0.65f)
        displayButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = DisplaySettingsScreen(game, radarScreen, background)
            }
        })

        if (radarScreen == null || !radarScreen.tutorial) stage.addActor(displayButton)
        val dataTagButton = TextButton("Data tag", buttonStyle)
        dataTagButton.setSize(MainMenuScreen.BUTTON_WIDTH.toFloat(), MainMenuScreen.BUTTON_HEIGHT.toFloat())
        dataTagButton.setPosition(2880 / 2.0f + 100, 1620 * 0.65f)
        dataTagButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = DataTagSettingsScreen(game, radarScreen, background)
            }
        })

        if (radarScreen == null || !radarScreen.tutorial) stage.addActor(dataTagButton)
        val trafficButton = TextButton("Traffic", buttonStyle)
        trafficButton.setSize(MainMenuScreen.BUTTON_WIDTH.toFloat(), MainMenuScreen.BUTTON_HEIGHT.toFloat())
        trafficButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH - 100, 1620 * 0.5f)
        trafficButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = TrafficSettingsScreen(game, radarScreen, background)
            }
        })

        if (radarScreen == null || !radarScreen.tutorial) stage.addActor(trafficButton)
        val othersButton = TextButton("Others", buttonStyle)
        othersButton.setSize(MainMenuScreen.BUTTON_WIDTH.toFloat(), MainMenuScreen.BUTTON_HEIGHT.toFloat())
        othersButton.setPosition(2880 / 2.0f + 100, 1620 * 0.5f)
        othersButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = OtherSettingsScreen(game, radarScreen, background)
            }
        })

        stage.addActor(othersButton)
        val alertsButton = TextButton("Alerts", buttonStyle)
        alertsButton.setSize(MainMenuScreen.BUTTON_WIDTH.toFloat(), MainMenuScreen.BUTTON_HEIGHT.toFloat())
        alertsButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH - 100, 1620 * 0.35f)
        alertsButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = AlertsSettingsScreen(game, radarScreen, background)
            }
        })

        if ((radarScreen == null || !radarScreen.tutorial) && TerminalControl.full) stage.addActor(alertsButton)

        //Set button textures
        val buttonStyle1 = TextButton.TextButtonStyle()
        buttonStyle1.font = Fonts.defaultFont12
        buttonStyle1.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle1.down = TerminalControl.skin.getDrawable("Button_down")

        //Set back button params
        backButton = TextButton("<= Back", buttonStyle1)
        backButton.width = MainMenuScreen.BUTTON_WIDTH.toFloat()
        backButton.height = MainMenuScreen.BUTTON_HEIGHT.toFloat()
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f)
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu
                if (radarScreen != null) {
                    game.screen = PauseScreen(game, radarScreen)
                } else {
                    game.screen = MenuSettingsScreen(game, background)
                }
            }
        })
        stage.addActor(backButton)
    }

    override fun loadUI() {
        super.loadUI()
        loadLabel(if (radarScreen != null) "Settings" else "Default game settings")
        loadButtons()
    }
}