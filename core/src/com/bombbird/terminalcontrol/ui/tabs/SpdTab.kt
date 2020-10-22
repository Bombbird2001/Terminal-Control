package com.bombbird.terminalcontrol.ui.tabs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.ui.Ui

class SpdTab(ui: Ui) : Tab(ui) {
    private var spdModeChanged = false
    var isSpdChanged = false
    private val spds: Array<String> = Array()
    
    fun loadModes() {
        modeButtons.addButton(NavState.SID_STAR_RESTR, "SID/STAR restrictions")
        modeButtons.addButton(NavState.NO_RESTR, "Unrestricted")
    }

    fun updateModeButtons() {
        modeButtons.changeButtonText(NavState.SID_STAR_RESTR, if (selectedAircraft is Arrival) "STAR restrictions" else "SID restrictions")
        modeButtons.setButtonColour(false)
    }

    override fun updateElements() {
        selectedAircraft?.let {
            notListening = true
            modeButtons.updateButtonActivity(it.navState.spdModes)
            if (visible) {
                valueBox.isVisible = true
            }
            spds.clear()
            var lowestSpd: Int
            var highestSpd = -1
            if (spdMode == NavState.SID_STAR_RESTR) {
                //Set spd restrictions in box
                if (latMode == NavState.HOLD_AT && it.isHolding) {
                    highestSpd = it.route.holdProcedure.getMaxSpdAtWpt(it.holdWpt)
                    if (highestSpd == -1) highestSpd = 250
                } else if (clearedWpt != null) {
                    highestSpd = it.getMaxWptSpd(clearedWpt)
                }
            }
            if (highestSpd == -1) {
                highestSpd = if (it.altitude >= 9900) {
                    it.climbSpd
                } else if (spdMode == NavState.NO_RESTR && it.request == Aircraft.HIGH_SPEED_REQUEST && it.isRequested) {
                    it.climbSpd
                } else {
                    250
                }
            }
            if (it is Departure) {
                lowestSpd = 200
            } else if (it is Arrival) {
                lowestSpd = 160
                if (it.ils != null && it.isLocCap) {
                    lowestSpd = it.apchSpd
                } else if (it.apchSpd > lowestSpd) {
                    while (it.apchSpd > lowestSpd) {
                        lowestSpd += 10
                    }
                }
            } else {
                lowestSpd = 0
                Gdx.app.log("Invalid aircraft type", "Aircraft not instance of departure or arrival")
            }
            clearedSpd = MathUtils.clamp(clearedSpd, lowestSpd, highestSpd)
            if (lowestSpd % 10 != 0) {
                spds.add(lowestSpd.toString())
                var spdTracker = lowestSpd + (10 - lowestSpd % 10)
                while (spdTracker <= highestSpd.coerceAtMost(250)) {
                    spds.add(spdTracker.toString())
                    spdTracker += 10
                }
            } else {
                while (lowestSpd <= highestSpd.coerceAtMost(250)) {
                    spds.add(lowestSpd.toString())
                    lowestSpd += 10
                }
            }
            if (highestSpd > 250) {
                spds.add(highestSpd.toString())
            }
            valueBox.items = spds
            valueBox.selected = clearedSpd.toString()
            notListening = false
        }
    }

    override fun compareWithAC() {
        spdModeChanged = false
        isSpdChanged = false
        selectedAircraft?.let {
            spdModeChanged = spdMode != it.navState.dispSpdMode.last()
            isSpdChanged = clearedSpd != it.navState.clearedSpd.last()
        }
        tabChanged = spdModeChanged || isSpdChanged
    }

    override fun updateElementColours() {
        notListening = true
        modeButtons.setButtonColour(spdModeChanged)

        //Spd box colour
        if (isSpdChanged) {
            valueBox.style.fontColor = Color.YELLOW
        } else {
            valueBox.style.fontColor = Color.WHITE
        }
        super.updateElementColours()
        notListening = false
    }

    override fun updateMode() {
        selectedAircraft?.navState?.sendSpd(spdMode, clearedSpd)
    }

    override val acState: Unit
        get() {
            spdModeChanged = false
            isSpdChanged = false
            selectedAircraft?.let {
                spdMode = it.navState.dispSpdMode.last()
                modeButtons.mode = spdMode
                clearedSpd = it.navState.clearedSpd.last()
            }
        }

    override val choices: Unit
        get() {
            spdMode = modeButtons.mode
            clearedSpd = valueBox.selected.toInt()
        }

}