package com.bombbird.terminalcontrol.entities.weather

import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import java.util.*
import kotlin.math.exp

object WindshearChance {
    private val radarScreen = TerminalControl.radarScreen!!
    private val logRegCoefficients = HashMap<String, FloatArray>()
    fun loadWsChance() {
        logRegCoefficients["TCTP"] = floatArrayOf(-7.828657998633319f, 0.3877695849020738f)
        logRegCoefficients["TCSS"] = floatArrayOf(-7.570236541732326f, 0.42294053309933444f)
        logRegCoefficients["TCWS"] = floatArrayOf(-8.192262378235482f, 0.24738504082400115f)
        logRegCoefficients["TCTT"] = floatArrayOf()
        logRegCoefficients["TCAA"] = floatArrayOf(-6.916322317610984f, 0.30476535614392136f)
        logRegCoefficients["TCBB"] = floatArrayOf()
        logRegCoefficients["TCOO"] = floatArrayOf(-8.435239918930467f, 0.2668787164182838f)
        logRegCoefficients["TCBE"] = floatArrayOf()
        logRegCoefficients["TCHH"] = floatArrayOf(-6.956372521078374f, 0.22558516085627847f)
        logRegCoefficients["TCMC"] = floatArrayOf()
        logRegCoefficients["TCBD"] = floatArrayOf(-12.598352844435272f, 0.4759057515584942f)
        logRegCoefficients["TCBS"] = floatArrayOf(-11.162357827620264f, 0.4884167870726882f)
        logRegCoefficients["TCMD"] = floatArrayOf(-5.842837306470557f, 0.18198226237092718f)
        logRegCoefficients["TCPG"] = floatArrayOf()
        logRegCoefficients["TCPO"] = floatArrayOf()
        logRegCoefficients["TCHX"] = floatArrayOf(-6.956372521078374f, 0.22558516085627847f)
    }

    private fun getRandomWs(icao: String, speed: Int): Boolean {
        if (!logRegCoefficients.containsKey(icao) || logRegCoefficients[icao]?.isEmpty() == true) return false
        val b0 = logRegCoefficients[icao]?.get(0) ?: return false
        val b1 = logRegCoefficients[icao]?.get(1) ?: return false
        val prob = (1 / (1 + exp(-b0 - b1 * speed.toDouble()))).toFloat()
        return MathUtils.randomBoolean(prob)
    }

    fun getRandomWsForAllRwy(icao: String, speed: Int): String {
        var ws = ""
        val airport = radarScreen.airports[icao] ?: return ws
        val stringBuilder = StringBuilder()
        for (runway in airport.runways.values) {
            if (!runway.isLanding) continue
            if (getRandomWs(airport.icao, speed)) {
                stringBuilder.append("R")
                stringBuilder.append(runway.name)
                stringBuilder.append(" ")
            }
        }
        ws = if (stringBuilder.length > 3 && stringBuilder.length == airport.landingRunways.size * 3) {
            "ALL RWY"
        } else {
            stringBuilder.toString()
        }
        return ws
    }
}