package com.bombbird.terminalcontrol.screens.settingsscreen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.files.GameSaver

class GlobalSettingsScreen(game: TerminalControl, background: Image?) : SettingsTemplateScreen(game, null, background) {
    private var sendCrash = false
    private lateinit var sendCrashBox: CheckBox
    private lateinit var sendLabel: Label
    private lateinit var zoom: SelectBox<String>
    private lateinit var zoomLabel: Label
    private var increaseZoom = false
    private lateinit var autosave: SelectBox<String>
    private lateinit var autosaveLabel: Label
    private var saveInterval = 0
    private lateinit var defaultTab: SelectBox<String>
    private lateinit var defaultTabLabel: Label
    private var defaultTabNo = 0

    init {
        infoString = "Set the global game settings below."
        loadUI(-1200, -200)
        setOptions()
    }

    override fun setButtonListeners() {
        cancelButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                background?.scaleBy(-specialScale)
                game.screen = MenuSettingsScreen(game, background)
            }
        })
        confirmButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                sendChanges()
                background?.scaleBy(-specialScale)
                game.screen = MenuSettingsScreen(game, background)
            }
        })
    }

    /** Loads selectBox for display settings  */
    override fun loadBoxes() {
        val checkBoxStyle = CheckBox.CheckBoxStyle()
        checkBoxStyle.checkboxOn = TerminalControl.skin.getDrawable("Checked")
        checkBoxStyle.checkboxOff = TerminalControl.skin.getDrawable("Unchecked")
        checkBoxStyle.font = Fonts.defaultFont20
        checkBoxStyle.fontColor = Color.WHITE
        sendCrashBox = CheckBox(" Send anonymous crash reports", checkBoxStyle)
        sendCrashBox.setPosition(5760 / 2f + 400, 3240 * 0.7f)
        sendCrashBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                sendCrash = sendCrashBox.isChecked
            }
        })
        stage.addActor(sendCrashBox)

        zoom = createStandardSelectBox()
        zoom.setItems("Off", "On")
        zoom.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                increaseZoom = "On" == zoom.selected
            }
        })

        autosave = createStandardSelectBox()
        autosave.setItems("Never", "30 sec", "1 min", "2 mins", "5 mins")
        autosave.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val selected = autosave.selected
                when {
                    selected == "Never" -> saveInterval = -1
                    selected.contains("sec") -> saveInterval = selected.split(" ".toRegex()).toTypedArray()[0].toInt()
                    selected.contains("min") -> saveInterval = selected.split(" ".toRegex()).toTypedArray()[0].toInt() * 60
                    else -> Gdx.app.log("Default settings", "Invalid autosave setting: $selected")
                }
            }
        })

        defaultTab = createStandardSelectBox()
        defaultTab.setItems("Lateral", "Altitude", "Speed")
        defaultTab.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                when (val selected = defaultTab.selected) {
                    "Lateral" -> defaultTabNo = 0
                    "Altitude" -> defaultTabNo = 1
                    "Speed" -> defaultTabNo = 2
                    else -> Gdx.app.log(javaClass.name, "Unknown default tab $selected selected")
                }
            }
        })

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        buttonStyle.font = Fonts.defaultFont20
        buttonStyle.fontColor = Color.WHITE
        val consentButton = TextButton("Ad Consent", buttonStyle)
        consentButton.setSize(1200f, 300f)
        consentButton.setPosition(sendCrashBox.x, 3240 * (0.75f - 2 * 0.15f) + yOffset)
        consentButton.label.setAlignment(Align.center)
        consentButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                TerminalControl.playServicesInterface.showAdConsentForm(false)
            }
        })
        stage.addActor(consentButton)
    }

    /** Loads labels for display settings  */
    override fun loadLabel() {
        super.loadLabel()
        sendLabel = Label("Sending anonymous crash reports will allow\nus to improve your game experience.\nNo personal or device information will be\nsent.", labelStyle)
        sendLabel.setPosition(sendCrashBox.x, sendCrashBox.y - 475)
        stage.addActor(sendLabel)
        zoomLabel = Label("Increased radar zoom: ", labelStyle)
        autosaveLabel = Label("Autosave interval: ", labelStyle)
        defaultTabLabel = Label("Default UI tab: ", labelStyle)
    }

    /** Loads actors for display settings into tabs  */
    override fun loadTabs() {
        val tab1 = SettingsTab(this, 1)
        tab1.addActors(zoom, zoomLabel)
        tab1.addActors(autosave, autosaveLabel)
        tab1.addActors(defaultTab, defaultTabLabel)
        settingsTabs.add(tab1)
    }

    /** Sets relevant options into select boxes  */
    override fun setOptions() {
        sendCrash = TerminalControl.sendAnonCrash
        increaseZoom = TerminalControl.increaseZoom
        saveInterval = TerminalControl.saveInterval
        defaultTabNo = TerminalControl.defaultTabNo
        sendCrashBox.isChecked = sendCrash
        zoom.selected = if (increaseZoom) "On" else "Off"
        when {
            saveInterval == -1 -> autosave.setSelected("Never")
            saveInterval < 60 -> autosave.setSelected("$saveInterval sec")
            else -> {
                val min = saveInterval / 60
                autosave.setSelected(min.toString() + " min" + if (min > 1) "s" else "")
            }
        }
        defaultTab.selectedIndex = defaultTabNo
    }

    /** Confirms and applies the changes set  */
    override fun sendChanges() {
        TerminalControl.sendAnonCrash = sendCrash
        TerminalControl.increaseZoom = increaseZoom
        TerminalControl.saveInterval = saveInterval
        TerminalControl.defaultTabNo = defaultTabNo
        GameSaver.saveSettings()
    }

    override fun updateTabs(screenActive: Boolean) {
        super.updateTabs(screenActive)
        sendCrashBox.isVisible = tab == 0
        sendLabel.isVisible = tab == 0
    }
}