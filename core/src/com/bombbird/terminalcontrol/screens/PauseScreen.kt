package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.CategorySelectScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.OtherSettingsScreen
import com.bombbird.terminalcontrol.screens.upgradescreen.AchievementScreen
import com.bombbird.terminalcontrol.screens.upgradescreen.UpgradeScreen
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.saving.GameSaver

class PauseScreen(game: TerminalControl, private val radarScreen: RadarScreen) : BasicScreen(game, 5760, 3240) {
    /** Loads the buttons for screen  */
    private fun loadButtons() {
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont30
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        val resumeButton = TextButton("Resume", textButtonStyle)
        resumeButton.setSize(1200f, 300f)
        resumeButton.setPosition((5760 - 1200) / 2f, 3240 - 1200.toFloat())
        resumeButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Un-pause the game
                radarScreen.setGameRunning(true)
                event.handle()
            }
        })
        stage.addActor(resumeButton)
        val settingsButton = TextButton("Settings", textButtonStyle)
        settingsButton.setSize(1200f, 300f)
        settingsButton.setPosition((5760 - 1200) / 2f, 3240 - 1600.toFloat())
        settingsButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Change to settings state
                if (radarScreen.tutorial) {
                    game.screen = OtherSettingsScreen(game, radarScreen, null)
                } else {
                    game.screen = CategorySelectScreen(game, null, radarScreen)
                }
            }
        })
        stage.addActor(settingsButton)
        val quitButton = TextButton(if (radarScreen.tutorial) "Quit" else "Save & Quit", textButtonStyle)
        quitButton.setSize(1200f, 300f)
        quitButton.setPosition((5760 - 1200) / 2f, 3240 - 2000.toFloat())
        quitButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu screen
                radarScreen.metar.isQuit = true
                if (!radarScreen.tutorial) GameSaver.saveGame() //Save the game first
                TerminalControl.radarScreen = null
                radarScreen.game.screen = MainMenuScreen(radarScreen.game, null)
                radarScreen.dispose()
            }
        })
        stage.addActor(quitButton)
        val buttonWidthSmall = MainMenuScreen.BUTTON_WIDTH_SMALL * 2
        val buttonHeightSmall = MainMenuScreen.BUTTON_HEIGHT_SMALL * 2

        //Set upgrade button (for full version only)
        if (TerminalControl.full) {
            val imageButtonStyle3 = ImageButton.ImageButtonStyle()
            imageButtonStyle3.imageUp = TerminalControl.skin.getDrawable("Upgrade_up")
            imageButtonStyle3.imageUp.minWidth = buttonWidthSmall
            imageButtonStyle3.imageUp.minHeight = buttonHeightSmall
            imageButtonStyle3.imageDown = TerminalControl.skin.getDrawable("Upgrade_down")
            imageButtonStyle3.imageDown.minWidth = buttonWidthSmall
            imageButtonStyle3.imageDown.minHeight = buttonHeightSmall
            val upgradeButton = ImageButton(imageButtonStyle3)
            upgradeButton.setPosition(5760 - buttonWidthSmall, 1790 + 50.toFloat())
            upgradeButton.setSize(buttonWidthSmall, buttonHeightSmall)
            upgradeButton.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    //Go to upgrade screen
                    game.screen = UpgradeScreen(game, null)
                }
            })
            stage.addActor(upgradeButton)
        }

        //Set achievement button
        val imageButtonStyle4 = ImageButton.ImageButtonStyle()
        imageButtonStyle4.imageUp = TerminalControl.skin.getDrawable("Medal_up")
        imageButtonStyle4.imageUp.minWidth = buttonWidthSmall
        imageButtonStyle4.imageUp.minHeight = buttonHeightSmall
        imageButtonStyle4.imageDown = TerminalControl.skin.getDrawable("Medal_down")
        imageButtonStyle4.imageDown.minWidth = buttonWidthSmall
        imageButtonStyle4.imageDown.minHeight = buttonHeightSmall
        val achievementButton = ImageButton(imageButtonStyle4)
        achievementButton.setPosition(5760 - buttonWidthSmall, if (TerminalControl.full) 1790f - 50 - buttonHeightSmall else 1790 - buttonHeightSmall / 2.0f)
        achievementButton.setSize(buttonWidthSmall, buttonHeightSmall)
        achievementButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go to upgrade screen
                game.screen = AchievementScreen(game, null)
            }
        })
        stage.addActor(achievementButton)
    }

    /** Loads the stats label on left side of screen  */
    private fun loadLabels() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont24
        labelStyle.fontColor = Color.WHITE
        val stringBuilder = StringBuilder()
        val arrStr = if (radarScreen.arrivals == 1) " arrival" else " arrivals"
        stringBuilder.append(radarScreen.arrivals).append(arrStr).append(" in control\n")
        val depStr = if (radarScreen.departures == 1) " departure" else " departures"
        stringBuilder.append(radarScreen.departures).append(depStr).append(" in control\n")
        val sepStr = if (radarScreen.separationIncidents == 1) "incident" else "incidents"
        stringBuilder.append(radarScreen.separationIncidents).append(" total separation ").append(sepStr).append("\n")
        stringBuilder.append(radarScreen.wakeInfringeTime.toInt()).append("s of wake separation infringement\n")
        stringBuilder.append(radarScreen.emergenciesLanded).append(" emergency aircraft landed\n")
        val totalSecs = radarScreen.playTime.toInt()
        val hrs = totalSecs / 3600
        val mins = totalSecs % 3600 / 60
        val minStr = if (mins < 10) "0$mins" else mins.toString()
        val sec = totalSecs % 60
        val secStr = if (sec < 10) "0$sec" else sec.toString()
        stringBuilder.append(hrs).append(":").append(minStr).append(":").append(secStr).append(" of total play time")
        val statsLabel = Label(stringBuilder.toString(), labelStyle)
        statsLabel.setPosition(100f, 1790 - statsLabel.height / 2)
        stage.addActor(statsLabel)
    }

    init {
        camera.position[2880f, 1620f] = 0f
        loadButtons()
        loadLabels()
    }
}