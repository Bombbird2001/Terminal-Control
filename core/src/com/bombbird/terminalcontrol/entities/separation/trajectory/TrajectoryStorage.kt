package com.bombbird.terminalcontrol.entities.separation.trajectory

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl

class TrajectoryStorage {
    val points: Array<Array<Array<PositionPoint>>>
    private val radarScreen = TerminalControl.radarScreen!!
    private var timer: Float

    init {
        val requiredSize = radarScreen.areaWarning.coerceAtLeast(radarScreen.collisionWarning).coerceAtLeast(radarScreen.advTraj).coerceAtLeast(Trajectory.HANDOVER_PREDICT_TIMING) / Trajectory.UPDATE_INTERVAL
        points = Array(true, requiredSize)
        resetStorage()
        timer = 2.5f
    }

    /** Clears the storage before updating with new points  */
    private fun resetStorage() {
        points.clear()
        val maxTime = radarScreen.areaWarning.coerceAtLeast(radarScreen.collisionWarning).coerceAtLeast(radarScreen.advTraj).coerceAtLeast(Trajectory.HANDOVER_PREDICT_TIMING)
        for (j in 0 until maxTime / Trajectory.UPDATE_INTERVAL) {
            val altitudePositionMatrix = Array<Array<PositionPoint>>()
            for (i in 0 until Trajectory.MAX_ALT / 1000) {
                altitudePositionMatrix.add(Array())
            }
            points.add(altitudePositionMatrix)
        }
    }

    /** Main update function, updates every 5 seconds  */
    fun update() {
        timer -= Gdx.graphics.deltaTime * radarScreen.speed
        if (timer > 0) return
        timer += Trajectory.UPDATE_INTERVAL / 2.0f
        updateTrajPoints()
        radarScreen.areaPenetrationChecker.checkSeparation()
        radarScreen.collisionChecker.checkSeparation()

        //AI controller to prevent conflict between aircraft handed over to centre
        radarScreen.handoverController.checkExistingConflicts()
        radarScreen.handoverController.resolveExistingConflict()
        radarScreen.handoverController.checkClearAllTargets()
    }

    /** Calculates trajectory for all aircraft, updates points array with new trajectory points  */
    private fun updateTrajPoints() {
        resetStorage()
        for (aircraft in radarScreen.aircrafts.values) {
            aircraft.trajectory.updateTrajectory()
            for ((timeIndex, positionPoint) in aircraft.trajectory.positionPoints.withIndex()) {
                if (positionPoint.altitude / 1000 < Trajectory.MAX_ALT / 1000) {
                    points[timeIndex][positionPoint.altitude / 1000].add(positionPoint)
                }
            }
        }
    }
}