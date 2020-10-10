package com.bombbird.terminalcontrol.utilities.math

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import org.apache.commons.lang3.ArrayUtils
import java.util.*

object RandomGenerator {
    private val excluded = Gdx.files.internal("game/aircrafts/exclude.air").readString().split("\\r?\\n".toRegex()).toTypedArray()

    /** Generates a random plane (with callsign, aircraft type)  */
    @JvmStatic
    fun randomPlane(airport: Airport): Array<String> {
        val size = airport.airlines.size
        var airline: String?
        var number: Int
        var aircraft = "A320"
        do {
            airline = airport.airlines[MathUtils.random(size - 1)]
            number = MathUtils.random(1, 999)
            val aircrafts = airport.aircrafts[airline]?.split(">".toRegex())?.toTypedArray() ?: continue
            aircraft = aircrafts[MathUtils.random(aircrafts.size - 1)]
        } while (ArrayUtils.contains(excluded, airline + number) || TerminalControl.radarScreen.allAircraft.contains(airline + number))
        TerminalControl.radarScreen.allAircraft.add(airline + number)
        return arrayOf(airline + number, aircraft)
    }

    /** Generates a random airport given the RadarScreen mainName variable  */
    @JvmStatic
    fun randomAirport(): Airport? {
        var total = 0
        val airportRange = HashMap<Airport, IntArray>()
        for (airport in TerminalControl.radarScreen.airports.values) {
            if (airport.isClosed || airport.landingRunways.size == 0) continue  //Don't spawn arrivals in a closed airport or airport with no landing runways available
            total += airport.aircraftRatio
            airportRange[airport] = intArrayOf(total - airport.aircraftRatio, total)
        }
        if (total < 1) return null //Return nothing if no airports available
        var airport: Airport? = null
        val index = MathUtils.random(1, total)
        for (airport1 in TerminalControl.radarScreen.airports.values) {
            val lowerRange: Int? = airportRange[airport1]?.get(0)
            val upperRange: Int? = airportRange[airport1]?.get(1)
            if (lowerRange != null && upperRange != null && index > lowerRange && index <= upperRange) {
                airport = airport1
            }
        }
        return airport
    }
}