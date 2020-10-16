package com.bombbird.terminalcontrol.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.math.MathTools.pointsAtBorder
import com.bombbird.terminalcontrol.utilities.math.MathTools.withinRange

class RequestFlasher(private val radarScreen: RadarScreen) {
    private val camera: OrthographicCamera = radarScreen.camera

    fun update() {
        val ratio = TerminalControl.WIDTH.toFloat() / TerminalControl.HEIGHT
        val defaultRatio = 16 / 9f
        val totalX = ratio * 3240
        val xOffset = totalX - 5760
        var minX = 0f
        var minY = 0f
        var maxX = 0f
        var maxY = 0f
        val points = camera.frustum.planePoints
        for (i in 0..3) {
            if (i == 0) {
                minX = points[i].x + radarScreen.ui.paneWidth * camera.zoom - xOffset / 2 * camera.zoom
                minY = points[i].y
            } else if (i == 2) {
                maxX = points[i].x
                maxY = points[i].y
                if (ratio < defaultRatio) maxX += xOffset / 2 * camera.zoom
            }
        }
        val margin = camera.zoom * 80
        minX += margin
        minY += margin
        maxX -= margin
        maxY -= margin
        val ctrX = (minX + maxX) / 2
        val ctrY = (minY + maxY) / 2
        radarScreen.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (aircraft in radarScreen.aircrafts.values) {
            if ((aircraft.isConflict || aircraft.isTerrainConflict || aircraft.isTrajectoryTerrainConflict || aircraft.isTrajectoryConflict || aircraft.isActionRequired) && (!withinRange(aircraft.radarX, minX, maxX) || !withinRange(aircraft.radarY, minY, maxY))) {
                val deltaX: Float = aircraft.radarX - ctrX
                val deltaY: Float = aircraft.radarY - ctrY
                val indicationPoint = pointsAtBorder(floatArrayOf(minX, maxX), floatArrayOf(minY, maxY), (minX + maxX) / 2, (minY + maxY) / 2, 90 - MathUtils.radiansToDegrees * MathUtils.atan2(deltaY, deltaX))
                var color: Color?
                if (System.currentTimeMillis() % 2000 >= 1000) {
                    color = if (aircraft.isConflict || aircraft.isTerrainConflict) {
                        Color.RED
                    } else if (aircraft.isTrajectoryTerrainConflict || aircraft.isTrajectoryConflict) {
                        Color.MAGENTA
                    } else if (aircraft is Departure) {
                        Color.GREEN
                    } else {
                        Color(0f, 200 / 255f, 255f, 1f)
                    }
                    radarScreen.shapeRenderer.color = color
                    val radius = if (Gdx.app.type == Application.ApplicationType.Android) 50 else 30
                    radarScreen.shapeRenderer.circle(indicationPoint[0], indicationPoint[1], radius * camera.zoom)
                }
            }
        }
        radarScreen.shapeRenderer.end()
    }

}