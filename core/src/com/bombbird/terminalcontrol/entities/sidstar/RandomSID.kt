package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager.checkNoiseAllowed
import com.bombbird.terminalcontrol.utilities.errors.ErrorHandler
import java.lang.IllegalStateException

object RandomSID {
    /** Gets a random SID for the airport and runway  */
    fun randomSID(airport: Airport, rwy: String): Sid? {
        val possibleSids = Array<Sid>()
        for (sid in airport.sids.values) {
            if (sid.runways.contains(rwy, false) && checkNoise(airport, sid.name)) possibleSids.add(sid)
        }
        return if (possibleSids.size == 0) {
            Gdx.app.log("Random SID", "No SIDs found to match criteria for " + airport.icao + " " + rwy)
            ErrorHandler.sendGenericError(IllegalStateException("No SIDs found to match criteria for " + airport.icao + " " + rwy), false)
            return null
        } else {
            possibleSids[MathUtils.random(0, possibleSids.size - 1)]
        }
    }

    /** Check whether a SID is allowed to be used for the airport at the current time  */
    private fun checkNoise(airport: Airport, sid: String): Boolean {
        return airport.sids[sid]?.let { checkNoiseAllowed(it) } ?: false
    }
}