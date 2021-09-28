package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.approaches.Approach
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldProcedure
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.entities.zones.SidStarZone
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.math.MathTools
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.pointsAtBorder
import org.json.JSONObject

class Route private constructor() {
    private val radarScreen: RadarScreen = TerminalControl.radarScreen!!
    private var isStar = false
    val routeData = RouteData()
    val routeDataDynamic = RouteData()
    lateinit var holdProcedure: HoldProcedure
        private set
    var heading: Int
        private set
    private lateinit var sidStarZone: SidStarZone
    lateinit var name: String
        private set
    val size
        get() = routeDataDynamic.size
    val waypoints
        get() = routeDataDynamic.waypoints
    var apchTrans = ""
    val removedPoints = RouteData()

    /** Initializes a default heading */
    init {
        heading = -1
    }

    /** Create new Route based on newly assigned STAR  */
    constructor(aircraft: Aircraft, star: Star, changeSTAR: Boolean) : this() {
        isStar = true
        val inbound = star.randomInbound
        for (i in 0 until inbound.size) {
            if ("HDG" == inbound[i].split(" ".toRegex()).toTypedArray()[0]) {
                if (!changeSTAR) aircraft.heading = inbound[i].split(" ".toRegex()).toTypedArray()[1].toInt().toDouble()
                heading = aircraft.heading.toInt() + 180
            } else {
                val data = inbound[i].split(" ".toRegex()).toTypedArray()
                routeData.add(
                    radarScreen.waypoints[data[1]]!!,
                    intArrayOf(data[2].toInt(), data[3].toInt(), data[4].toInt()),
                    data.size > 5 && data[5] == "FO"

                )
            }
        }
        routeData.addAll(star.routeData)
        var runway: String? = null
        for (i in 0 until star.runways.size) {
            val rwy = star.runways[i]
            if (aircraft.airport.landingRunways.containsKey(rwy)) {
                runway = rwy
                break
            }
        }
        if (runway == null) {
            runway = if (!star.runways.isEmpty) star.runways.first() else null
        }
        if (runway == null) throw RuntimeException("Runway selected is null")
        routeData.addAll(star.getRwyWpts(runway), star.getRwyRestrictions(runway), star.getRwyFlyOver(runway))
        recalculateRouteData()
        holdProcedure = HoldProcedure(star)
        name = star.name
        calculateStarZone()
    }

    /** Create new Route based on newly assigned SID  */
    constructor(aircraft: Aircraft, sid: Sid, runway: String, climbRate: Int) : this() {
        isStar = false
        routeData.addAll(sid.getInitWpts(runway)!!, sid.getInitRestrictions(runway)!!, sid.getInitFlyOver(runway)!!)
        routeData.addAll(sid.routeData)
        val transition = sid.randomTransition
        if (transition == null) {
            val waypoints = routeData.waypoints
            val calculatedTrack =  if (waypoints.size >= 2) {
                val wpt1 = waypoints[waypoints.size - 2]
                val wpt2 = waypoints[waypoints.size - 1]
                MathTools.getRequiredTrack(wpt1.posX, wpt1.posY, wpt2.posX, wpt2.posY).toInt()
            } else -1
            (aircraft as Departure).outboundHdg = calculatedTrack
            heading = calculatedTrack
        } else for (i in 0 until transition.size) {
            val data = transition[i].split(" ".toRegex()).toTypedArray()
            if (data[0] == "WPT") {
                //Waypoint
                    routeData.add(
                        radarScreen.waypoints[data[1]]!!,
                        intArrayOf(data[2].toInt(), data[3].toInt(), data[4].toInt()),
                        data.size > 5 && data[5] == "FO"
                    )
            } else {
                //Outbound heading
                (aircraft as Departure).outboundHdg = data[MathUtils.random(1, data.size - 1)].toInt()
                heading = aircraft.outboundHdg
            }
        }
        recalculateRouteData()
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
            val data = restr.getString(i).split(" ".toRegex()).toTypedArray()
            routeData.add(
                radarScreen.waypoints[waypoints.getString(i)]!!,
                intArrayOf(data[0].toInt(), data[1].toInt(), data[2].toInt()),
                fo.getBoolean(i)
            )
        }

