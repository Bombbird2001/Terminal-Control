package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class Star : SidStar {
    private lateinit var inbound: Array<Array<String>>
    private lateinit var rwyWpts: HashMap<String, Array<Waypoint>>
    private lateinit var rwyRestrictions: HashMap<String, Array<IntArray>>
    private lateinit var rwyFlyOver: HashMap<String, Array<Boolean>>

    constructor(airport: Airport, jo: JSONObject) : super(airport) {
        parseInfo(jo)
    }

    constructor(airport: Airport, wpts: JSONArray, restrictions: JSONArray, fo: JSONArray, name: String) : super(airport, wpts, restrictions, fo, name) {
        inbound = Array()
        rwyWpts = HashMap()
        rwyRestrictions = HashMap()
        rwyFlyOver = HashMap()
        val inbound1 = Array<String>()
        inbound1.add("HDG 360")
        inbound.add(inbound1)
    }

    override fun parseInfo(jo: JSONObject) {
        super.parseInfo(jo)
        inbound = Array()
        rwyWpts = HashMap()
        rwyRestrictions = HashMap()
        rwyFlyOver = HashMap()
        val rwys = jo.getJSONObject("rwys")
        for (rwy in rwys.keySet()) {
            runways.add(rwy)
            val wpts = Array<Waypoint>()
            val restrictions = Array<IntArray>()
            val flyOver = Array<Boolean>()
            val rwyObject = rwys.getJSONArray(rwy)
            for (i in 0 until rwyObject.length()) {
                val data = rwyObject.getString(i).split(" ".toRegex()).toTypedArray()
                val wptName = data[0]
                wpts.add(radarScreen.waypoints[wptName])
                restrictions.add(intArrayOf(data[1].toInt(), data[2].toInt(), data[3].toInt()))
                val fo = data.size > 4 && data[4] == "FO"
                flyOver.add(fo)
            }
            rwyWpts[rwy] = wpts
            rwyRestrictions[rwy] = restrictions
            rwyFlyOver[rwy] = flyOver
        }
        val inbounds = jo.getJSONArray("inbound")
        for (i in 0 until inbounds.length()) {
            val trans = inbounds.getJSONArray(i)
            val transData = Array<String>()
            for (j in 0 until trans.length()) {
                transData.add(trans.getString(j))
            }
            inbound.add(transData)
        }
    }

    val allInboundWpt: Array<String>
        get() {
            val wpts = Array<String>()
            for (i in 0 until inbound.size) {
                val inboundPts = inbound[i]
                if (inboundPts.size > 1) {
                    wpts.add(inboundPts[1].split(" ".toRegex()).toTypedArray()[1])
                }
            }
            wpts.add(routeData.waypoints.get(0).name)
            return wpts
        }

    val randomInbound: Array<String>
        get() = inbound[MathUtils.random(inbound.size - 1)]

    fun getRwyWpts(runway: String): Array<Waypoint> {
        return rwyWpts[runway] ?: Array()
    }

    fun getRwyRestrictions(runway: String): Array<IntArray> {
        return rwyRestrictions[runway] ?: Array()
    }

    fun getRwyFlyOver(runway: String): Array<Boolean> {
        return rwyFlyOver[runway] ?: Array()
    }
}