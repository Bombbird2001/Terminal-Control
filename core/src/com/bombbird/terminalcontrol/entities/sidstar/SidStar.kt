package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import org.json.JSONArray
import org.json.JSONObject

open class SidStar(val airport: Airport) {
    lateinit var name: String
    lateinit var runways: Array<String>
        private set
    lateinit var waypoints: Array<Waypoint>
        private set
    lateinit var restrictions: Array<IntArray>
        private set
    lateinit var flyOver: Array<Boolean>
        private set
    val radarScreen = TerminalControl.radarScreen!!
    lateinit var pronunciation: String

    constructor(airport: Airport, wpts: JSONArray, restriction: JSONArray, fo: JSONArray, name: String) : this(airport) {
        runways = Array()
        waypoints = Array()
        restrictions = Array()
        flyOver = Array()
        pronunciation = "null"
        this.name = name
        for (i in 0 until wpts.length()) {
            waypoints.add(radarScreen.waypoints[wpts.getString(i)])
            val data = restriction.getString(i).split(" ".toRegex()).toTypedArray()
            restrictions.add(intArrayOf(data[0].toInt(), data[1].toInt(), data[2].toInt()))
            flyOver.add(fo.getBoolean(i))
        }
    }

    /** Overridden method in SID, STAR to parse relevant information  */
    open fun parseInfo(jo: JSONObject) {
        runways = Array()
        waypoints = Array()
        restrictions = Array()
        flyOver = Array()
        pronunciation = jo.getString("pronunciation")
        val joWpts = jo.getJSONArray("route")
        for (i in 0 until joWpts.length()) {
            val data = joWpts.getString(i).split(" ".toRegex()).toTypedArray()
            val wptName = data[0]
            waypoints.add(radarScreen.waypoints[wptName])
            restrictions.add(intArrayOf(data[1].toInt(), data[2].toInt(), data[3].toInt()))
            val fo = data.size > 4 && data[4] == "FO"
            flyOver.add(fo)
            if (fo) Waypoint.flyOverPts[wptName] = true
        }
    }
}