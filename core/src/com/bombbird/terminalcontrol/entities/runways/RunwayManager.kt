package com.bombbird.terminalcontrol.entities.runways

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager
import com.bombbird.terminalcontrol.utilities.math.MathTools

/** Manages runway configurations for an airport */
class RunwayManager(private val airport: Airport, var prevNight: Boolean) {
    private val dayConfigs = Array<RunwayConfig>()
    private val nightConfigs = Array<RunwayConfig>()
    lateinit var latestRunwayConfig: RunwayConfig

    init {
        loadConfigs()
    }

    /** Loads the runway configurations for the selected airport */
    private fun loadConfigs() {
        when (airport.icao) {
            "TCTP" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("05L", "05R"), arrayOf("05L", "05R"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("23L", "23R"), arrayOf("23L", "23R"), false))
            }
            "TCSS" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("10"), arrayOf("10"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("28"), arrayOf("28"), false))
            }
            "TCWS" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("02L", "02C"), arrayOf("02L", "02C"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("20R", "20C"), arrayOf("20R", "20C"), false))
            }
            "TCTT" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("34L", "34R"), arrayOf("34R", "05"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("22", "23"), arrayOf("16L", "16R"), false))
                //runwayConfigs.add(RunwayConfig(airport, arrayOf("16L", "16R"), arrayOf("22", "16L"), false)) //16R for departure also?
                nightConfigs.add(RunwayConfig(airport, arrayOf("34L", "34R"), arrayOf("05"), true))
                nightConfigs.add(RunwayConfig(airport, arrayOf("22", "23"), arrayOf("16L"), true))
            }
            "TCAA" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("34L", "34R"), arrayOf("34L", "34R"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("16L", "16R"), arrayOf("16L", "16R"), false))
            }
            "TCBB" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("06L", "06R"), arrayOf("06L", "06R"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("24L", "24R"), arrayOf("24L", "24R"), false))
            }
            "TCOO" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("32L"), arrayOf("32L"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf(), arrayOf(), false)) //Empty runway configuration to represent unavailable runway configuration
            }
            "TCBE" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("09"), arrayOf("09"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf(), arrayOf(), false)) //Empty runway configuration to represent unavailable runway configuration
            }
            "TCHH" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("07L", "07R"), arrayOf("07L", "07R"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("25L", "25R"), arrayOf("25L", "25R"), false))
            }
            "TCMC" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("34"), arrayOf("34"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("16"), arrayOf("16"), false))
            }
            "TCBD" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("03L"), arrayOf("03R"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("21R"), arrayOf("21L"), false))
            }
            "TCBS" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("19L", "19R"), arrayOf("19L", "19R"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("01L", "01R"), arrayOf("01L", "01R"), false))
            }
            "TCMD" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("32L", "32R"), arrayOf("36L", "36R"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("18L", "18R"), arrayOf("14L", "14R"), false))
                nightConfigs.add(RunwayConfig(airport, arrayOf("32R"), arrayOf("36L"), true))
                nightConfigs.add(RunwayConfig(airport, arrayOf("18L"), arrayOf("14L"), true))
            }
            "TCPG" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("09L", "08R"), arrayOf("09R", "08L"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("26L", "27R"), arrayOf("27L", "26R"), false))
            }
            "TCPO" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("06"), arrayOf("07"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("25"), arrayOf("24"), false))
            }
            "TCHX" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("13"), arrayOf("13"), false))
                dayConfigs.add(RunwayConfig(airport, arrayOf("31"), arrayOf("31"), false))
            }
        }
    }

    /** Updates the runway configuration based on the winds, if required */
    fun updateRunways(windHdg: Int, windSpd: Int) {
        val arrayToUse = if (DayNightManager.isNight && !nightConfigs.isEmpty) nightConfigs else dayConfigs
        if (arrayToUse.isEmpty) {
            Gdx.app.log("RunwayManager", "No runway configurations available for ${airport.icao}")
            return
        }
        if (!requiresChange(windHdg, windSpd)) {
            if (airport.isPendingRwyChange) {
                airport.resetRwyChangeTimer()
            }
            return
        } //Return if no change needed
        for (config: RunwayConfig in arrayToUse) {
            config.calculateScores(windHdg, windSpd)
        }
        arrayToUse.sort()
        latestRunwayConfig = arrayToUse.first() //First element is the most preferred
        val pendingChange = latestRunwayConfig.applyConfig()
        if (pendingChange && !airport.isPendingRwyChange) {
            airport.isPendingRwyChange = true
            airport.rwyChangeTimer = 300f
            TerminalControl.radarScreen.utilityBox.commsManager.alertMsg("Runway change will occur for " + airport.icao + " soon due to change in winds. Tap the METAR label of " + airport.icao + " for more information.")
        } else if (!pendingChange && airport.isPendingRwyChange) {
            airport.resetRwyChangeTimer()
        }
    }

    /** Check if the current runway configuration needs to be changed due to winds */
    private fun requiresChange(windHdg: Int, windSpd: Int): Boolean {
        if (airport.takeoffRunways.isEmpty() && airport.landingRunways.isEmpty()) return true
        for (runway: Runway in airport.takeoffRunways.values) {
            if (MathTools.componentInDirection(windSpd, windHdg, runway.heading) < -5) return true
        }
        for (runway: Runway in airport.landingRunways.values) {
            if (MathTools.componentInDirection(windSpd, windHdg, runway.heading) < -5) return true
        }
        return prevNight != DayNightManager.isNight //Even if current config works, if config time no longer applies, needs to be changed also
    }

    /** Returns a list of suitable configurations with all runway tailwind < 5 knots AND is not a "blank configuration" */
    fun getSuitableConfigs(windHdg: Int, windSpd: Int): Array<RunwayConfig> {
        val suitableConfigs = Array<RunwayConfig>()
        val arrayToUse = if (DayNightManager.isNight && !nightConfigs.isEmpty) nightConfigs else dayConfigs
        for (config: RunwayConfig in arrayToUse) {
            if (config.isEmpty()) continue
            config.calculateScores(windHdg, windSpd)
            if (config.allRunwaysEligible) suitableConfigs.add(config)
        }

        return suitableConfigs
    }
}