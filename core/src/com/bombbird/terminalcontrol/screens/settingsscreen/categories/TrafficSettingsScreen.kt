package com.bombbird.terminalcontrol.screens.settingsscreen.categories

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.TrafficFlowScreen
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.saving.GameSaver
import java.util.*

class TrafficSettingsScreen(game: TerminalControl, radarScreen: RadarScreen?, background: Image?) : SettingsTemplateScreen(game, radarScreen, background) {
    lateinit var emer: SelectBox<String>
    lateinit var emerChanceLabel: Label
    lateinit var emerChance: Emergency.Chance

    //In game only
    private lateinit var tfcMode: SelectBox<String>
    private lateinit var night: SelectBox<String>
    private lateinit var nightStartHour: SelectBox<String>
    private lateinit var nightStartMin: SelectBox<String>
    private lateinit var nightEndHour: SelectBox<String>
    private lateinit var nightEndMin: SelectBox<String>
    private lateinit var flowButton: TextButton
    private lateinit var tfcLabel: Label
    private lateinit var tfcSel: RadarScreen.TfcMode
    private lateinit var nightLabel: Label
    private lateinit var timeLabel: Label
    private lateinit var timeLabel2: Label
    private var allowNight = false
    private var nightStart = 0
    private var nightEnd = 0

    init {
        infoString = "Set the traffic settings below."
        loadUI(-1200, -200)
        setOptions()
        updateTabs(true)
    }

