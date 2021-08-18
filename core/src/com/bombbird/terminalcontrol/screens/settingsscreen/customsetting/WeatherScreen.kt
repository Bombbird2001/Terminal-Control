package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.OtherSettingsScreen
import com.bombbird.terminalcontrol.utilities.Fonts
import kotlin.collections.HashMap

/** The screen to set custom weather */
class WeatherScreen(game: TerminalControl) : BasicScreen(game, 5760, 3240) {
    private var boxMap: HashMap<String, Array<SelectBox<Int>>> = HashMap()
    private var visBoxMap: HashMap<String, SelectBox<String>> = HashMap()
    fun loadUI() {
        stage.clear()
        loadButton()
        loadLabel()
        loadOptions()
    }

    /** Loads the wind options for each airport  */
    private fun loadOptions() {
        val radarScreen = TerminalControl.radarScreen!!
        var currentY = (3240 * 0.65f).toInt()
        val labelStyle = LabelStyle()
        labelStyle.font = Fonts.defaultFont30
        labelStyle.fontColor = Color.WHITE
        for (airport in radarScreen.airports.values) {
            val boxes = Array<SelectBox<Int>>()
            val airportLabel = Label(airport.icao + ": ", labelStyle)
            airportLabel.setPosition(5760 * 0.05f, currentY.toFloat())
            airportLabel.height = 300f
            airportLabel.setAlignment(Align.left)
            stage.addActor(airportLabel)
            val atLabel = Label("@", labelStyle)
            atLabel.setPosition(5760 * 0.35f, currentY.toFloat())
            atLabel.height = 300f
            atLabel.setAlignment(Align.left)
            stage.addActor(atLabel)
            val knotsLabel = Label("kts", labelStyle)
            knotsLabel.setPosition(5760 * 0.525f, currentY.toFloat())
            knotsLabel.height = 300f
            knotsLabel.setAlignment(Align.left)
            stage.addActor(knotsLabel)

            //Hundreds place for heading
            val hdg1 = generateSmallBoxes()
            hdg1.setPosition(5760 * 0.15f, currentY.toFloat())
            hdg1.setItems(0, 1, 2, 3)
            stage.addActor(hdg1)
            boxes.add(hdg1)

            //Tens place for heading
            val hdg2 = generateSmallBoxes()
            hdg2.setPosition(hdg1.x + 325, currentY.toFloat())
            hdg2.setItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            stage.addActor(hdg2)
            boxes.add(hdg2)

            //Ones place for heading
            val hdg3 = generateSmallBoxes()
            hdg3.setPosition(hdg2.x + 325, currentY.toFloat())
            hdg3.setItems(0, 5)
            stage.addActor(hdg3)
            boxes.add(hdg3)

            //Tens place for speed
            val spd1 = generateSmallBoxes()
            spd1.setPosition(5760 * 0.4f, currentY.toFloat())
            spd1.setItems(0, 1, 2, 3)
            stage.addActor(spd1)
            boxes.add(spd1)

            //Ones place for speed
            val spd2 = generateSmallBoxes()
            spd2.setPosition(spd1.x + 325, currentY.toFloat())
            spd2.setItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            stage.addActor(spd2)
            boxes.add(spd2)
            boxMap[airport.icao] = boxes

            //Set the current weather for the airport into the boxes
            val windDir = airport.winds[0]
            val windSpd = airport.winds[1]
            hdg1.selected = windDir / 100
            hdg2.selected = windDir / 10 % 10
            hdg3.selected = windDir % 10
            spd1.selected = windSpd / 10
            spd2.selected = windSpd % 10
            modulateHdg(hdg1, hdg2, hdg3)

            //Set listeners after initializing all boxes
            hdg1.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    modulateHdg(hdg1, hdg2, hdg3)
                }
            })
            hdg2.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    modulateHdg(hdg1, hdg2, hdg3)
                }
            })

            val visBox = SelectBox<String>(SettingsTemplateScreen.selectBoxStyle)
            visBox.setSize(500f, 300f)
            visBox.setAlignment(Align.center)
            visBox.list.setAlignment(Align.center)
            visBox.setPosition(5760 * 0.7f, currentY.toFloat())
            visBox.setItems("500", "1000", "2000", "3000", "4000", "5000", "6000", "7000", "8000", "9000", ">9999")
            visBox.selected = if (airport.visibility > 9900) ">9999" else if (airport.visibility < 1000) "500" else ((airport.visibility / 1000f).toInt() * 1000).toString()
            visBoxMap[airport.icao] = visBox

            val visLabel = Label("Visbility:                   m", labelStyle)
            visLabel.setPosition(5760 * 0.6f, currentY.toFloat())
            visLabel.height = 300f
            visLabel.setAlignment(Align.left)
            stage.addActor(visLabel)
            stage.addActor(visBox)

            currentY -= 486
        }
    }

    /** Ensures heading is valid, does not exceed 360  */
    private fun modulateHdg(hdg1: SelectBox<Int>, hdg2: SelectBox<Int>, hdg3: SelectBox<Int>) {
        if (hdg1.selected == 3) {
            hdg2.setItems(0, 1, 2, 3, 4, 5, 6)
        } else {
            hdg2.setItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            hdg3.setItems(0, 5)
        }
        if (hdg1.selected == 3 && hdg2.selected == 6) {
            hdg3.setItems(0)
        } else {
            hdg3.setItems(0, 5)
        }
    }

    /** Generates a standard small selectBox  */
    private fun generateSmallBoxes(): SelectBox<Int> {
        val box = SelectBox<Int>(SettingsTemplateScreen.selectBoxStyle)
        box.setSize(300f, 300f)
        box.setAlignment(Align.center)
        box.list.setAlignment(Align.center)
        return box
    }

    /** Loads heading label  */
    private fun loadLabel() {
        //Set label params
        val labelStyle = LabelStyle()
        labelStyle.font = Fonts.defaultFont30
        labelStyle.fontColor = Color.WHITE
        val headerLabel = Label("Custom Weather", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT
        headerLabel.setPosition(5760 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 3240 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)
        val labelStyle1 = LabelStyle()
        labelStyle1.font = Fonts.defaultFont20
        labelStyle1.fontColor = Color.WHITE
        val noteLabel = Label("Note: Use HDG 000 for variable (VRB) wind direction", labelStyle1)
        noteLabel.setPosition(5760 / 2.0f - noteLabel.width / 2.0f, 3240 * 0.8f)
        noteLabel.setAlignment(Align.center)
        stage.addActor(noteLabel)
    }

    /** Loads buttons  */
    private fun loadButton() {
        val textButtonStyle = TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont30
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        val cancelButton = TextButton("Cancel", textButtonStyle)
        cancelButton.setSize(1200f, 300f)
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800f)
        cancelButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = OtherSettingsScreen(game, TerminalControl.radarScreen, null)
            }
        })
        stage.addActor(cancelButton)

        val confirmButton = TextButton("Confirm", textButtonStyle)
        confirmButton.setSize(1200f, 300f)
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800f)
        confirmButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val newData = HashMap<String, IntArray>()
                for ((key, value) in boxMap) {
                    val windHdg = value[0].selected * 100 + value[1].selected * 10 + value[2].selected
                    val windSpd = value[3].selected * 10 + value[4].selected
                    val visibility = visBoxMap[key]?.selected?.let {
                        if (it == ">9999") 10000
                        else it.toInt()
                    } ?: 10000
                    newData[key] = intArrayOf(windHdg, windSpd, visibility)
                }
                TerminalControl.radarScreen?.weatherSel = RadarScreen.Weather.STATIC
                Gdx.app.postRunnable { TerminalControl.radarScreen?.metar?.updateCustomWeather(newData) }
                game.screen = OtherSettingsScreen(game, TerminalControl.radarScreen, null)
            }
        })
        stage.addActor(confirmButton)
    }

    /** Overrides show method of basic screen  */
    override fun show() {
        loadUI()
    }
}