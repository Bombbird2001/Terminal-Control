package com.bombbird.terminalcontrol.utilities.math.random

import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen

class DepartureGenerator(val radarScreen: RadarScreen, val airport: Airport, private val allAircraft: HashSet<String>, val delay: Int): RandomGenerator() {
    override fun run() {
        Thread.sleep(delay * 1000L)
        aircraftInfo = randomPlane(airport, allAircraft)
        if (aircraftInfo != null) done = true
    }
}