package com.bombbird.terminalcontrol.entities.approaches

import com.bombbird.terminalcontrol.entities.airports.Airport
import org.apache.commons.lang3.StringUtils

class Circling(airport: Airport, toParse: String): ILS(airport, toParse) {
    var minBreakAlt = 0
    var maxBreakAlt = 0
    var isLeft = false
    lateinit var imaginaryIls: ILS

    init {
        parseLDAInfo(toParse)
        loadImaginaryIls()
    }

    /** Overrides method in ILS to load additional circling approach data */
    private fun parseLDAInfo(toParse: String) {
        super.parseInfo(toParse)
        val info = toParse.split(",".toRegex()).toTypedArray()
        minBreakAlt = info[9].toInt()
        maxBreakAlt = info[10].toInt()
        isLeft = info[11] == "LEFT"
    }

    /** Loads the imaginary ILS from runway center line  */
    private fun loadImaginaryIls() {
        val text = "IMG" + rwy?.name + "," + rwy?.name + "," + rwy?.heading + "," + rwy?.x + "," + rwy?.y + ",0," + minima + ",4000," + StringUtils.join(towerFreq, ">")
        imaginaryIls = ILS(airport, text)
    }
}