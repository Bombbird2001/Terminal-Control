package com.bombbird.terminalcontrol.entities.separation.trajectory

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl

class TrajectoryStorage {
    val points: Array<Array<Array<PositionPoint>>>
    private val radarScreen = TerminalControl.radarScreen!!
    private var timer: Float

    /** Clears the storage before updating with new points  */
    private fun resetStorage() {
        points.clear()
        var maxTime = radarScreen.areaWarning.coerceAtLeast(radarScreen.collisionWarning)
        maxTime = maxTime.coerceAtLeast(radarScreen.advTraj)
        for (j in 0 until maxTime / Trajectory.INTERVAL) {
            val altitudePositionMatrix = Array<Array<PositionPoint>>()
            for (i in 0 until radarScreen.maxAlt / 1000) {
                altitudePositionMatrix.add(Array())
            }
            points.add(altitudePositionMatrix)
        }
    }

    /** Main update function, updates every 5 seconds  */
    fun update() {
        timer -= Gdx.graphics.deltaTime * radarScreen.speed
        if (timer > 0) return
        timer += Trajectory.INTERVAL / 2.0f
        updateTrajPoints()
        radarScreen.areaPenetrationChecker.checkSeparation()
        radarScreen.collisionChecker.checkSeparation()
    }

    /** Calculates trajectory for all aircraft, updates points array with new trajectory points  */
    private fun updateTrajPoints() {
        resetStorage()
        for (aircraft in radarScreen.aircrafts.values) {
            aircraft.trajectory.calculateTrajectory()
            for ((timeIndex, positionPoint) in aircraft.trajectory.positionPoints.withIndex()) {
                if (positionPoint.altitude / 1000 < radarScreen.maxAlt / 1000) {
                    points[timeIndex][positionPoint.altitude / 1000].add(positionPoint)
                }
            }
        }
    }

    init {
        val requiredSize = radarScreen.areaWarning.coerceAtLeast(radarScreen.collisionWarning).coerceAtLeast(radarScreen.advTraj) / Trajectory.INTERVAL
        points = Array(true, requiredSize)
        resetStorage()
        timer = 2.5f
    }
}