package com.bombbird.terminalcontrol.entities.separation

import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import kotlin.math.floor

class StormChecker {
    companion object {
        fun checkStorm(aircraft: Aircraft) {
            val radarScreen = TerminalControl.radarScreen!!
            var found = false
            for ((index, point) in aircraft.trajectory.positionPoints.withIndex()) {
                if (index <= 2) continue
                if (index >= 60 / 5) break
                for (storm in radarScreen.thunderCellArray) {
                    if (point.altitude < radarScreen.minAlt || point.altitude > radarScreen.maxAlt || point.altitude >= storm.topAltitude) continue
                    val coordX = floor((point.x - storm.centreX) / 10).toInt()
                    val coordY = floor((point.y - storm.centreY) / 10).toInt()
                    //Check all 9 squares
                    for (i in coordX - 1..coordX + 1) {
                        for (j in coordY - 1..coordY + 1) {
                            val coord = "$i $j"
                            if (storm.intensityMap[coord] ?: 0 > 3) {
                                found = true
                                break
                            }
                        }
                        if (found) break
                    }
                    if (found) break
                }
                if (found) {
                    if (aircraft.stormWarningTime > 61) aircraft.stormWarningTime = MathUtils.random(0, ((index + 1) * 5 - 40).coerceAtLeast(1)).toFloat()
                    break
                }
            }
            if (!found) {
                if (aircraft.stormWarningTime < 0) {
                    aircraft.isActionRequired = false
                    aircraft.ui.updateAckHandButton(aircraft)
                }
                aircraft.stormWarningTime = 65f
            }
        }
    }
}