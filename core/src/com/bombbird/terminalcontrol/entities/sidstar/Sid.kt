package com.bombbird.terminalcontrol.entities.sidstar

import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class Sid : SidStar {
    private lateinit var initClimb: HashMap<String, IntArray>
    private lateinit var initWpts: HashMap<String, com.badlogic.gdx.utils.Array<Waypoint>>
    private lateinit var initRestrictions: HashMap<String, com.badlogic.gdx.utils.Array<IntArray>>
    private lateinit var initFlyOver: HashMap<String, com.badlogic.gdx.utils.Array<Boolean>>
    var transition = com.badlogic.gdx.utils.Array<com.badlogic.gdx.utils.Array<String>>()
        private set
    var centre = arrayOf("Control", "125.5")
        private set

    constructor(airport: Airport, jo: JSONObject) : super(airport) {
        parseInfo(jo)
    }

    constructor(airport: Airport, wpts: JSONArray, restrictions: JSONArray, fo: JSONArray, name: String) : super(airport, wpts, restrictions, fo, name) {
        initClimb = HashMap()
        initWpts = HashMap()
        initRestrictions = HashMap()
        initFlyOver = HashMap()
    }

    override fun parseInfo(jo: JSONObject) {
        super.parseInfo(jo)
        initClimb = HashMap()
        initWpts = HashMap()
        initRestrictions = HashMap()
        initFlyOver = HashMap()
        val rwys = jo.getJSONObject("rwys")
        for (rwy in rwys.keySet()) {
            runways.add(rwy)
            val wpts = com.badlogic.gdx.utils.Array<Waypoint>()
            val restrictions = com.badlogic.gdx.utils.Array<IntArray>()
            val flyOver = com.badlogic.gdx.utils.Array<Boolean>()
            val rwyObject = rwys.getJSONObject(rwy)
            val initClimbData = rwyObject.getString("climb").split(" ".toRegex()).toTypedArray()
            val initWptData = rwyObject.getJSONArray("wpts")
            for (i in 0 until initWptData.length()) {
                val data = initWptData.getString(i).split(" ".toRegex()).toTypedArray()
                val wptName = data[0]
                wpts.add(radarScreen.waypoints[wptName])
                restrictions.add(intArrayOf(data[1].toInt(), data[2].toInt(), data[3].toInt()))
                val fo = data.size > 4 && data[4] == "FO"
                flyOver.add(fo)
                if (fo) Waypoint.flyOverPts[wptName] = true
            }
            initClimb[rwy] = intArrayOf(initClimbData[0].toInt(), initClimbData[1].toInt(), initClimbData[2].toInt())
            initWpts[rwy] = wpts
            initRestrictions[rwy] = restrictions
            initFlyOver[rwy] = flyOver
        }
        val transitions = jo.getJSONArray("transitions")
        for (i in 0 until transitions.length()) {
            val trans = transitions.getJSONArray(i)
            val transData = com.badlogic.gdx.utils.Array<String>()
            for (j in 0 until trans.length()) {
                transData.add(trans.getString(j))
            }
            transition.add(transData)
        }
        val control = jo.getJSONArray("control")
        centre[0] = control.getString(0)
        centre[1] = control.getString(1)
    }

    val randomTransition: com.badlogic.gdx.utils.Array<String>?
        get() = if (transition.isEmpty) null else transition.random()

    fun getInitClimb(runway: String?): IntArray? {
        if (runway == null) return null
        return initClimb[runway]
    }

    fun getInitWpts(runway: String?): com.badlogic.gdx.utils.Array<Waypoint>? {
        if (runway == null) return null
        return initWpts[runway]
    }

    fun getInitRestrictions(runway: String?): com.badlogic.gdx.utils.Array<IntArray>? {
        if (runway == null) return null
        return initRestrictions[runway]
    }

    fun getInitFlyOver(runway: String?): com.badlogic.gdx.utils.Array<Boolean>? {
        if (runway == null) return null
        return initFlyOver[runway]
    }
}