package com.bombbird.terminalcontrol.entities.approaches

import com.badlogic.gdx.math.Vector2
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.utilities.math.MathTools
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.sin

class RNAV(airport: Airport, name: String, jsonObject: JSONObject): Approach(airport, name, jsonObject) {
    val fafDist: Float

    init {
        isNpa = false
        fafDist = jsonObject.getDouble("fafDist").toFloat() + 3
        ilsDistNm = fafDist - gsOffsetNm
        distance1 = MathTools.nmToPixel(ilsDistNm)
        distance2 = distance1

        calculateGsRing()
    }

    /** Calculates position of the GS ring */
    private fun calculateGsRing() {
        gsRings.add(Vector2(x + MathTools.nmToPixel(ilsDistNm - 3) * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat(), y + MathTools.nmToPixel(ilsDistNm - 3) * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()))
    }
}