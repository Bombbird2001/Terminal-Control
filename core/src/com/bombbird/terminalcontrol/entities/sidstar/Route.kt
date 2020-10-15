package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldProcedure
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.entities.zones.SidStarZone
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.pointsAtBorder
import org.json.JSONObject

class Route private constructor() {
    private val radarScreen: RadarScreen = TerminalControl.radarScreen
    val waypoints: Array<Waypoint> = Array()
    val restrictions: Array<IntArray> = Array()
    val flyOver: Array<Boolean> = Array()
    lateinit var holdProcedure: HoldProcedure
        private set
    var heading: Int
        private set
    private lateinit var sidStarZone: SidStarZone
    lateinit var name: String
        private set

    /** Basic constructor for Route, should only be called by other Route constructors  */
    init {
        heading = -1
    }

    /** Create a new Route based on STAR, for compatibility with older versions  */
    constructor(star: Star) : this() {
        val inbound = star.randomInbound
        if ("HDG" != inbound[0].split(" ".toRegex()).toTypedArray()[0]) {
            val data = inbound[0].split(" ".toRegex()).toTypedArray()
            waypoints.add(radarScreen.waypoints[data[1]])
            restrictions.add(intArrayOf(data[2].toInt(), data[3].toInt(), data[4].toInt()))
            flyOver.add(data.size > 5 && data[5] == "FO")
        }
        waypoints.addAll(star.waypoints)
        restrictions.addAll(star.restrictions)
        flyOver.addAll(star.flyOver)
        holdProcedure = HoldProcedure(star)
        name = star.name
        loadStarZone()
    }

    /** Create a new Route based on SID, for compatibility with older versions  */
    constructor(sid: Sid, runway: String) : this() {
        waypoints.addAll(sid.getInitWpts(runway))
        restrictions.addAll(sid.getInitRestrictions(runway))
        flyOver.addAll(sid.getInitFlyOver(runway))
        waypoints.addAll(sid.waypoints)
        restrictions.addAll(sid.restrictions)
        flyOver.addAll(sid.flyOver)
        val transition = sid.randomTransition
        for (i in 0 until transition.size) {
            val data = transition[i].split(" ".toRegex()).toTypedArray()
            if (data[0] == "WPT") {
                //Waypoint
                waypoints.add(TerminalControl.radarScreen.waypoints[data[1]])
                restrictions.add(intArrayOf(data[2].toInt(), data[3].toInt(), data[4].toInt()))
                flyOver.add(data.size > 5 && data[5] == "FO")
            }
        }
        holdProcedure = HoldProcedure()
        name = sid.name
        loadSidZone(null, null, -1)
    }

    /** Create new Route based on newly assigned STAR  */
    constructor(aircraft: Aircraft, star: Star) : this() {
        val inbound = star.randomInbound
        for (i in 0 until inbound.size) {
            if ("HDG" == inbound[i].split(" ".toRegex()).toTypedArray()[0]) {
                aircraft.heading = inbound[i].split(" ".toRegex()).toTypedArray()[1].toInt().toDouble()
                heading = aircraft.heading.toInt() + 180
            } else {
                val data = inbound[i].split(" ".toRegex()).toTypedArray()
                waypoints.add(radarScreen.waypoints[data[1]])
                restrictions.add(intArrayOf(data[2].toInt(), data[3].toInt(), data[4].toInt()))
                flyOver.add(data.size > 5 && data[5] == "FO")
            }
        }
        waypoints.addAll(star.waypoints)
        restrictions.addAll(star.restrictions)
        flyOver.addAll(star.flyOver)
        var runway: String? = null
        for (i in 0 until star.runways.size) {
            val rwy = star.runways[i]
            if (aircraft.airport.landingRunways.containsKey(rwy)) {
                runway = rwy
                break
            }
        }
        if (runway == null) {
            runway = if (!star.runways.isEmpty) {
                star.runways.first()
            } else {
                throw RuntimeException("Runway selected is null")
            }
        }
        waypoints.addAll(star.getRwyWpts(runway))
        restrictions.addAll(star.getRwyRestrictions(runway))
        flyOver.addAll(star.getRwyFlyOver(runway))
        holdProcedure = HoldProcedure(star)
        name = star.name
        loadStarZone()
    }

