package com.bombbird.terminalcontrol.entities.procedures.holding

import com.bombbird.terminalcontrol.entities.sidstar.Star
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import java.util.*

class HoldProcedure() {
    val holdingPoints: HashMap<String, HoldingPoints> = HashMap()

    constructor(star: Star) : this() {
        val waypoints = star.waypoints
        for (i in 0 until waypoints.size) {
            val wptName = waypoints[i].name
            val holdingPoint = star.airport.holdingPoints
            holdingPoint[wptName]?.let { holdingPoints[wptName] = it }
        }
    }

    fun getEntryProcAtWpt(waypoint: Waypoint, heading: Double): Int {
        return holdingPoints[waypoint.name]?.getEntryProc(heading) ?: 3
    }

    fun renderShape(waypoint: Waypoint) {
        holdingPoints[waypoint.name]?.renderShape()
    }

    fun getMaxSpdAtWpt(waypoint: Waypoint): Int {
        return holdingPoints[waypoint.name]?.maxSpd ?: 250
    }

    fun getInboundHdgAtWpt(waypoint: Waypoint): Int {
        return holdingPoints[waypoint.name]?.inboundHdg ?: 360
    }

    fun getLegDistAtWpt(waypoint: Waypoint): Float {
        return holdingPoints[waypoint.name]?.legDist ?: 5f
    }

    fun getOppPtAtWpt(waypoint: Waypoint): FloatArray {
        return holdingPoints[waypoint.name]?.oppPoint ?: floatArrayOf(0f, 0f)
    }

    fun isLeftAtWpt(waypoint: Waypoint): Boolean {
        return holdingPoints[waypoint.name]?.isLeft == true
    }

    fun getAltRestAtWpt(waypoint: Waypoint): IntArray {
        return holdingPoints[waypoint.name]?.altRestrictions ?: intArrayOf(-1, -1)
    }
}