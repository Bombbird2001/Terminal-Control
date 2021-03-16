package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import org.json.JSONArray
import org.json.JSONObject

open class SidStar(val airport: Airport) {
    lateinit var name: String
    lateinit var runways: Array<String>
        private set
    val routeData = RouteData()
    val radarScreen = TerminalControl.radarScreen!!
    lateinit var pronunciation: String

    constructor(airport: Airport, wpts: JSONArray, restriction: JSONArray, fo: JSONArray, name: String) : this(airport) {
        runways = Array()
        pronunciation = "null"
        this.name = name
        for (i in 0 until wpts.length()) {
            val data = restriction.getString(i).split(" ".toRegex()).toTypedArray()
            routeData.add(
                radarScreen.waypoints[wpts.getString(i)]!!,
                intArrayOf(data[0].toInt(), data[1].toInt(), data[2].toInt()),
                fo.getBoolean(i)
            )
        }
    }

    /** Overridden method in SID, STAR to parse relevant information  */
    open fun parseInfo(jo: JSONObject) {
        runways = Array()
        pronunciation = jo.getString("pronunciation")
        val joWpts = jo.getJSONArray("route")
        for (i in 0 until joWpts.length()) {
            val data = joWpts.getString(i).split(" ".toRegex()).toTypedArray()
            val wptName = data[0]
            routeData.add(
                radarScreen.waypoints[wptName]!!,
                intArrayOf(data[1].toInt(), data[2].toInt(), data[3].toInt()),
                data.size > 4 && data[4] == "FO"
            )
        }
    }
}