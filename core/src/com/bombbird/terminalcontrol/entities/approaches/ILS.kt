package com.bombbird.terminalcontrol.entities.approaches

import com.badlogic.gdx.math.Vector2
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.sin

open class ILS(airport: Airport, name: String, jsonObject: JSONObject): Approach(airport, name, jsonObject) {
    init {
        calculateGsRings()
    }

    /** Calculates positions of the GS rings */
    fun calculateGsRings() {
        minAlt = -1
        for (i in 2..gsAlt / 1000) {
            if (i * 1000 > airport.elevation + 1000) {
                gsRings.add(Vector2(x + nmToPixel(getDistAtGsAlt(i * 1000f)) * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat(), y + nmToPixel(getDistAtGsAlt(i * 1000f)) * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()))
                if (minAlt == -1) minAlt = i
            }
        }
    }
}