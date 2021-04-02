package com.bombbird.terminalcontrol.entities.sidstar

import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.badlogic.gdx.utils.Array
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.HashMap

class Sid : SidStar {
    private val initClimb = HashMap<String, IntArray>()
    private val initRouteData = HashMap<String, RouteData>()
    private var transition = Array<Array<String>>()
    var centre = arrayOf("Control", "125.5")
        private set

    constructor(airport: Airport, jo: JSONObject) : super(airport) {
        parseInfo(jo)
    }

    constructor(airport: Airport, wpts: JSONArray, restrictions: JSONArray, fo: JSONArray, name: String) : super(airport, wpts, restrictions, fo, name)

    override fun parseInfo(jo: JSONObject) {
        super.parseInfo(jo)
        val rwys = jo.getJSONObject("rwys")
        for (rwy in rwys.keySet()) {
            runways.add(rwy)
            val routeData = RouteData()
            val rwyObject = rwys.getJSONObject(rwy)
            val initClimbData = rwyObject.getString("climb").split(" ".toRegex()).toTypedArray()
            val initWptData = rwyObject.getJSONArray("wpts")
            for (i in 0 until initWptData.length()) {
                val data = initWptData.getString(i).split(" ".toRegex()).toTypedArray()
                val wptName = data[0]
                routeData.add(
                    radarScreen.waypoints[wptName]!!,
                    intArrayOf(data[1].toInt(), data[2].toInt(), data[3].toInt()),
                    data.size > 4 && data[4] == "FO"
                )
            }
            initClimb[rwy] = intArrayOf(initClimbData[0].toInt(), initClimbData[1].toInt(), initClimbData[2].toInt())
            initRouteData[rwy] = routeData
        }
        val transitions = jo.getJSONArray("transitions")
        for (i in 0 until transitions.length()) {
            val trans = transitions.getJSONArray(i)
            val transData = Array<String>()
            for (j in 0 until trans.length()) {
                transData.add(trans.getString(j))
            }
            transition.add(transData)
        }
        val control = jo.getJSONArray("control")
        centre[0] = control.getString(0)
        centre[1] = control.getString(1)
    }

    val randomTransition: Array<String>?
        get() = if (transition.isEmpty) null else transition.random()

    fun getInitClimb(runway: String?): IntArray? {
        if (runway == null) return null
        return initClimb[runway]
    }

    fun getInitWpts(runway: String?): Array<Waypoint>? {
        if (runway == null) return null
        return initRouteData[runway]?.waypoints
    }

    fun getInitRestrictions(runway: String?): Array<IntArray>? {
        if (runway == null) return null
        return initRouteData[runway]?.restrictions
    }

    fun getInitFlyOver(runway: String?): Array<Boolean>? {
        if (runway == null) return null
        return initRouteData[runway]?.flyOver
    }

    fun isRadarDep(runway: String?): Boolean {
        if (runway == null) return false
        return initRouteData[runway]?.size == 0 && routeData.size == 0
    }
}