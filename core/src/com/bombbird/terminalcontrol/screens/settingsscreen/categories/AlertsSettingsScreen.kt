package com.bombbird.terminalcontrol.screens.settingsscreen.categories

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.areaAvailable
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.collisionAvailable
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.trajAvailable
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.utilities.files.GameSaver

class AlertsSettingsScreen(game: TerminalControl, radarScreen: RadarScreen?, background: Image?) : SettingsTemplateScreen(game, radarScreen, background) {
    lateinit var advTraj: SelectBox<String>
    lateinit var area: SelectBox<String>
    lateinit var collision: SelectBox<String>
    private lateinit var advTrajLabel: Label
    var advTrajTime = -1
    private lateinit var areaLabel: Label
    var areaWarning = -1
    private lateinit var collisionLabel: Label
    var collisionWarning = -1

    init {
        infoString = "Set the options for advanced alerts below."
        loadUI(-1200, -200)
        setOptions()
    }

    /** Loads selectBox for display settings  */
    override fun loadBoxes() {
        advTraj = createStandardSelectBox()
        advTraj.items = trajAvailable
        advTraj.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                advTrajTime = if ("Off" == advTraj.selected) {
                    -1
                } else {
                    advTraj.selected.split(" ".toRegex()).toTypedArray()[0].toInt()
                }
            }
        })
        area = createStandardSelectBox()
        area.items = areaAvailable
        area.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                areaWarning = if ("Off" == area.selected) {
                    -1
                } else {
                    area.selected.split(" ".toRegex()).toTypedArray()[0].toInt()
                }
            }
        })
        collision = createStandardSelectBox()
        collision.items = collisionAvailable
        collision.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                collisionWarning = if ("Off" == collision.selected) {
                    -1
                } else {
                    collision.selected.split(" ".toRegex()).toTypedArray()[0].toInt()
                }
            }
        })
    }

    /** Loads labels for display settings  */
    override fun loadLabel() {
        super.loadLabel()
        advTrajLabel = Label("Advanced\ntrajectory: ", labelStyle)
        areaLabel = Label("Area\npenetration\nalert: ", labelStyle)
        collisionLabel = Label("Collision alert: ", labelStyle)
    }

    /** Loads actors for display settings into tabs  */
    override fun loadTabs() {
        val tab1 = SettingsTab(this, 2)
        tab1.addActors(advTraj, advTrajLabel)
        tab1.addActors(area, areaLabel)
        tab1.addActors(collision, collisionLabel)
        settingsTabs.add(tab1)
    }

    /** Sets relevant options into select boxes  */
    override fun setOptions() {
        if (radarScreen == null) {
            advTrajTime = TerminalControl.advTraj
            areaWarning = TerminalControl.areaWarning
            collisionWarning = TerminalControl.collisionWarning
        } else {
            advTrajTime = radarScreen.advTraj
            areaWarning = radarScreen.areaWarning
            collisionWarning = radarScreen.collisionWarning
        }
        advTraj.selected = if (advTrajTime == -1) "Off" else "$advTrajTime sec"
        area.selected = if (areaWarning == -1) "Off" else "$areaWarning sec"
        collision.selected = if (collisionWarning == -1) "Off" else "$collisionWarning sec"
    }

    /** Confirms and applies the changes set  */
    override fun sendChanges() {
        if (radarScreen != null) {
            radarScreen.advTraj = advTrajTime
            radarScreen.areaWarning = areaWarning
            radarScreen.collisionWarning = collisionWarning
        } else {
            TerminalControl.advTraj = advTrajTime
            TerminalControl.areaWarning = areaWarning
            TerminalControl.collisionWarning = collisionWarning
            GameSaver.saveSettings()
        }
    }
}