        val removedWpts = jo.optJSONArray("removedWpts")
        val removedRestr = jo.optJSONArray("removedRestr")
        val removedFo = jo.optJSONArray("removedFo")
        if (removedWpts != null && removedRestr != null && removedFo != null) for (i in 0 until removedWpts.length()) {
            val data = removedRestr.getString(i).split(" ".toRegex()).toTypedArray()
            removedPoints.add(
                radarScreen.waypoints[removedWpts.getString(i)]!!,
                intArrayOf(data[0].toInt(), data[1].toInt(), data[2].toInt()),
                removedFo.getBoolean(i)
            )
        }

        holdProcedure = HoldProcedure()
        heading = jo.optInt("heading", -1)
        name = jo.optString("name", "null")
        apchTrans = jo.optString("apchTrans", "")
    }

    /** Create new Route based on saved route and SID name  */
    constructor(jo: JSONObject, sid: Sid, runway: Runway, climbRate: Int) : this(jo) {
        isStar = false
        if ("null" == name) name = sid.name
        loadSidZone(runway, sid, climbRate)
        recalculateRouteData()
    }

    /** Create new Route based on saved route and STAR name  */
    constructor(jo: JSONObject, star: Star) : this(jo) {
        isStar = true
        holdProcedure = HoldProcedure(star)
        name = star.name
        calculateStarZone()
        recalculateRouteData()
    }

    /** Loads sidStarZone for STAR routes  */
    private fun calculateStarZone() {
        sidStarZone = SidStarZone(this, false)
        sidStarZone.calculatePolygons(0)
    }

    /** Loads sidStarZone for SID routes  */
    private fun loadSidZone(runway: Runway?, sid: Sid?, climbRate: Int) {
        sidStarZone = SidStarZone(this, true)
        sidStarZone.calculatePolygons(routeData.waypoints.size - 1)
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
        return if (index >= routeDataDynamic.waypoints.size) {
            null
        } else routeDataDynamic.waypoints[index]
    }

    /** Calculates distance between remaining points, excluding distance between aircraft and current waypoint  */
    fun distBetRemainPts(nextWptIndex: Int): Float {
        var currentIndex = nextWptIndex
        var dist = 0f
        while (currentIndex < routeData.waypoints.size - 1) {
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
    fun getRemainingWaypoints(start: Int, end: Int): Array<Waypoint> {
        //Returns array of waypoints from index start to end
        val newRange = Array(routeDataDynamic.waypoints)
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
        return routeDataDynamic.waypoints.indexOf(radarScreen.waypoints[wptName], false)
    }

    fun getWptMinAlt(wptName: String?): Int {
        if (wptName == null) return -1
        return routeDataDynamic.restrictions[findWptIndex(wptName)][0]
    }

    fun getWptMinAlt(index: Int): Int {
        return routeDataDynamic.restrictions[index][0]
    }

    fun getWptMaxAlt(wptName: String?): Int {
        if (wptName == null) return -1
        return routeDataDynamic.restrictions[findWptIndex(wptName)][1]
    }

    fun getWptMaxAlt(index: Int): Int {
        return routeDataDynamic.restrictions[index][1]
    }

    fun getWptMaxSpd(wptName: String?): Int {
        if (wptName == null) return -1
        return routeDataDynamic.restrictions[findWptIndex(wptName)][2]
    }

    fun getWptMaxSpd(index: Int): Int {
        return routeDataDynamic.restrictions[index][2]
    }

    fun getWptFlyOver(wptName: String?): Boolean {
        if (wptName == null) return false
        return routeDataDynamic.flyOver[findWptIndex(wptName)]
    }

    /** Removes the waypoints from currently cleared approach, returns a boolean whether the aircraft is currently flying to one of the removed points */
    fun removeApchWpts(apch: Approach, direct: Waypoint?): Boolean {
        if (apch.routeDataMap.isEmpty() || routeData.waypoints.isEmpty) return false //No points to remove
        val wpts = apch.routeDataMap[apchTrans]?.waypoints ?: return false //If approach transition is not found, return
        val firstIndex = routeData.waypoints.lastIndexOf(wpts.first(), false)
        if (firstIndex == -1) return false //Route does not contain points
        routeData.removeRange(firstIndex, size - 1)
        routeData.addAll(removedPoints)
        removedPoints.clear()
        apchTrans = ""
        sidStarZone.calculatePolygons(0)
        recalculateRouteData()
        return wpts.contains(direct, false)
    }

    /** Adds the appropriate transition waypoints for the selected approach, returns a boolean whether any points were added */
    fun addApchWpts(apch: Approach, direct: Waypoint?): Boolean {
        if (apch.routeDataMap.isEmpty()) return false //No points to add
        removedPoints.clear()
        val trip = apch.getNextPossibleTransition(direct, this)
        trip.second.waypoints.let {
            if (it.isEmpty) return@let
            val firstIndex = findWptIndex(it.first().name)
            if (firstIndex > -1) routeData.removeRange(firstIndex, size - 1)
            removedPoints.addAll(trip.second)
        }
        trip.first.waypoints.let {
            if (it.isEmpty) return false
            if (waypoints.contains(it.first(), false)) return false //If waypoint already inside, don't add
            apchTrans = trip.third?.name ?: "vector"
            routeData.addAll(trip.first)
        }
        sidStarZone.calculatePolygons(0)
        recalculateRouteData()
        return true
    }

    /** Performs a recalculation of the route waypoint restrictions, due to a change in waypoints */
    private fun recalculateRouteData() {
        routeDataDynamic.clear()
        var minAlt = -1
        var maxAlt = -1
        var maxSpd = -1
        val wptData = routeData.waypoints
        val restrData = routeData.restrictions
        val foData = routeData.flyOver
        for (i in 0 until routeData.size) {
            if (isStar) {
                //Loop through for maximum altitude, waypoints, flyover, max speed
                if ((restrData[i][1] != -1 && maxAlt > restrData[i][1]) || (maxAlt == -1 && restrData[i][1] != -1)) maxAlt = restrData[i][1]
                if ((restrData[i][2] != -1 && maxSpd > restrData[i][2]) || (maxSpd == -1 && restrData[i][2] != -1)) maxSpd = restrData[i][2]
                routeDataDynamic.add(wptData[i], intArrayOf(-1, maxAlt, maxSpd), foData[i])
            } else {
                //Loop through for minimum altitude, waypoints, flyover
                if ((restrData[i][0] != -1 && minAlt < restrData[i][0]) || (minAlt == -1 && restrData[i][0] != -1)) minAlt = restrData[i][0]
                routeDataDynamic.add(wptData[i], intArrayOf(minAlt, -1, -1), foData[i])
            }
        }
        for (i in routeData.size - 1 downTo 0) {
            if (isStar) {
                //Loop through for minimum altitude
                if ((restrData[i][0] != -1 && minAlt < restrData[i][0]) || (minAlt == -1 && restrData[i][0] != -1)) minAlt = restrData[i][0]
                routeDataDynamic.restrictions[i][0] = minAlt
            } else {
                //Loop through for maximum altitude, max speed
                if ((restrData[i][1] != -1 && maxAlt > restrData[i][1]) || (maxAlt == -1 && restrData[i][1] != -1)) maxAlt = restrData[i][1]
                if ((restrData[i][2] != -1 && maxSpd > restrData[i][2]) || (maxSpd == -1 && restrData[i][2] != -1)) maxSpd = restrData[i][2]
                routeDataDynamic.restrictions[i][1] = maxAlt
                routeDataDynamic.restrictions[i][2] = maxSpd
            }
        }
    }
}