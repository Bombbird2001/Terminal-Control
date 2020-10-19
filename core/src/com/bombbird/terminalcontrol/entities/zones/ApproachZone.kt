package com.bombbird.terminalcontrol.entities.zones

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import java.util.*

open class ApproachZone(private val rwy1: String, private val rwy2: String, xMid: Float, yMid: Float, apchHdg: Int, nozWidth: Float, nozLength: Float, ntzWidth: Float) {
    private var active = false
    private val noz1: Polygon
    private val noz2: Polygon
    private val ntz: Polygon

    //All dimensions in nautical miles
    init {
        var angle = 90 - apchHdg + (TerminalControl.radarScreen?.magHdgDev ?: 0f)
        val ntzCoord = FloatArray(8)
        val ntzWidthPx = nmToPixel(ntzWidth)
        val nozLengthPx = nmToPixel(nozLength)
        val nozWidthPx = nmToPixel(nozWidth)
        //NTZ
        //Front left
        angle -= 90f
        ntzCoord[0] = xMid - ntzWidthPx * MathUtils.cosDeg(angle) / 2
        ntzCoord[1] = yMid - ntzWidthPx * MathUtils.sinDeg(angle) / 2
        //Front right
        ntzCoord[6] = xMid + ntzWidthPx * MathUtils.cosDeg(angle) / 2
        ntzCoord[7] = yMid + ntzWidthPx * MathUtils.sinDeg(angle) / 2
        //Back left
        angle -= 90f
        val xOffset = nozLengthPx * MathUtils.cosDeg(angle)
        val yOffset = nozLengthPx * MathUtils.sinDeg(angle)
        ntzCoord[2] = ntzCoord[0] + xOffset
        ntzCoord[3] = ntzCoord[1] + yOffset
        //Back right
        ntzCoord[4] = ntzCoord[6] + xOffset
        ntzCoord[5] = ntzCoord[7] + yOffset
        ntz = Polygon(ntzCoord)

        //Left NOZ
        val noz1Coord = FloatArray(8)
        //Front left
        angle += 90f
        noz1Coord[0] = xMid - nozWidthPx * MathUtils.cosDeg(angle)
        noz1Coord[1] = yMid - nozWidthPx * MathUtils.sinDeg(angle)
        //Front right
        noz1Coord[6] = ntzCoord[0]
        noz1Coord[7] = ntzCoord[1]
        //Back left
        noz1Coord[2] = noz1Coord[0] + xOffset
        noz1Coord[3] = noz1Coord[1] + yOffset
        //Back right
        noz1Coord[4] = noz1Coord[6] + xOffset
        noz1Coord[5] = noz1Coord[7] + yOffset
        noz1 = Polygon(noz1Coord)

        //Right NOZ
        val noz2Coord = FloatArray(8)
        //Front right
        noz2Coord[0] = xMid + nozWidthPx * MathUtils.cosDeg(angle)
        noz2Coord[1] = yMid + nozWidthPx * MathUtils.sinDeg(angle)
        //Front left
        noz2Coord[6] = ntzCoord[6]
        noz2Coord[7] = ntzCoord[7]
        //Back right
        noz2Coord[2] = noz2Coord[0] + xOffset
        noz2Coord[3] = noz2Coord[1] + yOffset
        //Back left
        noz2Coord[4] = noz2Coord[6] + xOffset
        noz2Coord[5] = noz2Coord[7] + yOffset
        noz2 = Polygon(noz2Coord)
    }

    fun renderShape() {
        if (!active) return
        val shapeRenderer = TerminalControl.radarScreen?.shapeRenderer
        shapeRenderer?.color = Color.GREEN
        shapeRenderer?.polygon(noz1.vertices)
        shapeRenderer?.polygon(noz2.vertices)
        shapeRenderer?.color = Color.RED
        shapeRenderer?.polygon(ntz.vertices)
    }

    fun updateStatus(ldgRwys: HashMap<String, Runway>) {
        active = ldgRwys.containsKey(rwy1) && ldgRwys.containsKey(rwy2)
    }

    fun checkSeparation(plane1: Aircraft, plane2: Aircraft): Boolean {
        return noz1.contains(plane1.x, plane1.y) && noz2.contains(plane2.x, plane2.y) || noz1.contains(plane2.x, plane2.y) && noz2.contains(plane1.x, plane1.y)
    }
}