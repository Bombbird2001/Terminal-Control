package com.bombbird.terminalcontrol.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.runways.RunwayConfig
import com.bombbird.terminalcontrol.utilities.Fonts

class RunwayChanger {
    private val background: Image = Image(TerminalControl.skin.getDrawable("ListBackground"))
    private val airportLabel: Label
    private val timeLabel: Label
    private val changeButton: TextButton
    private val confirmButton: TextButton
    private val scrollPane: ScrollPane
    private val newRunwaysLabel: Label
    private val possibleConfigs = Array<RunwayConfig>()
    private var runwayConfig: RunwayConfig? = null
    private var airport: Airport? = null
    var isVisible: Boolean
        private set

    init {
        val scrollTable = Table()
        scrollPane = ScrollPane(scrollTable)
        scrollPane.x = 0.11f * TerminalControl.radarScreen.ui.paneWidth
        scrollPane.y = 3240 * 0.055f
        scrollPane.setSize(0.78f * TerminalControl.radarScreen.ui.paneWidth, 3240 * 0.175f)
        scrollPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        scrollPane.isVisible = false
        scrollPane.debugAll()
        TerminalControl.radarScreen.uiStage.addActor(scrollPane)

        val labelStyle1 = LabelStyle()
        labelStyle1.fontColor = Color.BLACK
        labelStyle1.font = Fonts.defaultFont20
        newRunwaysLabel = Label("Loading...", labelStyle1)
        newRunwaysLabel.setSize(100f, 100f)
        newRunwaysLabel.setWrap(true)
        scrollTable.add(newRunwaysLabel).width(0.76f * TerminalControl.radarScreen.ui.paneWidth).height(300f).pad(10f, 0.01f * TerminalControl.radarScreen.ui.paneWidth, 10f, 0.11f * TerminalControl.radarScreen.ui.paneWidth)

        background.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                super.clicked(event, x, y)
                event.handle() //Prevents hiding of runway changer when background is tapped
            }
        })
        TerminalControl.radarScreen.ui.addActor(background, 0.1f, 0.8f, 3240 * 0.05f, 3240 * 0.35f)

        val labelStyle = LabelStyle()
        labelStyle.fontColor = Color.WHITE
        labelStyle.font = Fonts.defaultFont20
        airportLabel = Label("", labelStyle)
        TerminalControl.radarScreen.ui.addActor(airportLabel, 0.45f, -1f, 3240 * 0.38f, airportLabel.height)

        val textButtonStyle = TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        confirmButton = TextButton("Confirm runway change", textButtonStyle)
        confirmButton.align(Align.center)
        confirmButton.label.setWrap(true)
        confirmButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (runwayConfig != null) {
                    airport?.isPendingRwyChange = true
                    airport?.rwyChangeTimer = -1f
                    updateRunways()
                    airport?.isPendingRwyChange = false
                    airport?.rwyChangeTimer = 301f
                    hideAll()
                    TerminalControl.radarScreen.commBox.setVisible(true)
                    TerminalControl.radarScreen.ui.updateMetar()
                }
                event.handle()
            }

        })
        TerminalControl.radarScreen.ui.addActor(confirmButton, 0.55f, 0.3f, 3240 * 0.25f, 3240 * 0.11f)

        changeButton = TextButton("Change runway configuration", textButtonStyle)
        changeButton.align(Align.center)
        changeButton.label.setWrap(true)
        changeButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (possibleConfigs.size > 0) {
                    runwayConfig = if (runwayConfig == null) possibleConfigs.first() else {
                        if (possibleConfigs.size > 1) possibleConfigs.add(possibleConfigs.removeIndex(0))
                        possibleConfigs.first()
                    }
                }
                confirmButton.isVisible = runwayConfig != null
                scrollPane.isVisible = true
                updateRunwayLabel()
                event.handle()
            }
        })
        TerminalControl.radarScreen.ui.addActor(changeButton, 0.15f, 0.3f, 3240 * 0.25f, 3240 * 0.11f)

        var inputListener: InputListener? = null
        for (eventListener in scrollPane.listeners) {
            if (eventListener is InputListener) {
                inputListener = eventListener
            }
        }
        if (inputListener != null) scrollPane.removeListener(inputListener)

        timeLabel = Label("Time left: ", labelStyle1)
        TerminalControl.radarScreen.ui.addActor(timeLabel, 0.15f, 0.7f, 3240 * 0.3f, timeLabel.height)
        timeLabel.isVisible = false
        hideAll()
        isVisible = false
    }

    fun update() {
        if (airport == null) return
        airport?.rwyChangeTimer?.let {
            if (it < 0) hideAll()
            val min = it.toInt() / 60
            val sec = it.toInt() - min * 60
            val secText = if (sec < 10) "0$sec" else sec.toString()
            timeLabel.setText("Time left: $min:$secText")
        }
    }

    fun setMainVisible(visible: Boolean) {
        isVisible = visible
        background.isVisible = visible
        airportLabel.isVisible = visible
        changeButton.isVisible = visible
    }

    fun hideAll() {
        isVisible = false
        background.isVisible = false
        timeLabel.isVisible = false
        airportLabel.isVisible = false
        changeButton.isVisible = false
        confirmButton.isVisible = false
        scrollPane.isVisible = false
        possibleConfigs.clear()
    }

    fun setAirport(icao: String) {
        runwayConfig = null
        possibleConfigs.clear()
        scrollPane.isVisible = false
        airportLabel.setText(icao)
        TerminalControl.radarScreen.airports[icao]?.let { airport ->
            timeLabel.isVisible = airport.isPendingRwyChange
        }
        airport?.metar?.let {
            val windHdg = if (it.isNull("windDirection")) 0 else it.getInt("windDirection")
            var windSpd = it.getInt("windSpeed")
            if (windHdg == 0) windSpd = 0
            airport?.runwayManager?.getSuitableConfigs(windHdg, windSpd)?.let { configs ->
                for (config in configs) {
                    if (config.landingRunways.keys == airport?.landingRunways?.keys && config.takeoffRunways.keys == airport?.takeoffRunways?.keys) continue
                    possibleConfigs.add(config)
                }
            }
        }

        if (airport?.isPendingRwyChange == true) {
            runwayConfig = airport?.runwayManager?.latestRunwayConfig
            confirmButton.isVisible = true
            scrollPane.isVisible = true
        }
    }

    private fun updateRunwayLabel() {
        if (runwayConfig == null && possibleConfigs.isEmpty) {
            newRunwaysLabel.setText("Runway change not permitted due to winds")
            return
        }
        val stringBuilder = StringBuilder()
        val rwySet = HashSet<String>()
        runwayConfig?.landingRunways?.keys?.let {
            rwySet.addAll(it)
        }
        runwayConfig?.takeoffRunways?.keys?.let {
            rwySet.addAll(it)
        }
        for (rwy in rwySet) {
            if (airport?.runways?.get(rwy)?.isEmergencyClosed == false && airport?.runways?.get(rwy)?.oppRwy?.isEmergencyClosed == false) {
                val tkof = runwayConfig?.takeoffRunways?.containsKey(rwy)
                val ldg = runwayConfig?.landingRunways?.containsKey(rwy)
                val tmp: String = if (tkof == true && ldg == true) {
                    "takeoffs and landings."
                } else if (tkof == true) {
                    "takeoffs."
                } else if (ldg == true) {
                    "landings."
                } else {
                    "nothing lol."
                }
                stringBuilder.append("Runway ")
                stringBuilder.append(rwy)
                stringBuilder.append(" will be active for ")
                stringBuilder.append(tmp)
                stringBuilder.append("\n")
            }
        }
        if (stringBuilder.isEmpty()) {
            newRunwaysLabel.setText("All runways are/will be closed")
            return
        }
        newRunwaysLabel.setText(stringBuilder.toString())
    }

    //Change runways
    private fun updateRunways() {
        airport?.rwyChangeTimer = -1f
        runwayConfig?.applyConfig()
        airport?.updateZoneStatus()
        airport?.isPendingRwyChange = false
        airport?.rwyChangeTimer = 301f
    }

    fun containsLandingRunway(icao: String, rwy: String): Boolean {
        return icao == airport?.icao && runwayConfig?.landingRunways?.containsKey(rwy) == true && confirmButton.isVisible
    }
}