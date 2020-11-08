package com.bombbird.terminalcontrol.utilities.math

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import org.apache.commons.lang3.ArrayUtils
import java.util.*
import kotlin.collections.HashSet

object RandomGenerator {
    class MultiThreadGenerator(val radarScreen: RadarScreen, val allAircraft: HashSet<String>): Runnable {
        var done = false
        var finalAirport: Airport? = null
        var aircraftInfo: Array<String>? = null

        override fun run() {
            val airport = randomAirport()
            if (airport == null) {
                //If airports not available, set planes to control equal to current arrival number
                //so there won't be a sudden wave of new arrivals once airport is available again
                Gdx.app.postRunnable { radarScreen.planesToControl = radarScreen.arrivals.toFloat() }
                return
            }
            if (!RandomSTAR.starAvailable(airport)) {
                Gdx.app.postRunnable { radarScreen.spawnTimer = 10f } //Wait for another 10 seconds if no spawn points available
                return
            }
            finalAirport = airport
            aircraftInfo = randomPlane(airport, allAircraft)
            done = true
        }
    }

    private val excluded = Gdx.files.internal("game/aircrafts/exclude.air").readString().split("\\r?\\n".toRegex()).toTypedArray()

    /** Generates a random plane (with callsign, aircraft type)  */
    fun randomPlane(airport: Airport, allAircraft: HashSet<String>): Array<String> {
        val size = airport.airlines.size
        var airline: String?
        var number: Int
        var aircraft = "A320"
        do {
            airline = airport.airlines[MathUtils.random(size - 1)]
            number = MathUtils.random(1, 999)
            val aircrafts = airport.aircrafts[airline]?.split(">".toRegex())?.toTypedArray() ?: continue
            aircraft = aircrafts[MathUtils.random(aircrafts.size - 1)]
        } while (ArrayUtils.contains(excluded, airline + number) || allAircraft.contains(airline + number))
        return arrayOf(airline + number, aircraft)
    }

    /** Generates a random airport given the RadarScreen mainName variable  */

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