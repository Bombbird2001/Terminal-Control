package com.bombbird.terminalcontrol.entities.separation

import com.badlogic.gdx.graphics.Color
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.separation.trajectory.PositionPoint
import com.bombbird.terminalcontrol.entities.separation.trajectory.Trajectory
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import kotlin.math.abs

class CollisionChecker {
    private val radarScreen = TerminalControl.radarScreen!!
    private val aircraftStorage: com.badlogic.gdx.utils.Array<Array<Aircraft>> = com.badlogic.gdx.utils.Array()
    private val pointStorage: com.badlogic.gdx.utils.Array<Array<PositionPoint>> = com.badlogic.gdx.utils.Array()

    /** Checks separation between trajectory points of same timing  */
    fun checkSeparation() {
        for (aircraft in radarScreen.aircrafts.values) {
            aircraft.isTrajectoryConflict = false
        }
        aircraftStorage.clear()
        pointStorage.clear()
        var a = Trajectory.INTERVAL
        while (a <= radarScreen.collisionWarning) {
            val altitudePositionMatrix = radarScreen.trajectoryStorage.points[a / 5 - 1]
            //Check for each separate timing
            for (i in 0 until altitudePositionMatrix.size) {
                //Get all possible points to check
                val checkPoints = com.badlogic.gdx.utils.Array<PositionPoint>()
                if (i - 1 >= 0) {
                    checkPoints.addAll(altitudePositionMatrix[i - 1])
                }
                if (i + 1 < altitudePositionMatrix.size - 1) {
                    checkPoints.addAll(altitudePositionMatrix[i + 1])
                }
                checkPoints.addAll(altitudePositionMatrix[i])
                for (j in 0 until checkPoints.size) {
                    val point1 = checkPoints[j]
                    val aircraft1 = point1.aircraft ?: continue
                    for (k in j + 1 until checkPoints.size) {
                        val point2 = checkPoints[k]
                        val aircraft2 = point2.aircraft ?: continue

                        //Exception cases:
                        if (aircraft1.isConflict || aircraft1.isTerrainConflict || aircraft2.isConflict || aircraft2.isTerrainConflict) {
                            //If one of the aircraft is in conflict, inhibit warning for now
                            continue
                        }
                        if (aircraft1.isTrajectoryConflict && aircraft2.isTrajectoryConflict) {
                            //If both aircraft have been identified as potential conflicts already
                            continue
                        }
                        if (aircraft1.emergency.isActive || aircraft2.emergency.isActive) {
                            //If either plane is emergency
                            continue
                        }
                        if (aircraft1.isOnGround || aircraft2.isOnGround) {
                            //If either aircraft is on the ground
                            continue
                        }
                        if (point1.altitude < aircraft1.airport.elevation + 1400 || point2.altitude < aircraft2.airport.elevation + 1400 || aircraft1.altitude > radarScreen.maxAlt && aircraft2.altitude > radarScreen.maxAlt) {
                            //If either point is below 1400 feet or both above max alt
                            continue
                        }
                        if (aircraft1 is Arrival && aircraft2 is Arrival && aircraft1.airport.icao == aircraft2.airport.icao && aircraft1.altitude < aircraft1.airport.elevation + 6000 && aircraft2.altitude < aircraft2.airport.elevation + 6000) {
                            //If both planes are arrivals into same airport, check whether they are in different NOZ for simultaneous approach
                            val approachZones = aircraft1.airport.approachZones
                            var found = false
                            for (l in 0 until approachZones.size) {
                                if (approachZones[l].checkSeparation(aircraft1, aircraft2)) {
                                    found = true
                                    break
                                }
                            }
                            if (found) continue
                        }
                        if (aircraft1 is Departure && aircraft2 is Departure && aircraft1.airport.icao == aircraft2.airport.icao) {
                            //If both planes are departures from same airport, check whether they are in different NOZ for simultaneous departure
                            val departureZones = aircraft1.airport.departureZones
                            var found = false
                            for (l in 0 until departureZones.size) {
                                if (departureZones[l].checkSeparation(aircraft1, aircraft2)) {
                                    found = true
                                    break
                                }
                            }
                            if (found) continue
                        }

                        //Go around will not be considered as exceptions to the collision warning
                        val dist = pixelToNm(distanceBetween(point1.x, point1.y, point2.x, point2.y))
                        var minima = radarScreen.separationMinima.toFloat()
                        if (aircraft1.ils != null && aircraft2.ils != null && aircraft1.ils?.isInsideILS(aircraft1.x, aircraft1.y) == true && aircraft2.ils?.isInsideILS(aircraft2.x, aircraft2.y) == true) {
                            //If both planes have captured ILS and both have captured LOC and are within at least 1 of the 2 arcs, reduce minima to 2nm (using 1.85 so effective is 2.05)
                            minima = 1.85f
                        }
                        if (abs(point1.altitude - point2.altitude) < 990 && dist < minima + 0.2f) {
                            //Possible conflict, add to save arrays
                            aircraftStorage.add(arrayOf(aircraft1, aircraft2))
                            pointStorage.add(arrayOf(point1, point2))
                            aircraft1.isTrajectoryConflict = true
                            aircraft2.isTrajectoryConflict = true
                            aircraft1.dataTag.startFlash()
                            aircraft2.dataTag.startFlash()
                        }
                    }
                }
            }
            a += Trajectory.INTERVAL
        }
    }

    /** Renders STCAS alerts  */
    fun renderShape() {
        radarScreen.shapeRenderer.color = Color.MAGENTA
        for (i in 0 until aircraftStorage.size) {
            val aircraft1 = aircraftStorage[i][0]
            val aircraft2 = aircraftStorage[i][1]
            val point1 = pointStorage[i][0]
            val point2 = pointStorage[i][1]
            radarScreen.shapeRenderer.line(aircraft1.radarX, aircraft1.radarY, point1.x, point1.y)
            radarScreen.shapeRenderer.line(aircraft2.radarX, aircraft2.radarY, point2.x, point2.y)
            val midX = (point1.x + point2.x) / 2
            val midY = (point1.y + point2.y) / 2
            val halfLength = nmToPixel((radarScreen.separationMinima + 0.2f) / 2)
            radarScreen.shapeRenderer.rect(midX - halfLength, midY - halfLength, 2 * halfLength, 2 * halfLength)
        }
    }
}