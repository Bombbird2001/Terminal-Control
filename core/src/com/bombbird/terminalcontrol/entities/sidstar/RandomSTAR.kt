package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager.checkNoiseAllowed
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import org.json.JSONObject
import java.util.*

object RandomSTAR {
    private val noise = HashMap<String, HashMap<String, Boolean>>()
    val time = HashMap<String, HashMap<String, Float>>()

    /** Loads STAR noise info for the airport  */
    fun loadStarNoise(icao: String) {
        noise[icao] = FileLoader.loadNoise(icao, false)
    }

    /** Loads arrival entry timings  */
    fun loadEntryTiming(airport: Airport) {
        val stars = HashMap<String, Float>()
        for (star in airport.stars.values) {
            stars[star.name] = 0f
        }
        time[airport.icao] = stars
    }

    /** Loads arrival entry timings from save  */
    fun loadEntryTiming(airport: Airport, jsonObject: JSONObject) {
        for (star in jsonObject.keySet()) {
            if (time[airport.icao]?.containsKey(star) == true) {
                time[airport.icao]?.set(star, jsonObject.getDouble(star).toFloat())
            }
        }
    }

    /** Updates timings  */
    fun update() {
        val dt = Gdx.graphics.deltaTime
        for (icao in time.keys) {
            val airportStars = time[icao] ?: continue
            for (star in airportStars.keys) {
                val starTime = airportStars[star] ?: continue
                time[icao]?.set(star, starTime - dt)
            }
        }
    }

    /** Gets a random STAR for the airport and runway  */
    fun randomSTAR(airport: Airport): Star {
        val rwys: HashMap<String, Runway> = airport.landingRunways
        val possibleStars = Array<Star>()
        for (star in airport.stars.values) {
            var found = false
            for (i in 0 until star.runways.size) {
                if (rwys.containsKey(star.runways.get(i))) {
                    found = true
                    break
                }
            }
            if (found && checkNoise(airport, star.name) && (time[airport.icao]?.get(star.name) ?: 1f) < 0) possibleStars.add(star)
        }
        return if (possibleStars.size == 0) {
            val runways = Array<String>()
            for (rwy in rwys.keys) {
                runways.add(rwy)
            }
            Gdx.app.log("Random STAR", "No STARs found to match criteria for " + airport.icao + " " + runways.toString())
            throw IllegalArgumentException("No STARs found to match criteria for " + airport.icao + " " + runways.toString())
        } else {
            possibleStars.random()
        }
    }

    /** Gets a list of all possible STARs that can be used with the current runway configuration  */
    fun getAllPossibleSTARnames(airport: Airport): Array<String> {
        val array = Array<String>()
        for (star in airport.stars.values) {
            var found = false
            for (i in 0 until star.runways.size) {
                if (airport.landingRunways.containsKey(star.runways.get(i))) {
                    found = true
                    break
                }
            }
            if (found && checkNoise(airport, star.name)) array.add(star.name + " arrival")
        }
        return array
    }

    /** Check whether a STAR is allowed to be used for the airport at the current time  */
    private fun checkNoise(airport: Airport, star: String): Boolean {
        return noise[airport.icao]?.get(star)?.let { checkNoiseAllowed(it) } ?: true //Star can be used both during day, night if not in hashMap
    }

    /** Used to check if any STAR is available at airport, called before spawning new arrival  */
    fun starAvailable(airport: Airport): Boolean {
        for (star in airport.stars.values) {
            if ((time[airport.icao]?.get(star.name) ?: 1f) < 0 && checkNoise(airport, star.name)) return true
        }
        return false
    }

    /** Resets STAR timer to 60 seconds after arrival spawns there */
    fun starUsed(icao: String, star: String) {
        time[icao]?.set(star, 60f)
    }
}