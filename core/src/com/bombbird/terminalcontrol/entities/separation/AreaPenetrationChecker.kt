package com.bombbird.terminalcontrol.entities.separation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.entities.approaches.LDA
import com.bombbird.terminalcontrol.entities.separation.trajectory.PositionPoint
import com.bombbird.terminalcontrol.entities.separation.trajectory.Trajectory
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel

class AreaPenetrationChecker {
    private val radarScreen = TerminalControl.radarScreen!!
    private val aircraftStorage: Array<Aircraft> = Array()
    private val pointStorage: Array<PositionPoint> = Array()

    /** Checks separation between points and terrain  */
    fun checkSeparation() {
        for (aircraft in radarScreen.aircrafts.values) {
            aircraft.isTrajectoryTerrainConflict = false
        }
        aircraftStorage.clear()
        pointStorage.clear()
        var i = Trajectory.UPDATE_INTERVAL
        while (i <= radarScreen.areaWarning) {

            //For each trajectory timing
            for (obstacle in radarScreen.obsArray) {
                //Check only the array levels below obstacle height
                val requiredArrays = (obstacle.minAlt / 1000).coerceAtMost(radarScreen.maxAlt / 1000 - 1)
                for (j in 0 until requiredArrays) {
                    for (positionPoint in radarScreen.trajectoryStorage.points[i / 5 - 1][j]) {
                        val aircraft = positionPoint.aircraft ?: continue

                        //Exception cases
                        if (aircraft.isConflict || aircraft.isTerrainConflict) {
                            //If aircraft is already in conflict, inhibit warning for now
                            continue
                        }
                        if (aircraft.isTrajectoryTerrainConflict) {
                            //If aircraft is already predicted to conflict with terrain
                            continue
                        }
                        if ((aircraft.navState.dispLatMode.first() == NavState.SID_STAR || aircraft.ils != null && aircraft.ils?.isInsideILS(positionPoint.x, positionPoint.y) == true) && !obstacle.isEnforced && (aircraft.route.inSidStarZone(positionPoint.x, positionPoint.y, aircraft.altitude) || (aircraft.isHolding && aircraft.holdWpt?.let { aircraft.altitude > aircraft.route.holdProcedure.getAltRestAtWpt(it)[0] - 100 } == true))) {
                            //If latMode is STAR/SID or point is within localizer range, is within sidStarZone and obstacle is not a restricted area, ignore
                            continue
                        }
                        if (aircraft.navState.clearedIls.first() != null) {
                            //If aircraft is cleared for ILS approach, inhibit to prevent nuisance warnings
                            continue
                        }
                        if (aircraft.isOnGround || aircraft.isGsCap || aircraft is Arrival && aircraft.ils is LDA && aircraft.isLocCap || aircraft is Arrival && aircraft.ils != null && aircraft.ils?.name?.contains("IMG") == true ||
                                aircraft.isGoAroundWindow) {
                            //Suppress terrain warnings if aircraft is already on the ILS's GS or is on the NPA, or is on the ground, or is on the imaginary ILS for LDA (if has not captured its GS yet), or just did a go around
                            continue
                        }
                        if (positionPoint.altitude < obstacle.minAlt - 50 && obstacle.isIn(positionPoint.x, positionPoint.y)) {
                            //Possible conflict, add to save arrays
                            aircraftStorage.add(aircraft)
                            pointStorage.add(positionPoint)
                            aircraft.isTrajectoryTerrainConflict = true
                            aircraft.dataTag.startFlash()
                        }
                    }
                }
            }
            i += Trajectory.UPDATE_INTERVAL
        }
    }

    /** Renders APW alerts  */
    fun renderShape() {
        radarScreen.shapeRenderer.color = Color.RED
        for (i in 0 until aircraftStorage.size) {
            val aircraft = aircraftStorage[i]
            val x = pointStorage[i].x
            val y = pointStorage[i].y
            radarScreen.shapeRenderer.line(aircraft.radarX, aircraft.radarY, x, y)
            val halfLength = nmToPixel(1.5f)
            radarScreen.shapeRenderer.rect(x - halfLength, y - halfLength, halfLength * 2, halfLength * 2)
        }
    }
}