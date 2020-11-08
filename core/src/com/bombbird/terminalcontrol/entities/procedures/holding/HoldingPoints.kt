package com.bombbird.terminalcontrol.entities.procedures.holding

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import org.json.JSONObject

class HoldingPoints(wpt: String, val altRestrictions: IntArray, val maxSpd: Int, val isLeft: Boolean, val inboundHdg: Int, val legDist: Float) {
    val waypoint: Waypoint
    lateinit var oppPoint: FloatArray
        private set
    private val radarScreen = TerminalControl.radarScreen!!
    private val shapeRenderer = radarScreen.shapeRenderer

    companion object {
        //Constant turn diameter
        private const val turnDiameterNm = 3f
    }

    init {
        waypoint = radarScreen.waypoints[wpt]!!
        calculateOppPoint()
    }

    constructor(wpt: String, jo: JSONObject) : this(wpt, intArrayOf(jo.getInt("minAlt"), jo.getInt("maxAlt")), jo.getInt("maxSpd"), jo.getBoolean("left"), jo.getInt("inboundHdg"), jo.getDouble("legDist").toFloat())

    /** Calculates the coordinates of the point opposite to the fix in the holding pattern, using the given turn diameter and leg distance  */
    private fun calculateOppPoint() {
        val inboundTrack = inboundHdg - radarScreen.magHdgDev
        val legPxDist = nmToPixel(legDist)
        val xOffset1 = legPxDist * MathUtils.cosDeg(270 - inboundTrack)
        val yOffset1 = legPxDist * MathUtils.sinDeg(270 - inboundTrack)
        var xOffset2 = nmToPixel(turnDiameterNm) * MathUtils.cosDeg(-inboundTrack)
        var yOffset2 = nmToPixel(turnDiameterNm) * MathUtils.sinDeg(-inboundTrack)
        if (isLeft) {
            xOffset2 = -xOffset2
            yOffset2 = -yOffset2
        }
        oppPoint = floatArrayOf(waypoint.posX + xOffset1 + xOffset2, waypoint.posY + yOffset1 + yOffset2)
    }

    /** Renders the visuals for the holding pattern  */
    fun renderShape() {
        val radiusPx = nmToPixel(turnDiameterNm / 2f)
        var track1 = inboundHdg - radarScreen.magHdgDev
        track1 += if (isLeft) -90f else 90f
        val midpoint1 = floatArrayOf(waypoint.posX + radiusPx * MathUtils.cosDeg(90 - track1), waypoint.posY + radiusPx * MathUtils.sinDeg(90 - track1))
        val end1 = floatArrayOf(waypoint.posX + 2 * radiusPx * MathUtils.cosDeg(90 - track1), waypoint.posY + 2 * radiusPx * MathUtils.sinDeg(90 - track1))
        val track2 = track1 + 180
        val midpoint2 = floatArrayOf(oppPoint[0] + radiusPx * MathUtils.cosDeg(90 - track2), oppPoint[1] + radiusPx * MathUtils.sinDeg(90 - track2))
        val end2 = floatArrayOf(oppPoint[0] + 2 * radiusPx * MathUtils.cosDeg(90 - track2), oppPoint[1] + 2 * radiusPx * MathUtils.sinDeg(90 - track2))
        shapeRenderer.arc(midpoint1[0], midpoint1[1], radiusPx, 270 - (track1 + if (isLeft) 0 else -180), 180f)
        shapeRenderer.arc(midpoint2[0], midpoint2[1], radiusPx, 270 - (track2 + if (isLeft) 0 else -180), 180f)
        shapeRenderer.line(waypoint.posX.toFloat(), waypoint.posY.toFloat(), end2[0], end2[1])
        shapeRenderer.line(end1[0], end1[1], oppPoint[0], oppPoint[1])
        shapeRenderer.color = Color.BLACK
        shapeRenderer.line(waypoint.posX.toFloat(), waypoint.posY.toFloat(), end1[0], end1[1])
        shapeRenderer.line(oppPoint[0], oppPoint[1], end2[0], end2[1])
    }

    fun getEntryProc(heading: Double): Int {
        //Offset is relative to opposite of inbound heading
        var offset = heading - inboundHdg + 180
        if (offset < -180) {
            offset += 360.0
        } else if (offset > 180) {
            offset -= 360.0
        }
        return if (!isLeft) {
            if (offset > -1 && offset < 129) {
                1
            } else if (offset < -1 && offset > -69) {
                2
            } else {
                3
            }
        } else {
            if (offset < 1 && offset > -129) {
                1
            } else if (offset > 1 && offset < 69) {
                2
            } else {
                3
            }
        }
    }
}