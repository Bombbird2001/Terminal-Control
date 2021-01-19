package com.bombbird.terminalcontrol.screens.settingsscreen.categories

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.sweepAvailable
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.WeatherScreen
import com.bombbird.terminalcontrol.utilities.files.GameSaver
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class OtherSettingsScreen(game: TerminalControl, radarScreen: RadarScreen?, background: Image?) : SettingsTemplateScreen(game, radarScreen, background) {
    lateinit var weather: SelectBox<String>
    lateinit var sound: SelectBox<String>
    lateinit var sweep: SelectBox<String>
    lateinit var storms: SelectBox<String>
    private lateinit var weatherLabel: Label
    lateinit var weatherSel: RadarScreen.Weather
    private lateinit var soundLabel: Label
    var soundSel = 0
    private lateinit var sweepLabel: Label
    var radarSweep = 0f
    private lateinit var stormLabel: Label
    var stormNumber = 0

    //In game only
    private lateinit var speed: SelectBox<String>
    private lateinit var speedLabel: Label
    private var speedSel = 0

    init {
        infoString = "Set miscellaneous options below."
        loadUI(-1200, -200)
        setOptions()
    }

    /** Loads selectBox for display settings  */
    override fun loadBoxes() {
        weather = createStandardSelectBox()
        val weatherModes = Array<String>()
        weatherModes.add("Live weather", "Random weather", "Static weather")
        if (radarScreen != null) weatherModes.add("Set custom weather...")
        weather.items = weatherModes
        weather.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if ("Set custom weather..." == weather.selected) {
                    //Go to weather change screen
                    game.screen = WeatherScreen(game)
                } else {
                    weatherSel = RadarScreen.Weather.valueOf(weather.selected.split(" ".toRegex()).toTypedArray()[0].toUpperCase(Locale.ROOT))
                }
            }
        })
        storms = createStandardSelectBox()
        val stormOptions = Array<String>()
        stormOptions.add("Off", "Low", "Medium", "High")
        stormOptions.add("Nightmare")
        storms.items = stormOptions
        storms.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                stormNumber = when (storms.selected) {
                    "Off" -> 0
                    "Low" -> 2
                    "Medium" -> 4
                    "High" -> 8
                    "Nightmare" -> 12
                    else -> {
                        Gdx.app.log(className, "Unknown storm amount setting " + storms.selected)
                        0
                    }
                }
            }
        })
        sound = createStandardSelectBox()
        val options = Array<String>()
        options.add("Pilot voices + sound effects", "Sound effects only", "Off")
        sound.items = options
        sound.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                soundSel = when (sound.selected) {
                    "Pilot voices + sound effects" -> 2
                    "Sound effects only" -> 1
                    "Off" -> 0
                    else -> {
                        Gdx.app.log(className, "Unknown sound setting " + sound.selected)
                        1
                    }
                }
            }
        })
        sweep = createStandardSelectBox()
        sweep.items = sweepAvailable
        sweep.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                radarSweep = sweep.selected.substring(0, sweep.selected.length - 1).toFloat()
            }
        })
        if (radarScreen != null) {
            speed = createStandardSelectBox()
            speed.setItems("1x", "2x", "4x")
            speed.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    speedSel = speed.selected[0].toInt() - 48
                }
            })
        }
    }

    /** Loads labels for display settings  */
    override fun loadLabel() {
        super.loadLabel()
        weatherLabel = Label("Weather: ", labelStyle)
        stormLabel = Label("Storms: ", labelStyle)
        soundLabel = Label("Sounds: ", labelStyle)
        sweepLabel = Label("Radar sweep: ", labelStyle)
        if (radarScreen != null) {
            speedLabel = Label("Speed: ", labelStyle)
        }
    }

    /** Loads actors for display settings into tabs  */
    override fun loadTabs() {
        if (radarScreen != null && radarScreen.tutorial) {
            setOptions()
            //Only show speed tab in tutorial settings
            val tab1 = SettingsTab(this, 2)
            tab1.addActors(speed, speedLabel)
            settingsTabs.add(tab1)
            return
        }
        val tab1 = SettingsTab(this, 2)
        tab1.addActors(weather, weatherLabel)
        tab1.addActors(storms, stormLabel)
        tab1.addActors(sound, soundLabel)
        tab1.addActors(sweep, sweepLabel)
        if (radarScreen != null) {
            tab1.addActors(speed, speedLabel)
        }
        settingsTabs.add(tab1)
    }

    /** Sets relevant options into select boxes  */
    override fun setOptions() {
        if (radarScreen != null) {
            speedSel = radarScreen.speed
            speed.selected = speedSel.toString() + "x"
            weatherSel = radarScreen.weatherSel
            stormNumber = radarScreen.stormNumber
            soundSel = radarScreen.soundSel
            radarSweep = radarScreen.radarSweepDelay
            if (radarScreen.tutorial) return
        } else {
            radarSweep = TerminalControl.radarSweep
            weatherSel = TerminalControl.weatherSel
            stormNumber = TerminalControl.stormNumber
            soundSel = TerminalControl.soundSel
        }
        weather.selected = weatherSel.toString()[0].toString() + weatherSel.toString().substring(1).toLowerCase(Locale.ROOT) + " weather"
        storms.selected = when (stormNumber) {
            0 -> "Off"
            2 -> "Low"
            4 -> "Medium"
            8 -> "High"
            12 -> "Nightmare"
            else -> "Off"
        }
        var soundIndex = (if (Gdx.app.type == Application.ApplicationType.Android) 2 else 1) - soundSel
        if (soundIndex < 0) soundIndex = 0
        sound.selectedIndex = soundIndex
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        sweep.selected = df.format(radarSweep.toDouble()) + "s"
    }

    /** Confirms and applies the changes set  */
    override fun sendChanges() {
        if (radarScreen != null) {
            val changedToLive = weatherSel === RadarScreen.Weather.LIVE && radarScreen.weatherSel !== RadarScreen.Weather.LIVE
            radarScreen.weatherSel = weatherSel
            radarScreen.stormNumber = stormNumber
            radarScreen.soundSel = soundSel
            radarScreen.speed = speedSel
            radarScreen.radarSweepDelay = radarSweep
            if (radarSweep < radarScreen.radarTime) radarScreen.radarTime = radarSweep
            if (changedToLive) radarScreen.metar.updateMetar(false) //If previous weather mode was not live, but now is changed to live, get live weather immediately
            if (stormNumber > 0 && radarScreen.thunderCellArray.isEmpty && radarScreen.stormSpawnTime > 10) radarScreen.stormSpawnTime = 10f //If setting was just turned on, set spawn time to 10
            Gdx.app.postRunnable { radarScreen.ui.updateInfoLabel() }
        } else {
            TerminalControl.weatherSel = weatherSel
            TerminalControl.stormNumber = stormNumber
            TerminalControl.soundSel = soundSel
            TerminalControl.radarSweep = radarSweep
            GameSaver.saveSettings()
        }
    }
}