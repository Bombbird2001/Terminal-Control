package com.bombbird.terminalcontrol.entities.approaches

import com.bombbird.terminalcontrol.entities.airports.Airport

class Circling(airport: Airport, toParse: String): ILS(airport, toParse) {
    var minBreakAlt = 0
    var maxBreakAlt = 0
    var isLeft = false
    lateinit var dependentILS: ILS

    init {
        parseLDAInfo(toParse)
    }

    /** Overrides method in ILS to load additional circling approach data */
    private fun parseLDAInfo(toParse: String) {
        super.parseInfo(toParse)
        val info = toParse.split(",".toRegex()).toTypedArray()
        minBreakAlt = info[9].toInt()
        maxBreakAlt = info[10].toInt()
        isLeft = info[11] == "LEFT"
        dependentILS = airport.approaches[info[12]]!!
    }
}