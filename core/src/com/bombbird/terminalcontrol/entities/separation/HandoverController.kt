package com.bombbird.terminalcontrol.entities.separation

import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.separation.trajectory.PositionPoint
import com.bombbird.terminalcontrol.utilities.math.MathTools
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class HandoverController {
    private val radarScreen = TerminalControl.radarScreen!!
    val aircraftList = Array<kotlin.Array<Aircraft>>()
    val targetAltitudeList = HashMap<String, Int>()

    /** Re-clear altitude for conflicts that has not been resolved */
    fun resolveExistingConflict() {
        val aircraftConflicts = radarScreen.collisionChecker.handoverAircraftStorage
        val conflictPoints = radarScreen.collisionChecker.handoverPointStorage

        for ((index, conflict) in aircraftConflicts.withIndex()) {
            val acft1 = conflict[0]
            val acft2 = conflict[1]

            //Check if conflict has already been handled
            var found = false
            for (list in aircraftList) {
                if (list.contains(acft1) && list.contains(acft2)) {
                    found = true
                    break
                }
            }
            if (found) continue

            val points = conflictPoints[index]
            val avgAlt = (points[0].altitude + points[1].altitude) / 2f

            //Store aircraft's cleared altitude prior to modification
            aircraftList.add(arrayOf(acft1, acft2))
            if (!targetAltitudeList.containsKey(acft1.callsign)) targetAltitudeList[acft1.callsign] = acft1.clearedAltitude
            if (!targetAltitudeList.containsKey(acft2.callsign)) targetAltitudeList[acft2.callsign] = acft2.clearedAltitude

            //Only modify cleared altitude if aircraft is not under your control, and is under centre control (not tower)
            when (acft1.isEligibleForHandoverCheck || acft2.isEligibleForHandoverCheck) {
                acft1.altitude > avgAlt && acft1.clearedAltitude < acft1.altitude && acft2.altitude < avgAlt && acft2.clearedAltitude > acft2.altitude -> {
                    //Case 1: 1st aircraft is descending from above, 2nd aircraft climbing from below
                    updateAIAltitude(acft1, ceil(avgAlt / 1000).toInt() * 1000)
                    updateAIAltitude(acft2, floor(avgAlt / 1000).toInt() * 1000)
                }
                acft1.altitude < avgAlt && acft1.clearedAltitude > acft1.altitude && acft2.altitude > avgAlt && acft2.clearedAltitude < acft2.altitude -> {
                    //Reverse of case 1
                    updateAIAltitude(acft1, floor(avgAlt / 1000).toInt() * 1000)
                    updateAIAltitude(acft2, ceil(avgAlt / 1000).toInt() * 1000)
                }
                acft1.altitude < avgAlt && acft1.clearedAltitude > acft1.altitude && acft2.altitude < avgAlt && acft2.clearedAltitude > acft2.altitude -> {
                    //Case 2: Both aircraft climbing from below, but will intersect due to vertical speed differences
                    //Clear the aircraft below to 2000 feet below the conflict alt floored
                    updateAIAltitude(if (acft1.altitude < acft2.altitude) acft1 else acft2, floor(avgAlt / 1000).toInt() * 1000 - 2000)
                }
                acft1.altitude > avgAlt && acft1.clearedAltitude < acft1.altitude && acft2.altitude > avgAlt && acft2.clearedAltitude < acft2.altitude -> {
                    //Case 3: Both aircraft descending from above, but will intersect due to vertical speed differences
                    //Clear the aircraft above to 2000 feet above the conflict alt ceiling-ed
                    updateAIAltitude(if (acft1.altitude > acft2.altitude) acft1 else acft2, ceil(avgAlt / 1000).toInt() * 1000 + 2000)
                }
                acft1.altitude == avgAlt && acft1.clearedAltitude == avgAlt.toInt() && avgAlt == acft2.altitude && acft2.clearedAltitude == avgAlt.toInt() -> {
                    //Case 4: Both aircraft flying level at same altitude
                    if (acft1 is Arrival && acft2 is Departure) {
                        updateAIAltitude(acft1, floor(avgAlt / 1000).toInt() * 1000)
                        updateAIAltitude(acft2, ceil(avgAlt / 1000).toInt() * 1000)
                    } else {
                        updateAIAltitude(acft1, ceil(avgAlt / 1000).toInt() * 1000)
                        updateAIAltitude(acft2, floor(avgAlt / 1000).toInt() * 1000)
                    }
                }
            }
        }
    }

    /** Check whether existing conflicts that are already handled can be cleared back to their target altitude */
    fun checkExistingConflicts() {
        for (iterableIndex in Array(aircraftList).withIndex()) {
            val aircraftArray = iterableIndex.value
            val acft1 = aircraftArray[0]
            val acft2 = aircraftArray[1]
            val acft1Target = targetAltitudeList[acft1.callsign] ?: acft1.targetAltitude
            val acft2Target = targetAltitudeList[acft2.callsign] ?: acft2.targetAltitude
            if (!acft1.isEligibleForHandoverCheck && !acft2.isEligibleForHandoverCheck) continue
            val traj1 = acft1.trajectory.getTrajectory(if (!acft1.isEligibleForHandoverCheck) -1 else acft1Target)
            val traj2 = acft2.trajectory.getTrajectory(if (!acft2.isEligibleForHandoverCheck) -1 else acft2Target)

            //Test if the 2 aircraft will be in conflict if re-cleared to original target altitude
            if (checkTrajectoryConflict(traj1, traj2) != -1) continue

            //If no more conflict between these 2, check for further clearance
            checkClearToTarget(acft1, acft1Target)
            checkClearToTarget(acft2, acft2Target)
            aircraftList.removeValue(aircraftArray, false)
        }
    }

    /** Checks and clears the aircraft to a further altitude */
    private fun checkClearToTarget(aircraft: Aircraft, targetAlt: Int) {
        val newTarget = checkOtherConflict(aircraft, targetAlt)
        updateAIAltitude(aircraft, newTarget)
    }

    /** Checks and clears all aircraft still in the targetAltitudeList */
    fun checkClearAllTargets() {
        val copy = HashMap(targetAltitudeList)
        for ((callsign, alt) in copy) {
            val aircraft = radarScreen.aircrafts[callsign] ?: continue
            var stillInConflict = false
            for (list in aircraftList) {
                if (list.contains(aircraft)) {
                    stillInConflict = true
                    break
                }
            }
            if (stillInConflict) continue
            if (!aircraft.isEligibleForHandoverCheck || aircraft.clearedAltitude == targetAltitudeList[aircraft.callsign]) {
                targetAltitudeList.remove(callsign)
                continue
            }
            var found = false
            for (list in aircraftList) {
                if (list.contains(aircraft)) {
                    found = true
                    break
                }
            }
            if (found) continue
            checkClearToTarget(aircraft, alt)
        }
    }

    /** Updates the cleared altitude after checking that aircraft is being controlled by centre */
    private fun updateAIAltitude(aircraft: Aircraft, newAlt: Int) {
        if (!aircraft.isEligibleForHandoverCheck) return
        aircraft.updateClearedAltitude(newAlt)
        aircraft.navState.replaceAllClearedAlt()

        //Remove from targetAltitudeList if cleared to the target altitude
        if (newAlt == targetAltitudeList[aircraft.callsign]) targetAltitudeList.remove(aircraft.callsign)
    }

    /** Returns the most suitable altitude to be cleared to */
    private fun checkOtherConflict(aircraft: Aircraft, targetAlt: Int): Int {
        val climbing = targetAlt > aircraft.altitude
        if (climbing) {
            for (alt in targetAlt downTo radarScreen.minAlt step 1000) {
                if (!checkConflictToAlt(aircraft, alt)) return alt
            }
        } else {
            for (alt in targetAlt..(radarScreen.maxAlt + 10000) step 1000) {
                if (!checkConflictToAlt(aircraft, alt)) return alt
            }
        }
        return targetAlt
    }

    /** Checks whether there will be any conflict encountered on the way to or at the required altitude, returns true if there will be conflict else false */
    private fun checkConflictToAlt(aircraft: Aircraft, newAlt: Int): Boolean {
        val newTrajectory = aircraft.trajectory.getTrajectory(newAlt)
        for (otherPlane in radarScreen.aircrafts.values) {
            if (otherPlane.callsign == aircraft.callsign) {
                continue
            }
            if (checkTrajectoryConflict(newTrajectory, otherPlane.trajectory.positionPoints) != -1) return true
        }
        return false
    }

    /** Checks whether there is conflict between the 2 supplied trajectories, returns avgAlt if conflict will occur, else -1 */
    private fun checkTrajectoryConflict(traj1: Array<PositionPoint>, traj2: Array<PositionPoint>): Int {
        for (i in 0 until 12.coerceAtMost(traj1.size).coerceAtMost(traj2.size)) {
            val point1 = traj1[i]
            val point2 = traj2[i]
            val dist = MathTools.pixelToNm(MathTools.distanceBetween(point1.x, point1.y, point2.x, point2.y))
            val minima = radarScreen.separationMinima.toFloat()
            if (abs(point1.altitude - point2.altitude) < 950 && dist < minima + 0.2f) {
                //Possible conflict, don't change cleared altitude for now
                return (point1.altitude + point2.altitude) / 2
            }
        }
        return -1
    }

    /** Loads the save data for existing conflicts */
    fun loadSaveData(save: JSONObject?) {
        if (save == null) return
        aircraftList.clear()
        targetAltitudeList.clear()

        val aircraftArray = save.getJSONArray("aircraftList")
        for (i in 0 until aircraftArray.length()) {
            val acft1 = radarScreen.aircrafts[aircraftArray.getJSONArray(i).getString(0)] ?: continue
            val acft2 = radarScreen.aircrafts[aircraftArray.getJSONArray(i).getString(1)] ?: continue
            aircraftList.add(arrayOf(acft1, acft2))
        }

        val altArray = save.getJSONObject("targetAltitudeList")
        for (key in altArray.keySet()) {
            targetAltitudeList[key] = altArray.getInt(key)
        }
    }
}