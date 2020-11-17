package com.bombbird.terminalcontrol.entities.runways

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.utilities.math.MathTools

/** Stores individual runway configuration info and comparison logic */
class RunwayConfig(private val airport: Airport, landingRunways: Array<String>, takeoffRunways: Array<String>): Comparable<RunwayConfig> {
    val landingRunwayMap = HashMap<String, Runway>()
    val takeoffRunwayMap = HashMap<String, Runway>()
    var allRunwaysEligible = false
    private var tailwindTotal = 0f
    private var windScore = 0f

    init {
        for (runway in landingRunways) {
            val rwy = airport.runways[runway]
            if (rwy == null) {
                Gdx.app.log("RunwayConfig", "Landing rwy $runway unavailable for ${airport.icao}")
            } else this.landingRunwayMap[rwy.name] = rwy
        }

        for (runway in takeoffRunways) {
            val rwy = airport.runways[runway]
            if (rwy == null) {
                Gdx.app.log("RunwayConfig", "Takeoff rwy $runway unavailable for ${airport.icao}")
            } else this.takeoffRunwayMap[rwy.name] = rwy
        }
    }

    /** Calculate comparison scores */
    fun calculateScores(windHdg: Int, windSpd: Int) {
        if (isEmpty()) {
            //Placeholder values: empty configurations will only be used if airport has only one configuration, which does not meet <5 knots tailwind requirement
            //Set allRunwaysEligible to true for empty configuration so it is used
            allRunwaysEligible = true
            return
        }

        tailwindTotal = 0f
        windScore = 0f

        allRunwaysEligible = true
        for (runway in landingRunwayMap.values) {
            val component = MathTools.componentInDirection(windSpd, windHdg, runway.heading)
            if (component < -5) allRunwaysEligible = false
            windScore += component
            tailwindTotal += (-component).coerceAtLeast(0f) //Add to tailwindTotal if component < 0, if headwind set 0
        }
        for (runway in takeoffRunwayMap.values) {
            val component = MathTools.componentInDirection(windSpd, windHdg, runway.heading)
            if (component < -5) allRunwaysEligible = false
            windScore += component * 2 //Takeoff wind more important, gives twice the score
            tailwindTotal += (-component).coerceAtLeast(0f) //Add to tailwindTotal if component < 0, if headwind set 0
        }
    }

    /** Applies this runway configuration to airport, returns whether the runway configuration has changed or not */
    fun applyConfig(): Boolean {
        val changeSet = HashSet<Runway>()
        val ldgCopy = HashMap(airport.landingRunways)
        for (runway: Runway in landingRunwayMap.values) {
            if (ldgCopy.remove(runway.name) == null) {
                changeSet.add(runway)
            }
        }
        val takeoffCopy = HashMap(airport.takeoffRunways)
        for (runway: Runway in takeoffRunwayMap.values) {
            if (takeoffCopy.remove(runway.name) == null) {
                changeSet.add(runway)
            }
        }
        changeSet.addAll(ldgCopy.values)
        changeSet.addAll(takeoffCopy.values)

        if (airport.rwyChangeTimer <= 0 && airport.isPendingRwyChange) {
            //Update only if timer is up
            for (runway: Runway in changeSet) {
                airport.setActive(runway.name, landingRunwayMap.containsKey(runway.name), takeoffRunwayMap.containsKey(runway.name))
            }
        }

        return changeSet.isNotEmpty()
    }

    /** Returns whether this configuration is an empty placeholder configuration */
    fun isEmpty(): Boolean {
        return landingRunwayMap.isEmpty() && takeoffRunwayMap.isEmpty()
    }

    /** Implements comparison operator; returns -1 if configuration is preferred over other, else 1 */
    override operator fun compareTo(other: RunwayConfig): Int {
        return if (allRunwaysEligible && !other.allRunwaysEligible) -1
        else if (!allRunwaysEligible && other.allRunwaysEligible) 1
        else if (allRunwaysEligible && other.allRunwaysEligible) if (windScore > other.windScore) -1 else 1 //Compare windScore if both runways eligible
        else if (tailwindTotal < other.tailwindTotal) -1 else 1 //If both ineligible, compare tailwindTotal instead
    }
}