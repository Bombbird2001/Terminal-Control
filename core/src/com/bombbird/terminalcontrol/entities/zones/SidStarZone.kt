package com.bombbird.terminalcontrol.entities.zones

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.sidstar.Route
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceFromBorder
import com.bombbird.terminalcontrol.utilities.math.MathTools.feetToPixel

class SidStarZone(private val route: Route, private val isSid: Boolean) {
    val polygons: Array<Polygon> = Array()
    val minAlt: Array<Int> = Array()

    /** Returns the required polygon given the starting position, track (direction) and dist (length)  */
    private fun calculatePolygon(posX: Float, posY: Float, track: Float, dist: Float, extraNm: Float): Polygon {
        val extraLengthPx = nmToPixel(extraNm)
        val leftX = posX - extraLengthPx
        val rightX = leftX + dist + 2 * extraLengthPx
        val topY = posY + extraLengthPx
        val bottomY = posY - extraLengthPx
        val polygon = Polygon(floatArrayOf(leftX, topY, rightX, topY, rightX, bottomY, leftX, bottomY))
        polygon.setOrigin(posX, posY)
        polygon.rotation = track
        return polygon
    }

    /** Calculates all route polygons  */
    fun calculatePolygons(lastWpt: Int) {
        for (i in 0 until route.waypoints.size) {
            val wpt1 = route.getWaypoint(i) ?: continue
            if (!wpt1.isInsideRadar) continue
            if (i + 1 < route.waypoints.size) {
                route.getWaypoint(i + 1)?.let { wpt2 ->
                    val routeVector = Vector2((wpt2.posX - wpt1.posX).toFloat(), (wpt2.posY - wpt1.posY).toFloat())
                    polygons.add(calculatePolygon(wpt1.posX.toFloat(), wpt1.posY.toFloat(), routeVector.angle(), routeVector.len(), 2f))
                    minAlt.add(route.getWptMinAlt(i + if (isSid) 0 else 1))
                }
            }
            if (i == lastWpt) {
                //Additionally calculates the polygon for inbound STAR/outbound SID segments - heading is from waypoint
                val outboundTrack = route.heading - (TerminalControl.radarScreen?.magHdgDev ?: 0f)
                val dist = distanceFromBorder(floatArrayOf(1260f, 4500f), floatArrayOf(0f, 3240f), wpt1.posX.toFloat(), wpt1.posY.toFloat(), outboundTrack)
                polygons.add(calculatePolygon(wpt1.posX.toFloat(), wpt1.posY.toFloat(), 90 - outboundTrack, dist, 2f))
                minAlt.add(route.getWptMinAlt(i))
            }
        }
    }

    /** Calculates the runway polygons for departures  */
    fun calculateDepRwyPolygons(runway: Runway, sid: Sid?, climbRate: Int) {
        polygons.add(calculatePolygon(runway.x, runway.y, 90 - runway.trueHdg, feetToPixel(runway.feetLength.toFloat()), 4f))
        minAlt.add(-1)
        val oppX = runway.oppRwy.x
        val oppY = runway.oppRwy.y
        val wptX = route.getWaypoint(0)?.posX?.toFloat() ?: 0f
        val wptY = route.getWaypoint(0)?.posY?.toFloat() ?: 0f
        val initClimb = sid?.getInitClimb(runway.name)
        val initialClimbAlt = initClimb?.get(1) ?: -1
        var initialClimbHdg = initClimb?.get(0) ?: -1
        if (initialClimbHdg == -1) initialClimbHdg = runway.heading
        if (initialClimbAlt != -1 && initialClimbAlt - runway.elevation > 800) {
            //Give some distance for aircraft to climb, in px
            val climbDist = nmToPixel((initialClimbAlt - runway.elevation) / 60f / climbRate * 220) //Assume 220 knots climb speed on average
            val track = 90 - (initialClimbHdg - (TerminalControl.radarScreen?.magHdgDev ?: 0f))
            polygons.add(calculatePolygon(oppX, oppY, track, climbDist, 3f))
            minAlt.add(-1)
            val intermediateVector = Vector2(climbDist, 0f)
            intermediateVector.rotate(track)
            intermediateVector.add(oppX, oppY)
            val wptVector = Vector2(wptX, wptY)
            val intermediateToWpt = wptVector.sub(intermediateVector)
            polygons.add(calculatePolygon(intermediateVector.x, intermediateVector.y, intermediateToWpt.angle(), intermediateToWpt.len(), 3f))
            minAlt.add(initialClimbAlt)
        } else {
            //Go directly to first waypoint
            val vector2 = Vector2(wptX - oppX, wptY - oppY)
            polygons.add(calculatePolygon(oppX, oppY, vector2.angle(), vector2.len(), 3f))
            minAlt.add(initialClimbAlt)
        }
    }

    /** Checks whether supplied aircraft is within any polygon, and is above polygon's minimum altitude, 100 ft leeway */
    fun contains(x: Float, y: Float, alt: Float): Boolean {
        for ((index, polygon) in polygons.withIndex()) {
            if (polygon.contains(x, y) && (minAlt[index] == -1 || alt + 100 > minAlt[index])) return true
        }
        return false
    }

}