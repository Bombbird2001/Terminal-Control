package com.bombbird.terminalcontrol.entities.runways

import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.utilities.math.MathToolsKt

class RunwayConfig(private val airport: Airport, vararg runways: String): Comparable<RunwayConfig> {
    private val runways = Array<Runway?>()
    private var tailwindTotal: Float = 0f
    private var windScore: Float = 0f

    init {
        for (runway in runways) {
            this.runways.add(airport.runways[runway])
        }
    }

    fun calculateScores(windHdg: Int, windSpd: Int) {
        tailwindTotal = 0f
        windScore = 0f

        for (runway in runways) {
            if (runway == null) continue
            val component = MathToolsKt.componentInDirection(windSpd, windHdg, runway.heading)
            windScore += component
            tailwindTotal += (-component).coerceAtLeast(0f) //Add to tailwindTotal if component < 0, if headwind set 0
        }
    }

    override operator fun compareTo(other: RunwayConfig): Int {
        if (windScore != other.windScore) return if (windScore > other.windScore) -1 else 1
        return if (tailwindTotal < other.tailwindTotal) -1 else 1
    }
}