    /** Create new Route based on newly assigned SID  */
    constructor(aircraft: Aircraft, sid: Sid, runway: String, climbRate: Int) : this() {
        waypoints.addAll(sid.getInitWpts(runway))
        restrictions.addAll(sid.getInitRestrictions(runway))
        flyOver.addAll(sid.getInitFlyOver(runway))
        waypoints.addAll(sid.waypoints)
        restrictions.addAll(sid.restrictions)
        flyOver.addAll(sid.flyOver)
        val transition = sid.randomTransition
        for (i in 0 until transition.size) {
            val data = transition[i].split(" ".toRegex()).toTypedArray()
            if (data[0] == "WPT") {
                //Waypoint
                waypoints.add(TerminalControl.radarScreen.waypoints[data[1]])
                restrictions.add(intArrayOf(data[2].toInt(), data[3].toInt(), data[4].toInt()))
                flyOver.add(data.size > 5 && data[5] == "FO")
            } else {
                //Outbound heading
                (aircraft as Departure).outboundHdg = data[MathUtils.random(1, data.size - 1)].toInt()
                heading = aircraft.outboundHdg
            }
        }
        holdProcedure = HoldProcedure()
        name = sid.name
        loadSidZone(aircraft.airport.runways[runway], sid, climbRate)
    }

    /** Create new Route based on saved route, called only by other constructors  */
    private constructor(jo: JSONObject) : this() {
        val waypoints = jo.getJSONArray("waypoints")
        val restr = jo.getJSONArray("restrictions")
        val fo = jo.getJSONArray("flyOver")
        for (i in 0 until waypoints.length()) {
            this.waypoints.add(radarScreen.waypoints[waypoints.getString(i)])
            val data = restr.getString(i).split(" ".toRegex()).toTypedArray()
            restrictions.add(intArrayOf(data[0].toInt(), data[1].toInt(), data[2].toInt()))
            flyOver.add(fo.getBoolean(i))
        }
        holdProcedure = HoldProcedure()
        heading = jo.optInt("heading", -1)
        name = jo.optString("name", "null")
    }

    /** Create new Route based on saved route and SID name  */
    constructor(jo: JSONObject, sid: Sid?, runway: Runway?, climbRate: Int) : this(jo) {
        if ("null" == name && sid != null) name = sid.name
        loadSidZone(runway, sid, climbRate)
    }

    /** Create new Route based on saved route and STAR name  */
    constructor(jo: JSONObject, star: Star) : this(jo) {
        holdProcedure = HoldProcedure(star)
        name = star.name
        loadStarZone()
    }

    /** Loads sidStarZone for STAR routes  */
    private fun loadStarZone() {
        sidStarZone = SidStarZone(this, false)
        sidStarZone.calculatePolygons(0)
    }

    /** Loads sidStarZone for SID routes  */
    private fun loadSidZone(runway: Runway?, sid: Sid?, climbRate: Int) {
        sidStarZone = SidStarZone(this, true)
        sidStarZone.calculatePolygons(waypoints.size - 1)
        if (runway != null && sid != null && climbRate > -1) sidStarZone.calculateDepRwyPolygons(runway, sid, climbRate)
    }

    /** Draws the lines between aircraft, waypoints with shapeRenderer  */
    fun joinLines(start: Int, end: Int, outbound: Int) {
        var prevPt: Waypoint? = null
        var index = start
        while (index < end) {
            val waypoint = getWaypoint(index)
            if (prevPt != null && waypoint != null) {
                radarScreen.shapeRenderer.line(prevPt.posX.toFloat(), prevPt.posY.toFloat(), waypoint.posX.toFloat(), waypoint.posY.toFloat())
            }
            prevPt = waypoint
            index++
        }
        if (prevPt != null) {
            drawOutbound(prevPt.posX.toFloat(), prevPt.posY.toFloat(), outbound)
        }
    }

