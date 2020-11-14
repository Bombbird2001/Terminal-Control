package com.bombbird.terminalcontrol.utilities.math.random

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import org.apache.commons.lang3.ArrayUtils
import java.util.*
import kotlin.collections.HashSet

open class RandomGenerator: Runnable {
    companion object {
        var id = 0
    }
    private val excluded = Gdx.files.internal("game/aircrafts/exclude.air").readString().split("\\r?\\n".toRegex()).toTypedArray()

    val thisId = id
    var done = false
    var aircraftInfo: Array<String>? = null
    var cycles = 0

    init {
        id++
    }

    override fun run() {}

    /** Generates a random plane (with callsign, aircraft type)  */
    fun randomPlane(airport: Airport, allAircraft: HashSet<String>): Array<String>? {
        val size = airport.airlines.size
        var airline: String?
        var number: Int
        var aircraft = "A320"
        do {
            if (cycles >= 100) return null
            airline = airport.airlines[MathUtils.random(size - 1)]
            number = MathUtils.random(1, 999)
            val aircrafts = airport.aircrafts[airline]?.split(">".toRegex())?.toTypedArray()
            if (aircrafts == null) {
                cycles = 100
                break
            }
            aircraft = aircrafts[MathUtils.random(aircrafts.size - 1)]
            cycles++
        } while (ArrayUtils.contains(excluded, airline + number) || allAircraft.contains(airline + number))
        return arrayOf(airline + number, aircraft)
    }

    /** Generates a random airport */
    fun randomAirport(): Airport? {
        val radarScreen = TerminalControl.radarScreen!!
        var total = 0
        val airportRange = HashMap<Airport, IntArray>()
        for (airport in radarScreen.airports.values) {
            if (airport.isClosed || airport.landingRunways.size == 0) continue  //Don't spawn arrivals in a closed airport or airport with no landing runways available
            total += airport.aircraftRatio
            airportRange[airport] = intArrayOf(total - airport.aircraftRatio, total)
        }
        if (total < 1) return null //Return nothing if no airports available
        var airport: Airport? = null
        val index = MathUtils.random(1, total)
        for (airport1 in radarScreen.airports.values) {
            val lowerRange: Int? = airportRange[airport1]?.get(0)
            val upperRange: Int? = airportRange[airport1]?.get(1)
            if (lowerRange != null && upperRange != null && index > lowerRange && index <= upperRange) {
                airport = airport1
            }
        }
        return airport
    }
}