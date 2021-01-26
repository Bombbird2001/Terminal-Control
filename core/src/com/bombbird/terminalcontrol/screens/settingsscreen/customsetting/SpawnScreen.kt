package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.TrafficSettingsScreen
import com.bombbird.terminalcontrol.utilities.Fonts
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog


class SpawnScreen(game: TerminalControl): BasicScreen(game, 5760, 3240) {
    private val radarScreen = TerminalControl.radarScreen!!

    private lateinit var airportBox: SelectBox<String>
    private lateinit var airlineBox: SelectBox<String>
    private lateinit var callsignField: TextField
    private lateinit var typeBox: SelectBox<String>
    private lateinit var starBox: SelectBox<String>

    /** Load the UI */
    fun loadUI() {
        stage.clear()
        loadButton()
        loadLabel()
        loadOptions()
    }

    /** Load the label describing each field */
    private fun loadLabel() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE

        val headerLabel = Label("Spawn custom arrival aircraft", labelStyle)
        headerLabel.setPosition(5760 / 2.0f - headerLabel.width / 2.0f, 3240 - 300f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)

        val airportLabel = Label("Airport: ", labelStyle)
        airportLabel.setPosition(1340f - airportLabel.width, 3240 * 0.8f + 150 - airportLabel.height / 2)
        stage.addActor(airportLabel)

        val callsignLabel = Label("Callsign: ", labelStyle)
        callsignLabel.setPosition(1340f - callsignLabel.width, 3240 * 0.65f + 150 - callsignLabel.height / 2)
        stage.addActor(callsignLabel)

        val typeLabel = Label("Aircraft type: ", labelStyle)
        typeLabel.setPosition(1340f - typeLabel.width, 3240 * 0.5f + 150 - typeLabel.height / 2)
        stage.addActor(typeLabel)

        val starLabel = Label("STAR: ", labelStyle)
        starLabel.setPosition(1340f - starLabel.width, 3240 * 0.35f + 150 - starLabel.height / 2)
        stage.addActor(starLabel)
    }

    /** Loads the spawn box options */
    private fun loadOptions() {
        airportBox = createStandardBox()
        airportBox.setPosition(1440f, 3240 * 0.8f)
        airportBox.items = Array(radarScreen.airports.values.map { it.icao }.toTypedArray())
        airportBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                updateAirlineOptions()
                updateStarOptions()
                event.handle()
            }
        })
        stage.addActor(airportBox)

        airlineBox = createStandardBox()
        airlineBox.setPosition(1440f, 3240 * 0.65f)
        updateAirlineOptions()
        airlineBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                updateAircraftTypeOptions()
                event.handle()
            }
        })
        stage.addActor(airlineBox)

        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE

        val textFieldStyle = TextField.TextFieldStyle()
        textFieldStyle.background = TerminalControl.skin.getDrawable("Button_up")
        textFieldStyle.font = Fonts.defaultFont24
        textFieldStyle.fontColor = Color.WHITE
        val oneCharSizeCalibrationThrowAway = Label("|", labelStyle)
        val cursorColor = Pixmap(
            oneCharSizeCalibrationThrowAway.width.toInt(),
            oneCharSizeCalibrationThrowAway.height.toInt(),
            Pixmap.Format.RGB888
        )
        cursorColor.setColor(Color.WHITE)
        cursorColor.fill()
        textFieldStyle.cursor = Image(Texture(cursorColor)).drawable

        callsignField = TextField("", textFieldStyle)
        callsignField.setPosition(airlineBox.x + airlineBox.width + 100, airlineBox.y)
        callsignField.setSize(350f, 300f)
        callsignField.maxLength = 4
        callsignField.textFieldFilter = TextField.TextFieldFilter.DigitsOnlyFilter()
        callsignField.alignment = Align.center
        stage.addActor(callsignField)
        stage.keyboardFocus = callsignField

        typeBox = createStandardBox()
        typeBox.setPosition(1440f, 3240 * 0.5f)
        updateAircraftTypeOptions()
        stage.addActor(typeBox)

        starBox = createStandardBox()
        starBox.setPosition(1440f, 3240 * 0.35f)
        updateStarOptions()
        stage.addActor(starBox)
    }

    /** Updates the available airlines at the airport */
    private fun updateAirlineOptions() {
        airlineBox.items = Array(
            radarScreen.airports[airportBox.selected]?.airlines?.values?.distinct()?.toTypedArray() ?: arrayOf("???"))
        airlineBox.items.sort()
    }

    /** Updates the available aircraft types for airline */
    private fun updateAircraftTypeOptions() {
        typeBox.items = Array(radarScreen.airports[airportBox.selected]?.aircrafts?.get(airlineBox.selected)?.split(">".toRegex())?.toTypedArray() ?: arrayOf("???"))
        typeBox.items.sort()
    }

    /** Updates the available STARs */
    private fun updateStarOptions() {
        starBox.items = Array(radarScreen.airports[airportBox.selected].let { airport ->
            airport?.stars?.values?.filter { it.runways.containsAny(Array(airport.landingRunways.keys.toTypedArray()), false) }?.map { it.name }?.toTypedArray()
        } ?: arrayOf("???"))
        starBox.items.sort()
    }

    /** Makes the standard selectBox for the UI */
    private fun createStandardBox(): SelectBox<String> {
        val selectBox = SelectBox<String>(SettingsTemplateScreen.selectBoxStyle)
        selectBox.setSize(700f, 300f)
        selectBox.setAlignment(Align.center)
        selectBox.list.setAlignment(Align.center)

        return selectBox
    }

    /** Loads buttons  */
    private fun loadButton() {
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont30
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        val cancelButton = TextButton("Cancel", textButtonStyle)
        cancelButton.setSize(1200f, 300f)
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800f)
        cancelButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                returnToTrafficScreen()
            }
        })
        stage.addActor(cancelButton)

        val confirmButton = TextButton("Spawn", textButtonStyle)
        confirmButton.setSize(1200f, 300f)
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800f)
        confirmButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val callsign = airlineBox.selected + callsignField.text
                if (callsignField.text.isBlank()) {
                    CustomDialog("Invalid callsign", "Callsign number cannot be empty", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                    return
                } else if (radarScreen.allAircraft.contains(callsign)) {
                    CustomDialog("Invalid callsign", "Callsign already exists in game", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                    return
                }
                Gdx.app.postRunnable {
                    val arrival = Arrival(callsign, typeBox.selected, radarScreen.airports[airportBox.selected]!!, starBox.selected)
                    radarScreen.aircrafts[callsign] = arrival
                    radarScreen.arrivals++
                }
                returnToTrafficScreen()
            }
        })
        stage.addActor(confirmButton)
    }

    /** Overrides show method of basic screen */
    override fun show() {
        loadUI()
    }

    /** Return to traffic settings screen */
    private fun returnToTrafficScreen() {
        game.screen = TrafficSettingsScreen(game, TerminalControl.radarScreen, null)
    }
}