package com.bombbird.terminalcontrol.entities.separation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.utilities.math.MathTools
import kotlin.math.floor
import kotlin.math.roundToInt

class StormChecker {
    companion object {
        /** Uses aircraft trajectory to determine if aircraft is about to enter storm, sets the request timer if so */
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
                            if (storm.intensityMap[coord]?.intensity ?: 0 > 3) {
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

        /** Checks if the departure path of runway is in storm, sets stormInPath variable to true if so */
        fun checkRunwayStorm(runway: Runway) {
            val radarScreen = TerminalControl.radarScreen!!
            val vector2 = Vector2(0f, 1f)
            vector2.rotateDeg(-runway.trueHdg)
            var found = false
            for (i in 0..MathTools.feetToPixel(runway.feetLength.toFloat()).roundToInt() + 5) {
                vector2.setLength(MathTools.nmToPixel(i.toFloat()) + 0.1f)
                for (storm in radarScreen.thunderCellArray) {
                    val coordX = floor((runway.position[0] + vector2.x - storm.centreX) / 10).toInt()
                    val coordY = floor((runway.position[1] + vector2.y - storm.centreY) / 10).toInt()
                    //Check all 9 squares
                    for (j in coordX - 1..coordX + 1) {
                        for (k in coordY - 1..coordY + 1) {
                            val coord = "$j $k"
                            if (storm.intensityMap[coord]?.intensity ?: 0 > 3) {
                                found = true
                                break
                            }
                        }
                        if (found) break
                    }
                    if (found) break
                }
                if (found) break
            }
            runway.isStormInPath = found
        }
    }
}