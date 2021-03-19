package com.bombbird.terminalcontrol.entities.approaches

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import org.json.JSONObject

class OffsetILS(airport: Airport, name: String, jsonObject: JSONObject): ILS(airport, name, jsonObject) {
    var lineUpDist = 0f
        private set
    lateinit var imaginaryIls: ILS
        private set

    init {
        loadImaginaryIls()
        calculateLDARings()

        lineUpDist = jsonObject.getDouble("lineUpDist").toFloat()
    }

    /** Loads the imaginary ILS from runway center line  */
    private fun loadImaginaryIls() {
        val jo = JSONObject()
        jo.put("runway", rwy.name)
        jo.put("heading", heading)
        jo.put("x", x.toDouble())
        jo.put("y", y.toDouble())
        jo.put("gsOffset", 0.0)
        jo.put("minima", 0)
        jo.put("maxGS", 4000)
        jo.put("tower", "${towerFreq[0]}>${towerFreq[1]}")
        jo.put("wpts", JSONObject())
        imaginaryIls = ILS(airport, "IMG" + rwy.name, jo)
    }

    /** Overrides method in ILS to ignore it if NPA  */
    private fun calculateLDARings() {
        if (!isNpa) {
            super.calculateGsRings()
        } else {
            val gsRings = Array<Vector2>()
            nonPrecAlts?.let {
                for (i in 0 until it.size) {
                    gsRings.add(Vector2(x + nmToPixel(it[i][1]) * MathUtils.cosDeg(270 - heading + (TerminalControl.radarScreen?.magHdgDev ?: 0f)), y + nmToPixel(it[i][1]) * MathUtils.sinDeg(270 - heading + (TerminalControl.radarScreen?.magHdgDev ?: 0f))))
                }
            }
            this.gsRings = gsRings
        }
    }
}