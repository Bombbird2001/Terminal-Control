package com.bombbird.terminalcontrol.entities.separation

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.completeAchievement
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.incrementConflicts
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.approaches.LDA
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.zones.ApproachZone
import com.bombbird.terminalcontrol.entities.zones.DepartureZone
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import kotlin.math.abs
import kotlin.math.roundToInt

class SeparationChecker : Actor() {
    companion object {
        const val NORMAL_CONFLICT = 0
        const val ILS_LESS_THAN_10NM = 1
        const val PARALLEL_ILS = 2
        const val ILS_NTZ = 3
        const val MVA = 4
        const val SID_STAR_MVA = 5
        const val RESTRICTED = 6
        const val WAKE_INFRINGE = 7
    }

    private val flightLevels: Array<Array<Aircraft>>
    private val labels = Array<Label>()
    private val lineStorage = Array<FloatArray>()
    private val radarScreen = TerminalControl.radarScreen!!
    var lastNumber: Int
    var time: Float
    private var updateTimer: Float
    val allConflictCallsigns = Array<String>()
    val allConflicts = Array<Int>()

    init {
        lastNumber = 0
        time = 0f
        updateTimer = 0f
        flightLevels = Array(true, radarScreen.maxAlt / 1000)
        for (i in 0 until radarScreen.maxAlt / 1000) {
            flightLevels.add(Array())
        }
    }

    /** Draws the labels, if any  */
    override fun draw(batch: Batch, alpha: Float) {
        for (label in labels) {
            label.draw(batch, 1f)
        }
    }

    /** Called to add a new separation label to the array, for when there are less labels than the number of separation incidents between aircraft  */
    private fun newSeparationLabel(): Label {
        val labelStyle = Label.LabelStyle()
        labelStyle.fontColor = Color.RED
        labelStyle.font = Fonts.defaultFont6
        return Label("", labelStyle)
    }

