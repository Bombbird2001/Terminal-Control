package com.bombbird.terminalcontrol.entities.airports

import java.util.*

object AirportName {
    val airportNames = HashMap<String, String>()

    fun getAirportName(icao: String): String {
        return airportNames[icao] ?: "-"
    }
}