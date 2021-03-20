package com.bombbird.terminalcontrol.ui.tabs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.utilities.Fonts

class AltTab(ui: Ui) : Tab(ui) {
    override val choices: Unit
        get() {
            altMode = modeButtons.mode
            clearedAlt = if (valueBox.selected.contains("FL")) valueBox.selected.substring(2).toInt() * 100 else valueBox.selected.toInt()
            clearedExpedite = expediteButton.isChecked
        }
    
    private var altModeChanged = false
    var isAltChanged = false
    var isExpediteChanged = false
        private set
    private val alts: Array<String> = Array()
    private lateinit var expediteButton: TextButton

    init {
        loadExpediteButton()
    }
    
    fun loadModes() {
        modeButtons.addButton(NavState.SID_STAR_RESTR, "Climb via SID/Descend via STAR")
        modeButtons.addButton(NavState.NO_RESTR, "Unrestricted")
    }

    private fun loadExpediteButton() {
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.BLACK
        textButtonStyle.up = Ui.lightBoxBackground
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down_sharp")
        textButtonStyle.checked = TerminalControl.skin.getDrawable("Button_down_sharp")
        expediteButton = TextButton("Expedite", textButtonStyle)
        expediteButton.setProgrammaticChangeEvents(false)
        expediteButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                choiceMade()
                event.handle()
            }
        })
        addActor(expediteButton, 0.1f, 0.25f, 3240 - 1325f, 300f)
    }

    fun updateModeButtons() {
        modeButtons.changeButtonText(NavState.SID_STAR_RESTR, if (selectedAircraft is Arrival) "Descend via STAR" else "Climb via SID")
        modeButtons.setButtonColour(false)
    }

    override fun updateElements() {
        selectedAircraft?.let {
            notListening = true
            modeButtons.updateButtonActivity(it.navState.altModes)
            if (visible) {
                valueBox.isVisible = true
            }
            alts.clear()
            var lowestAlt: Int
            var highestAlt = -1
            val allAlts: Array<Int>
            when (it) {
                is Departure -> {
                    lowestAlt = it.lowestAlt
                    highestAlt = radarScreen.maxAlt
                    if (altMode == NavState.SID_STAR_RESTR && it.route.getWptMinAlt(clearedWpt) > highestAlt) {
                        highestAlt = it.route.getWptMinAlt(clearedWpt)
                    }
                    allAlts = createAltArray(lowestAlt, highestAlt)
                }
                is Arrival -> {
                    lowestAlt = radarScreen.minAlt
                    var apchMode = false
                    if (latMode == NavState.SID_STAR && clearedILS != Ui.NOT_CLEARED_APCH) {
                        apchMode = true
                        val tmpLowestAlt = it.airport.approaches[clearedILS?.substring(3)]?.getNextPossibleTransition(radarScreen.waypoints[clearedWpt], it.route)?.first?.restrictions?.let { it2 -> if (it2.size > 0) it2[it2.size - 1][0] else null }
                        if (tmpLowestAlt == -1) lowestAlt = radarScreen.minAlt
                        if (tmpLowestAlt != null) highestAlt = lowestAlt
                    } else if (latMode == NavState.HOLD_AT) {
                        val restr: IntArray = radarScreen.waypoints[holdWpt]?.let { it1 -> it.route.holdProcedure.getAltRestAtWpt(it1) } ?: intArrayOf(-1, -1)
                        lowestAlt = restr[0]
                        highestAlt = restr[1]
                    } else if (altMode == NavState.SID_STAR_RESTR && it.altitude < radarScreen.maxAlt) {
                        //Set alt restrictions in box
                        highestAlt = it.altitude.toInt()
                        highestAlt -= highestAlt % 1000
                        val starHighestAlt = it.direct?.name?.let { it2 -> it.route.getWptMaxAlt(it2) } ?: radarScreen.maxAlt
                        if (starHighestAlt > -1) highestAlt = starHighestAlt
                    }
                    if (highestAlt == -1) {
                        highestAlt = radarScreen.maxAlt
                    }
                    if (!apchMode && highestAlt < it.altitude.toInt() && it.altitude.toInt() <= radarScreen.maxAlt) {
                        highestAlt = it.altitude.toInt() / 1000 * 1000
                    }
                    if (it.emergency.isActive && it.emergency.type == Emergency.Type.PRESSURE_LOSS) {
                        highestAlt = 10000 //Cannot climb above 10000 feet due to pressure loss
                    }
                    if (highestAlt < lowestAlt) highestAlt = lowestAlt
                    if (it.isGsCap || it.apch != null && it.apch?.isNpa == true && it.isLocCap && it.navState.dispLatMode.first() == NavState.VECTORS) {
                        lowestAlt = it.apch?.missedApchProc?.climbAlt ?: radarScreen.minAlt
                        highestAlt = lowestAlt
                    }
                    allAlts = createAltArray(lowestAlt, highestAlt)
                    val icao: String = it.airport.icao
                    if ("TCOO" == icao) {
                        checkAndAddIntermediate(allAlts, it.altitude, 3500)
                    } else if ("TCHH" == icao && it.sidStar.runways.contains("25R", false)) {
                        checkAndAddIntermediate(allAlts, it.altitude, 4300)
                        checkAndAddIntermediate(allAlts, it.altitude, 4500)
                    } else if ("TCHX" == icao) {
                        checkAndAddIntermediate(allAlts, it.altitude, 4500)
                    }
                    allAlts.sort()
                }
                else -> {
                    lowestAlt = 0
                    highestAlt = 10000
                    allAlts = createAltArray(lowestAlt, highestAlt)
                    Gdx.app.log("Invalid aircraft type", "Aircraft not instance of departure or arrival")
                }
            }
            clearedAlt = MathUtils.clamp(clearedAlt, allAlts.first(), allAlts[allAlts.size - 1])
            //Adds the possible altitudes between range to array
            for (alt in allAlts) {
                alts.add(if (alt / 100 >= radarScreen.transLvl) "FL" + alt / 100 else alt.toString())
            }
            valueBox.items = alts
            valueBox.selected = if (clearedAlt / 100 >= radarScreen.transLvl) "FL" + clearedAlt / 100 else clearedAlt.toString()
            expediteButton.isChecked = clearedExpedite
            notListening = false
        }
    }

    fun createAltArray(lowestAlt: Int, highestAlt: Int): Array<Int> {
        val newAltArray = Array<Int>()
        if (lowestAlt == highestAlt) {
            newAltArray.add(lowestAlt)
            return newAltArray
        }
        var start = lowestAlt
        if (lowestAlt % 1000 != 0) {
            newAltArray.add(lowestAlt)
            start = (lowestAlt / 1000 + 1) * 1000
        }
        var i = start
        while (i < highestAlt) {
            newAltArray.add(i)
            i += 1000
        }
        newAltArray.add(highestAlt)
        return newAltArray
    }

    private fun checkAndAddIntermediate(allAlts: Array<Int>, currentAlt: Float, altToAdd: Int) {
        if (currentAlt < altToAdd - 20 || allAlts.contains(altToAdd, false)) return  //Current aircraft altitude must be at lowest 20 feet lower than altitude to add
        allAlts.add(altToAdd)
    }

    override fun compareWithAC() {
        altModeChanged = false
        isAltChanged = false
        isExpediteChanged = false
        tabChanged = false
        selectedAircraft?.let {
            altModeChanged = altMode != it.navState.dispAltMode.last()
            isAltChanged = clearedAlt != it.navState.clearedAlt.last()
            isExpediteChanged = clearedExpedite != it.navState.clearedExpedite.last()
        }
        tabChanged = altModeChanged || isAltChanged || isExpediteChanged
    }

    override fun updateElementColours() {
        notListening = true
        modeButtons.setButtonColour(altModeChanged)

        //Alt box colour
        valueBox.style.fontColor = if (isAltChanged) Color.YELLOW else Color.WHITE

        //Expedite button colour
        if (isExpediteChanged) {
            expediteButton.style.fontColor = Color.YELLOW
        } else {
            expediteButton.style.fontColor = if (expediteButton.isChecked) Color.WHITE else Color.BLACK
        }
        super.updateElementColours()
        notListening = false
    }

    override fun updateMode() {
        selectedAircraft?.navState?.sendAlt(altMode, clearedAlt, clearedExpedite)
    }

    override val acState: Unit
        get() {
            altModeChanged = false
            isAltChanged = false
            isExpediteChanged = false
            selectedAircraft?.let {
                altMode = it.navState.dispAltMode.last()
                modeButtons.mode = altMode
                clearedAlt = it.navState.clearedAlt.last()
                clearedExpedite = it.navState.clearedExpedite.last()
            }
        }
}