    /** Draws the outbound track from waypoint (if latMode is After waypoint, fly heading)  */
    private fun drawOutbound(previousX: Float, previousY: Float, outbound: Int) {
        if (outbound != -1 && previousX <= 4500 && previousX >= 1260 && previousY <= 3240 && previousY >= 0) {
            val outboundTrack = outbound - radarScreen.magHdgDev
            val point = pointsAtBorder(floatArrayOf(1260f, 4500f), floatArrayOf(0f, 3240f), previousX, previousY, outboundTrack)
            radarScreen.shapeRenderer.line(previousX, previousY, point[0], point[1])
        }
    }

    /** Draws the sidStarZone boundaries  */
    fun drawPolygons() {
        for (polygon in sidStarZone.polygons) {
            radarScreen.shapeRenderer.polygon(polygon.transformedVertices)
        }
    }

    /** Checks whether supplied coordinates is within the sidStarZone of the route  */
    fun inSidStarZone(x: Float, y: Float, alt: Float): Boolean {
        return sidStarZone.contains(x, y, alt)
    }

    fun getWaypoint(index: Int): Waypoint? {
        return if (index >= waypoints.size) {
            null
        } else waypoints[index]
    }

    /** Calculates distance between remaining points, excluding distance between aircraft and current waypoint  */
    fun distBetRemainPts(nextWptIndex: Int): Float {
        var currentIndex = nextWptIndex
        var dist = 0f
        while (currentIndex < waypoints.size - 1) {
            if (getWaypoint(currentIndex + 1)?.isInsideRadar != true) break
            dist += distBetween(currentIndex, currentIndex + 1)
            currentIndex++
        }
        return dist
    }

    /** Calculates distance between 2 waypoints in the route based on their indices  */
    fun distBetween(pt1: Int, pt2: Int): Float {
        val waypoint1 = getWaypoint(pt1)
        val waypoint2 = getWaypoint(pt2)
        return if (waypoint1 != null && waypoint2 != null) pixelToNm(distanceBetween(waypoint1.posX.toFloat(), waypoint1.posY.toFloat(), waypoint2.posX.toFloat(), waypoint2.posY.toFloat())) else 0f
    }

    /** Returns an array of waypoints from start to end index inclusive  */
    fun getRemainingWaypoints(start: Int, end: Int): Array<Waypoint?> {
        //Returns array of waypoints from index start to end
        val newRange = Array(waypoints)
        if (end >= start) {
            if (start > 0) {
                newRange.removeRange(0, start - 1)
            }
            val newEnd = end - start
            if (newEnd < newRange.size - 1) {
                newRange.removeRange(newEnd + 1, newRange.size - 1)
            }
        }
        return newRange
    }

    fun findWptIndex(wptName: String?): Int {
        return waypoints.indexOf(radarScreen.waypoints[wptName], false)
    }

    fun getWptMinAlt(wptName: String?): Int {
        return restrictions[findWptIndex(wptName)][0]
    }

    fun getWptMinAlt(index: Int): Int {
        return restrictions[index][0]
    }

    fun getWptMaxAlt(wptName: String?): Int {
        return restrictions[findWptIndex(wptName)][1]
    }

    fun getWptMaxAlt(index: Int): Int {
        return restrictions[index][1]
    }

    fun getWptMaxSpd(wptName: String?): Int {
        return restrictions[findWptIndex(wptName)][2]
    }

    fun getWptMaxSpd(index: Int): Int {
        return restrictions[index][2]
    }

    fun getWptFlyOver(wptName: String?): Boolean {
        return flyOver[findWptIndex(wptName)]
    }
}