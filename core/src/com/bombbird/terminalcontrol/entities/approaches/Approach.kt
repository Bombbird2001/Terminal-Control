package com.bombbird.terminalcontrol.entities.approaches

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.math.MathTools
import org.json.JSONObject
import java.util.HashMap

open class Approach(val airport: Airport, name: String, jsonObject: JSONObject) : Actor() {
    companion object {
        const val angle1 = 6
        const val angle2 = 3
        const val dashDistNm = 1f
    }

    var distance1 = MathTools.nmToPixel(10f)
    var ilsDistNm = 25f
    var distance2 = MathTools.nmToPixel(ilsDistNm)

    private lateinit var wpts: Array<Waypoint>
    private lateinit var restrictions: Array<IntArray>
    private lateinit var flyOver: Array<Boolean>

    private var name: String
    private var x = 0f
    private var y = 0f
    var heading = 0
        private set
    var gsOffsetNm = 0f
    var minima = 0
        private set
    var gsAlt = 0
        private set
    lateinit var towerFreq: kotlin.Array<String>
        private set
    lateinit var rwy: Runway
        private set
    val missedApchProc: MissedApproach
    var isNpa = false
    var nonPrecAlts: Queue<FloatArray>? = null
    var minAlt = 0
    val radarScreen: RadarScreen = TerminalControl.radarScreen!!

    init {
        this.name = name
        loadInfo(jsonObject)

        var missed = name
        if ("IMG" == name.substring(0, 3)) {
            missed = "LDA" + rwy.name
            if (!airport.missedApproaches.containsKey(missed)) missed = "CIR" + rwy.name
        }
        if ("TCHX" == airport.icao && "IMG13" == name) missed = "IGS13"
        missedApchProc = airport.missedApproaches[missed]!!
    }

    private fun loadInfo(jo: JSONObject) {
        rwy = airport.runways[jo.getString("runway")]!!
        heading = jo.getInt("heading")
        x = jo.getDouble("x").toFloat()
        y = jo.getDouble("y").toFloat()
        gsOffsetNm = jo.getDouble("gsOffset").toFloat()
        minima = jo.getInt("minima")
        gsAlt = jo.getInt("maxGS")
        towerFreq = jo.getString("tower").split(">".toRegex()).toTypedArray()

        val alts = jo.getJSONArray("npa")
        if (alts.length() > 0) {
            isNpa = true
            nonPrecAlts = Queue()
            for (i in 0 until alts.length()) {
                val altDist = FloatArray(2)
                val data = alts.getString(i).split(">")
                altDist[0] = data[0].toFloat()
                altDist[1] = data[1].toFloat()
                nonPrecAlts?.addLast(altDist)
            }
        }

        wpts = Array()
        restrictions = Array()
        flyOver = Array()
        val joWpts = jo.getJSONArray("wpts")
        for (i in 0 until joWpts.length()) {
            val data = joWpts.getString(i).split(" ".toRegex()).toTypedArray()
            val wptName = data[0]
            wpts.add(radarScreen.waypoints[wptName])
            restrictions.add(intArrayOf(data[1].toInt(), data[2].toInt(), data[3].toInt()))
            val fo = data.size > 4 && data[4] == "FO"
            flyOver.add(fo)
        }
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String) {
        this.name = name
    }

    override fun getX(): Float {
        return x
    }

    override fun setX(x: Float) {
        this.x = x
    }

    override fun getY(): Float {
        return y
    }

    override fun setY(y: Float) {
        this.y = y
    }
}