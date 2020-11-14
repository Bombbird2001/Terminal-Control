package com.bombbird.terminalcontrol.utilities.math.random

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen

class ArrivalGenerator(val radarScreen: RadarScreen, val allAircraft: HashSet<String>): RandomGenerator() {
    var finalAirport: Airport? = null

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
        if (aircraftInfo != null) done = true
    }
}