    /** Updates the state of aircraft separation  */
    fun update() {
        updateTimer -= Gdx.graphics.deltaTime
        time -= Gdx.graphics.deltaTime
        if (updateTimer < 0) {
            updateTimer += 0.5f
            for (aircraft in radarScreen.aircrafts.values) {
                aircraft.isWarning = false
                aircraft.isConflict = false
                aircraft.isTerrainConflict = false
            }
            for (label in labels) {
                label.setText("")
                label.name = ""
            }
            for (obstacle in radarScreen.obsArray) {
                obstacle.isConflict = false
            }
            allConflicts.clear()
            allConflictCallsigns.clear()
            checkAircraftSep()
            checkRestrSep()
            var tmpActive = allConflicts.size
            while (tmpActive > lastNumber) {
                radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.95f))
                radarScreen.separationIncidents = radarScreen.separationIncidents + 1
                incrementConflicts()
                tmpActive--
            }
            //Subtract wake separately (don't include 5% penalty)
            for (aircraft in radarScreen.aircrafts.values) {
                if (aircraft.isWakeInfringe && aircraft.isArrivalDeparture) {
                    allConflicts.add(WAKE_INFRINGE)
                    allConflictCallsigns.add(aircraft.callsign)
                    aircraft.isConflict = true
                    radarScreen.shapeRenderer.color = Color.RED
                    radarScreen.shapeRenderer.circle(aircraft.radarX, aircraft.radarY, 48.6f)
                }
            }
            lastNumber = allConflicts.size
        }
        if (time <= 0) {
            time += 3f
            radarScreen.setScore(radarScreen.getScore() - allConflicts.size)
        }
        for (aircraft in radarScreen.aircrafts.values) {
            if ((aircraft.isConflict || aircraft.isTerrainConflict || aircraft.isWakeInfringe) && !aircraft.isSilenced) {
                radarScreen.soundManager.playConflict()
                break
            }
            aircraft.isPrevConflict = aircraft.isConflict || aircraft.isTerrainConflict || aircraft.isWakeInfringe
        }
    }

    /** Updates the levels each aircraft belongs to  */
    fun updateAircraftPositions() {
        for (array in flightLevels) {
            array.clear()
        }
        for (aircraft in radarScreen.aircrafts.values) {
            val flightLevel = (aircraft.altitude / 1000).toInt()
            if (flightLevel < radarScreen.maxAlt / 1000) {
                flightLevels[flightLevel].add(aircraft)
            }
        }
    }

    /** Checks that each aircraft is separated from one another  */
    private fun checkAircraftSep() {
        lineStorage.clear()
        for (i in 0 until flightLevels.size) {
            //Get all the possible planes to check
            val planesToCheck = Array<Aircraft>()
            if (i - 1 >= 0) {
                planesToCheck.addAll(flightLevels[i - 1])
            }
            if (i + 1 < flightLevels.size - 1) {
                planesToCheck.addAll(flightLevels[i + 1])
            }
            planesToCheck.addAll(flightLevels[i])
            for (j in 0 until planesToCheck.size) {
                val plane1 = planesToCheck[j]
                for (k in j + 1 until planesToCheck.size) {
                    val plane2 = planesToCheck[k]

                    //Split up exception cases to make it easier to read
                    if (plane1.emergency.isActive || plane2.emergency.isActive) {
                        //If either plane is an emergency
                        continue
                    }
                    if (plane1.altitude < plane1.airport.elevation + 1400 || plane2.altitude < plane1.airport.elevation + 1400 || plane1.altitude > radarScreen.maxAlt && plane2.altitude > radarScreen.maxAlt) {
                        //If either plane is below 1400 feet or both above max alt
                        continue
                    }
                    if (plane1 is Arrival && plane2 is Arrival && plane1.airport.icao == plane2.airport.icao && plane1.altitude < plane1.airport.elevation + 6000 && plane2.altitude < plane2.airport.elevation + 6000) {
                        //If both planes are arrivals into same airport, check whether they are in different NOZ for simultaneous approach
                        val approachZones: Array<ApproachZone> = plane1.airport.approachZones
                        var found = false
                        for (l in 0 until approachZones.size) {
                            if (approachZones[l].checkSeparation(plane1, plane2)) {
                                found = true
                                break
                            }
                        }
                        if (found) continue
                    }
                    if (plane1 is Departure && plane2 is Departure && plane1.airport.icao == plane2.airport.icao) {
                        //If both planes are departures from same airport, check whether they are in different NOZ for simultaneous departure
                        val departureZones: Array<DepartureZone> = plane1.airport.departureZones
                        var found = false
                        for (l in 0 until departureZones.size) {
                            if (departureZones[l].checkSeparation(plane1, plane2)) {
                                found = true
                                break
                            }
                        }
                        if (found) continue
                    }
                    if (plane1.isGoAroundWindow || plane2.isGoAroundWindow) {
                        //If either plane went around less than 2 minutes ago
                        continue
                    }
                    val dist = pixelToNm(distanceBetween(plane1.x, plane1.y, plane2.x, plane2.y))
                    var minima = radarScreen.separationMinima.toFloat()
                    if (plane1.ils != null && plane2.ils != null && plane1.ils != plane2.ils && plane1.ils?.isInsideILS(plane1.x, plane1.y) == true && plane2.ils?.isInsideILS(plane2.x, plane2.y) == true) {
                        //If both planes are on different ILS and both have captured LOC and are within at least 1 of the 2 arcs, reduce separation to 2nm (staggered separation)
                        minima = 2f
                    }
                    if (plane1.ils != null && plane1.ils == plane2.ils) {
                        val runway: Runway = plane1.ils?.rwy ?: continue
                        if (pixelToNm(distanceBetween(plane1.x, plane1.y, runway.x, runway.y)) < 10 &&
                                pixelToNm(distanceBetween(plane2.x, plane2.y, runway.x, runway.y)) < 10) {
                            //If both planes on the same LOC but are both less than 10nm from runway threshold, separation minima is reduced to 2.5nm
                            minima = 2.5f
                            //TODO If visibility is poor, reduced separation doesn't apply?
                        }
                    }
                    if (abs(plane1.altitude - plane2.altitude) < 975 && dist < minima + 2) {
                        if (abs(plane1.altitude - plane2.altitude) < 900 && dist < minima) {
                            if (!plane1.isConflict || !plane2.isConflict) {
                                //TODO Change separation minima depending on visibility(?)
                                //Aircraft have infringed minima of 1000 feet and 3nm apart
                                if (!plane1.isPrevConflict) plane1.isSilenced = false
                                if (!plane2.isPrevConflict) plane2.isSilenced = false
                                plane1.isConflict = true
                                plane2.isConflict = true
                                lineStorage.add(floatArrayOf(plane1.radarX, plane1.radarY, plane2.radarX, plane2.radarY, 1f))
                                if (abs(plane1.altitude - plane2.altitude) < 200 && dist < 0.5f) completeAchievement("thatWasClose")
                                when (minima) {
                                    2f -> allConflicts.add(PARALLEL_ILS)
                                    2.5f -> allConflicts.add(ILS_LESS_THAN_10NM)
                                    else -> {
                                        var found = false
                                        if (plane1 is Arrival && plane2 is Arrival && plane1.airport.icao == plane2.airport.icao && plane1.altitude < plane1.airport.elevation + 6000 && plane2.altitude < plane2.airport.elevation + 6000) {
                                            //If both planes are arrivals into same airport, check whether they are in different NOZ for simultaneous approach
                                            val approachZones: Array<ApproachZone> = plane1.airport.approachZones
                                            for (l in 0 until approachZones.size) {
                                                if (approachZones[l].isInNTZ(plane1) || approachZones[l].isInNTZ(plane2)) {
                                                    found = true
                                                }
                                            }
                                        } else if (plane1 is Departure && plane2 is Departure && plane1.airport.icao == plane2.airport.icao) {
                                            //If both planes are departures from same airport, check whether they are in different NOZ for simultaneous departure
                                            val departureZones: Array<DepartureZone> = plane1.airport.departureZones
                                            for (l in 0 until departureZones.size) {
                                                if (departureZones[l].isInNTZ(plane1) || departureZones[l].isInNTZ(plane2)) {
                                                    found = true
                                                }
                                            }
                                        }
                                        allConflicts.add(if (found) ILS_NTZ else NORMAL_CONFLICT)
                                    }
                                }
                                allConflictCallsigns.add("${plane1.callsign}, ${plane2.callsign}")
                            }
                        } else if (!plane1.isWarning || !plane2.isWarning) {
                            //Aircraft within 1000 feet, 5nm of each other
                            plane1.isWarning = true
                            plane2.isWarning = true
                            lineStorage.add(floatArrayOf(plane1.radarX, plane1.radarY, plane2.radarX, plane2.radarY, 0f))
                        }
                        var found = false
                        for (label in labels) {
                            if ("" == label.name) {
                                setLabel(label, plane1, plane2)
                                found = true
                                break
                            }
                        }
                        if (!found) {
                            val label = newSeparationLabel()
                            setLabel(label, plane1, plane2)
                            labels.add(label)
                        }
                    }
                }
            }
        }
    }

    /** Called to update the label with aircraft data  */
    private fun setLabel(label: Label, plane1: Aircraft, plane2: Aircraft) {
        val dist = pixelToNm(distanceBetween(plane1.radarX, plane1.radarY, plane2.radarX, plane2.radarY))
        label.style.fontColor = if (plane1.isConflict && plane2.isConflict) Color.RED else Color.ORANGE
        label.setText(((dist * 100).roundToInt() / 100f).toString())
        label.pack()
        label.name = "Taken"
        label.isVisible = true
        label.setPosition((plane1.radarX + plane2.radarX - label.width) / 2, (plane1.radarY + plane2.radarY - label.height) / 2)
    }

    /** Checks that each aircraft is separated from each obstacles/restricted area  */
    private fun checkRestrSep() {
        for (aircraft in radarScreen.aircrafts.values) {
            if (aircraft.isOnGround || aircraft.isGsCap || aircraft is Arrival && aircraft.ils is LDA && aircraft.isLocCap ||
                    aircraft is Arrival && aircraft.ils != null && aircraft.ils?.name?.contains("IMG") == true ||
                    aircraft.isGoAroundWindow) {
                //Suppress terrain warnings if aircraft is already on the ILS's GS or is on the NPA, or is on the ground, or is on the imaginary ILS for LDA (if has not captured its GS yet), or just did a go around
                continue
            }
            var conflict = false
            val isVectored = aircraft.isVectored && (aircraft.ils == null || aircraft.ils?.isInsideILS(aircraft.x, aircraft.y) == false) //Technically not being vectored if within localizer range
            val isInZone: Boolean = aircraft.route.inSidStarZone(aircraft.x, aircraft.y, aircraft.altitude) //No need for belowMinAlt as this already takes into account
            for (obstacle in radarScreen.obsArray) {
                //If aircraft is infringing obstacle
                if (obstacle.isIn(aircraft) && aircraft.altitude < obstacle.minAlt - 100) {
                    if (obstacle.isEnforced) {
                        //Enforced, conflict
                        conflict = true
                    } else {
                        //Not enforced, conflict only if not excluded, is vectored, is below the STAR minimum altitude or is not within the SID/STAR zone
                        conflict = isVectored || !isInZone
                        obstacle.isConflict = conflict
                    }
                    if (conflict) {
                        when {
                            obstacle.isEnforced -> allConflicts.add(RESTRICTED)
                            isVectored -> allConflicts.add(MVA)
                            !isInZone -> allConflicts.add(SID_STAR_MVA)
                            else -> allConflicts.add(MVA) //Shouldn't even happen
                        }
                        allConflictCallsigns.add(aircraft.callsign)
                        break
                    }
                }
            }
            if (conflict && !aircraft.isTerrainConflict) {
                aircraft.isTerrainConflict = true
                aircraft.isConflict = true
                if (!aircraft.isPrevConflict) aircraft.isSilenced = false
            }
        }
    }

    /** Renders the separation rings if aircraft is in conflict  */
    fun renderShape() {
        val radius = (nmToPixel(radarScreen.separationMinima.toFloat()) / 2).toInt()
        for (aircraft in radarScreen.aircrafts.values) {
            if (aircraft.isConflict || aircraft.isTerrainConflict) {
                radarScreen.shapeRenderer.color = Color.RED
                radarScreen.shapeRenderer.circle(aircraft.radarX, aircraft.radarY, radius.toFloat())
                radarScreen.planesToControl = radarScreen.planesToControl - Gdx.graphics.deltaTime * 0.025f
            } else if (aircraft.isWarning) {
                radarScreen.shapeRenderer.color = Color.YELLOW
                radarScreen.shapeRenderer.circle(aircraft.radarX, aircraft.radarY, radius.toFloat())
            }
        }
        for (coords in lineStorage) {
            radarScreen.shapeRenderer.color = if (coords[4] == 0f) Color.YELLOW else Color.RED
            radarScreen.shapeRenderer.line(coords[0], coords[1], coords[2], coords[3])
        }
    }
}