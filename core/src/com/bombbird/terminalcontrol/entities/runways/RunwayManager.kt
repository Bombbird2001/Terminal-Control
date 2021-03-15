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
                dayConfigs.add(RunwayConfig(airport, arrayOf("05L", "05R"), arrayOf("05L", "05R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("23L", "23R"), arrayOf("23L", "23R")))
            }
            "TCSS" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("10"), arrayOf("10")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("28"), arrayOf("28")))
            }
            "TCWS" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("02L", "02C"), arrayOf("02L", "02C")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("02L", "02R"), arrayOf("02L", "02R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("20R", "20C"), arrayOf("20R", "20C")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("20L", "20C"), arrayOf("20L", "20C")))
            }
            "TCTT" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("34L", "34R"), arrayOf("34R", "05")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("22", "23"), arrayOf("16L", "16R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("16L", "16R"), arrayOf("22", "16R")))
                nightConfigs.add(RunwayConfig(airport, arrayOf("34L", "34R"), arrayOf("05")))
                nightConfigs.add(RunwayConfig(airport, arrayOf("22", "23"), arrayOf("16L")))
            }
            "TCAA" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("34L", "34R"), arrayOf("34L", "34R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("16L", "16R"), arrayOf("16L", "16R")))
            }
            "TCBB" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("06L", "06R"), arrayOf("06L", "06R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("24L", "24R"), arrayOf("24L", "24R")))
            }
            "TCOO" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("32L"), arrayOf("32L")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("14R"), arrayOf("14R")))
            }
            "TCBE" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("09"), arrayOf("09")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("27"), arrayOf("27")))
            }
            "TCHH" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("07L", "07R"), arrayOf("07L", "07R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("25L", "25R"), arrayOf("25L", "25R")))
            }
            "TCMC" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("34"), arrayOf("34")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("16"), arrayOf("16")))
            }
            "TCBD" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("03L"), arrayOf("03R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("21R"), arrayOf("21L")))
            }
            "TCBS" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("19L", "19R"), arrayOf("19L", "19R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("01L", "01R"), arrayOf("01L", "01R")))
            }
            "TCMD" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("32L", "32R"), arrayOf("36L", "36R")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("18L", "18R"), arrayOf("14L", "14R")))
                nightConfigs.add(RunwayConfig(airport, arrayOf("32R"), arrayOf("36L")))
                nightConfigs.add(RunwayConfig(airport, arrayOf("18L"), arrayOf("14L")))
            }
            "TCPG" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("09L", "08R"), arrayOf("09R", "08L")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("26L", "27R"), arrayOf("27L", "26R")))
            }
            "TCPO" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("06"), arrayOf("07")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("25"), arrayOf("24")))
            }
            "TCHX" -> {
                dayConfigs.add(RunwayConfig(airport, arrayOf("13"), arrayOf("13")))
                dayConfigs.add(RunwayConfig(airport, arrayOf("31"), arrayOf("31")))
            }
        }
    }

    /** Updates the runway configuration based on the winds, if required */
    fun updateRunways(windHdg: Int, windSpd: Int) {
        val arrayToUse = if (DayNightManager.isNight && !nightConfigs.isEmpty) Array(nightConfigs) else Array(dayConfigs)
        if (arrayToUse.isEmpty) {
            Gdx.app.log("RunwayManager", "No runway configurations available for ${airport.icao}")
            return
        }
        if (!requiresChange(windHdg, windSpd)) {
            if (airport.isPendingRwyChange) {
                airport.resetRwyChangeTimer()
            }
            prevNight = DayNightManager.isNight
            return
        } //Return if no change needed
        prevNight = DayNightManager.isNight
        for (config: RunwayConfig in arrayToUse) {
            config.calculateScores(windHdg, windSpd)
        }
        arrayToUse.sort()
        latestRunwayConfig = arrayToUse.first() //First element is the most preferred
        val pendingChange = latestRunwayConfig.applyConfig()
        if (pendingChange && !airport.isPendingRwyChange) {
            airport.isPendingRwyChange = true
            airport.rwyChangeTimer = 300f
            TerminalControl.radarScreen?.utilityBox?.commsManager?.alertMsg("Runway change will occur for " + airport.icao + " soon due to change in winds. Tap the METAR label of " + airport.icao + " for more information.")
        } else if (!pendingChange && airport.isPendingRwyChange) {
            airport.resetRwyChangeTimer()
        }
    }

    /** Check if the current runway configuration needs to be changed due to winds or change in night mode */
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

    /** Returns a list of suitable configurations with all runway tailwind < 5 knots, will exclude the "blank configuration" if at least 1 other config is available */
    fun getSuitableConfigs(windHdg: Int, windSpd: Int): Array<RunwayConfig> {
        val suitableConfigs = Array<RunwayConfig>()
        val arrayToUse = if (DayNightManager.isNight && !nightConfigs.isEmpty) Array(nightConfigs) else Array(dayConfigs)
        for (config: RunwayConfig in arrayToUse) {
            if (config.isEmpty() && !suitableConfigs.isEmpty) continue //If there is already at least 1 suitable configuration, don't add empty configs
            config.calculateScores(windHdg, windSpd)
            if (config.allRunwaysEligible) suitableConfigs.add(config)
        }

        if (suitableConfigs.size > 1 && suitableConfigs.first().isEmpty()) {
            suitableConfigs.removeIndex(0) //If first config is empty and there are other configs, remove it
        }

        return suitableConfigs
    }
}