package com.bombbird.terminalcontrol.screens.settingsscreen.categories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.DataTagLayoutScreen
import com.bombbird.terminalcontrol.ui.datatag.DataTagConfig
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.files.GameSaver

class DataTagSettingsScreen(game: TerminalControl, radarScreen: RadarScreen?, background: Image?) : SettingsTemplateScreen(game, radarScreen, background) {
    private lateinit var dataTag: SelectBox<String>
    private lateinit var bordersBackground: SelectBox<String>
    private lateinit var lineSpacing: SelectBox<String>
    private lateinit var dataTagLabel: Label
    private var datatagConfig = "Default"
    private lateinit var bordersBackgroundLabel: Label
    private var alwaysShowBordersBackground = false
    private lateinit var lineSpacingLabel: Label
    private var lineSpacingValue = TerminalControl.lineSpacingValue

    init {
        infoString = "Set the data tag display options below."
        loadUI(-1200, -200)
        setOptions()
    }

    /** Loads selectBox for display settings  */
    override fun loadBoxes() {
        dataTag = createStandardSelectBox()
        dataTag.setItems("Default", "Compact", "Manage layouts...")
        dataTag.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (dataTag.selected == "Manage layouts...") {
                    //New layout page
                    game.screen = DataTagLayoutScreen(game, background)
                } else datatagConfig = dataTag.selected
            }
        })

        bordersBackground = createStandardSelectBox()
        bordersBackground.setItems("Always", "When selected")
        bordersBackground.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                when (bordersBackground.selected) {
                    "Always" -> alwaysShowBordersBackground = true
                    "When selected" -> alwaysShowBordersBackground = false
                    else -> Gdx.app.log(className, "Unknown show borders setting " + dataTag.selected)
                }
            }
        })

        lineSpacing = createStandardSelectBox()
        lineSpacing.setItems("Compact", "Default", "Large")
        lineSpacing.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                lineSpacingValue = lineSpacing.selectedIndex
            }
        })
    }

    /** Loads labels for display settings  */
    override fun loadLabel() {
        super.loadLabel()
        dataTagLabel = Label("Data tag style: ", labelStyle)
        bordersBackgroundLabel = Label("Show data tag border\nand background: ", labelStyle)
        lineSpacingLabel = Label("Row spacing: ", labelStyle)
    }

    /** Loads actors for display settings into tabs  */
    override fun loadTabs() {
        val tab1 = SettingsTab(this, 2)
        tab1.addActors(dataTag, dataTagLabel)
        tab1.addActors(bordersBackground, bordersBackgroundLabel)
        tab1.addActors(lineSpacing, lineSpacingLabel)
        settingsTabs.add(tab1)
    }

    /** Sets relevant options into select boxes  */
    override fun setOptions() {
        if (radarScreen == null) {
            datatagConfig = TerminalControl.datatagConfig
            alwaysShowBordersBackground = TerminalControl.alwaysShowBordersBackground
            lineSpacingValue = TerminalControl.lineSpacingValue
        } else {
            datatagConfig = radarScreen.datatagConfig.name
            alwaysShowBordersBackground = radarScreen.alwaysShowBordersBackground
            lineSpacingValue = radarScreen.lineSpacingValue
        }
        dataTag.selected = datatagConfig
        bordersBackground.selected = if (alwaysShowBordersBackground) "Always" else "When selected"
        lineSpacing.selectedIndex = lineSpacingValue
    }

    /** Confirms and applies the changes set  */
    override fun sendChanges() {
        if (radarScreen != null) {
            radarScreen.datatagConfig = DataTagConfig(datatagConfig)
            radarScreen.alwaysShowBordersBackground = alwaysShowBordersBackground
            val lineSpacingChanged = radarScreen.lineSpacingValue != lineSpacingValue
            radarScreen.lineSpacingValue = lineSpacingValue

            //Apply the new line spacing to label
            if (lineSpacingChanged) {
                val newLabelStyle = Label.LabelStyle()
                newLabelStyle.fontColor = Color.WHITE
                when (radarScreen.lineSpacingValue) {
                    0 -> newLabelStyle.font = Fonts.compressedFont6
                    1 -> newLabelStyle.font = Fonts.defaultFont6
                    2 -> newLabelStyle.font = Fonts.expandedFont6
                    else -> {
                        newLabelStyle.font = Fonts.defaultFont6
                        Gdx.app.log(javaClass.name, "Unknown radarScreen line spacing value " + radarScreen.lineSpacingValue)
                    }
                }
                for (aircraft in radarScreen.aircrafts.values) {
                    //Apply the changes to all
                    aircraft.dataTag.updateLabelStyle(newLabelStyle)
                }
            }
        } else {
            TerminalControl.datatagConfig = datatagConfig
            TerminalControl.alwaysShowBordersBackground = alwaysShowBordersBackground
            TerminalControl.lineSpacingValue = lineSpacingValue
            GameSaver.saveSettings()
        }
    }
}