    /** Loads selectBox for display settings  */
    override fun loadBoxes() {
        emer = createStandardSelectBox()
        emer.setItems("Off", "Low", "Medium", "High")
        emer.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                emerChance = Emergency.Chance.valueOf(emer.selected.toUpperCase(Locale.US))
            }
        })

        if (radarScreen != null) {
            tfcMode = createStandardSelectBox()
            tfcMode.setItems("Normal", "Arrivals only")
            tfcMode.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    tfcSel = RadarScreen.TfcMode.valueOf(tfcMode.selected.toUpperCase(Locale.US).replace(" ".toRegex(), "_"))
                }
            })
            night = createStandardSelectBox()
            night.setItems("On", "Off")
            night.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    allowNight = "On" == night.selected
                    updateTabs(true)
                }
            })
            night.name = "night2"
            nightStartHour = SelectBox(selectBoxStyle)
            val options7 = Array<String>(24)
            for (i in 0..23) {
                var hour = i.toString()
                if (hour.length == 1) hour = "0$hour"
                options7.add(hour)
            }
            nightStartHour.items = options7
            nightStartHour.setSize(300f, 300f)
            nightStartHour.setAlignment(Align.center)
            nightStartHour.list.setAlignment(Align.center)
            nightStartHour.name = "night"
            nightStartMin = SelectBox(selectBoxStyle)
            nightStartMin.setItems("00", "15", "30", "45")
            nightStartHour.addListener(object : ChangeListener() {
                //Put here to prevent any potential NPE
                override fun changed(event: ChangeEvent, actor: Actor) {
                    nightStart = nightStartHour.selected.toInt() * 100 + nightStartMin.selected.toInt()
                }
            })
            nightStartMin.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    nightStart = nightStartHour.selected.toInt() * 100 + nightStartMin.selected.toInt()
                }
            })
            nightStartMin.setSize(300f, 300f)
            nightStartMin.setPosition(400f, 0f)
            nightStartMin.setAlignment(Align.center)
            nightStartMin.list.setAlignment(Align.center)
            nightStartMin.name = "night"
            nightEndHour = SelectBox(selectBoxStyle)
            nightEndHour.items = options7
            nightEndHour.setSize(300f, 300f)
            nightEndHour.setPosition(950f, 0f)
            nightEndHour.setAlignment(Align.center)
            nightEndHour.list.setAlignment(Align.center)
            nightEndHour.name = "night"
            nightEndMin = SelectBox(selectBoxStyle)
            nightEndMin.setItems("00", "15", "30", "45")
            nightEndHour.addListener(object : ChangeListener() {
                //Put here to prevent any potential NPE
                override fun changed(event: ChangeEvent, actor: Actor) {
                    nightEnd = nightEndHour.selected.toInt() * 100 + nightEndMin.selected.toInt()
                }
            })
            nightEndMin.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    nightEnd = nightEndHour.selected.toInt() * 100 + nightEndMin.selected.toInt()
                }
            })
            nightEndMin.setSize(300f, 300f)
            nightEndMin.setPosition(1350f, 0f)
            nightEndMin.setAlignment(Align.center)
            nightEndMin.list.setAlignment(Align.center)
            nightEndMin.name = "night"
        }
    }

    /** Loads labels for display settings  */
    override fun loadLabel() {
        super.loadLabel()
        emerChanceLabel = Label("Emergencies: ", labelStyle)
        if (radarScreen != null) {
            tfcLabel = Label("Traffic: ", labelStyle)
            nightLabel = Label("Night mode: ", labelStyle)
            nightLabel.name = "night2"
            timeLabel = Label("Active from:", labelStyle)
            timeLabel.name = "night"
            timeLabel2 = Label("to", labelStyle)
            timeLabel2.setPosition(nightStartHour.x - nightLabel.width + 1300, nightStartHour.y + nightStartHour.height / 2 - timeLabel2.height / 2)
            timeLabel2.name = "night"
        }
    }

    override fun loadButton() {
        super.loadButton()
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        flowButton = TextButton("Arrival traffic settings", textButtonStyle)
        flowButton.setSize(1200f, 300f)
        flowButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //New traffic page
                game.screen = TrafficFlowScreen(game)
                event.handle()
            }
        })
    }

    /** Loads actors for display settings into tabs  */
    override fun loadTabs() {
        val tab1 = SettingsTab(this, 2)
        tab1.addActors(emer, emerChanceLabel)
        if (radarScreen != null) {
            tab1.addActors(tfcMode, tfcLabel)
            tab1.addActors(night, nightLabel)
            tab1.addActors(nightStartHour, timeLabel, nightStartMin, nightEndHour, nightEndMin, timeLabel2)
            tab1.addButton(flowButton)
        }
        settingsTabs.add(tab1)
    }

    /** Sets relevant options into select boxes  */
    override fun setOptions() {
        emerChance = radarScreen?.emerChance ?: TerminalControl.emerChance
        val tmp = emerChance.toString().toLowerCase(Locale.US)
        emer.selected = tmp.substring(0, 1).toUpperCase(Locale.ROOT) + tmp.substring(1)
        if (radarScreen != null) {
            tfcSel = radarScreen.tfcMode
            val tmp2 = tfcSel.toString().toLowerCase(Locale.US)
            tfcMode.selected = (tmp2.substring(0, 1).toUpperCase(Locale.ROOT) + tmp2.substring(1)).replace("_".toRegex(), " ")
            allowNight = radarScreen.allowNight
            night.selected = if (allowNight) "On" else "Off"
            nightStart = radarScreen.nightStart
            var hr = (nightStart / 100).toString()
            if (hr.length == 1) hr = "0$hr"
            nightStartHour.selected = hr
            nightStartMin.selected = (nightStart % 100).toString()
            nightEnd = radarScreen.nightEnd
            var hr2 = (nightEnd / 100).toString()
            if (hr2.length == 1) hr2 = "0$hr2"
            nightEndHour.selected = hr2
            nightEndMin.selected = (nightEnd % 100).toString()
        }
    }

    /** Confirms and applies the changes set  */
    override fun sendChanges() {
        if (radarScreen == null) {
            TerminalControl.emerChance = emerChance
            GameSaver.saveSettings()
        } else {
            radarScreen.emerChance = emerChance
            radarScreen.tfcMode = tfcSel
            val changed = radarScreen.allowNight != allowNight || radarScreen.nightStart != nightStart || radarScreen.nightEnd != nightEnd
            radarScreen.allowNight = allowNight
            radarScreen.nightStart = nightStart
            radarScreen.nightEnd = nightEnd
            if (changed) {
                for (airport in radarScreen.airports.values) {
                    airport.updateRunwayUsage() //Possible change in runway usage
                }
            }
            radarScreen.ui.updateInfoLabel()
        }
    }

    fun isAllowNight(): Boolean {
        return allowNight && radarScreen != null
    }
}