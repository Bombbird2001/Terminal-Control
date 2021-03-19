package com.bombbird.terminalcontrol.entities.approaches

import com.bombbird.terminalcontrol.entities.airports.Airport
import org.json.JSONObject

class Circling(airport: Airport, name: String, jsonObject: JSONObject): ILS(airport, name, jsonObject) {
    var minBreakAlt = 0
    var maxBreakAlt = 0
    var isLeft = false
    lateinit var imaginaryIls: ILS

    init {
        loadImaginaryIls()

        minBreakAlt = jsonObject.getInt("minBreakAlt")
        maxBreakAlt = jsonObject.getInt("maxBreakAlt")
        isLeft = jsonObject.getBoolean("left")
    }

    /** Loads the imaginary ILS from runway center line  */
    private fun loadImaginaryIls() {
        val jo = JSONObject()
        jo.put("runway", rwy.name)
        jo.put("heading", rwy.heading)
        jo.put("x", rwy.x.toDouble())
        jo.put("y", rwy.y.toDouble())
        jo.put("gsOffset", 0.0)
        jo.put("minima", minima)
        jo.put("maxGS", 4000)
        jo.put("tower", "${towerFreq[0]}>${towerFreq[1]}")
        jo.put("wpts", JSONObject())
        imaginaryIls = ILS(airport, "IMG" + rwy.name, jo)
    }
}