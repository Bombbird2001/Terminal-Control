package com.bombbird.terminalcontrol.entities.trafficmanager

import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import java.util.*

object DayNightManager {
    private val NIGHT_AVAILABLE = Array<String>()

    private fun loadArray() {
        NIGHT_AVAILABLE.add("TCTT", "TCHH", "TCBB", "TCMD")
    }

    /** Checks if a SID/STAR is allowed depending on whether night mode is active */
    @JvmStatic
    fun checkNoiseAllowed(night: Boolean): Boolean {
        return if (isNight) night else !night
    }

    /** Checks whether airport is utilising night operations */
    @JvmStatic
    val isNight: Boolean
        get() {
            if (!isNightAvailable || !TerminalControl.radarScreen.allowNight) return false
            val radarScreen = TerminalControl.radarScreen
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val additional = if (calendar[Calendar.AM_PM] == Calendar.PM) 12 else 0
            val time = (calendar[Calendar.HOUR] + additional) * 100 + calendar[Calendar.MINUTE]
            return if (radarScreen.nightEnd <= radarScreen.nightStart) {
                //Cross midnight
                time >= radarScreen.nightStart || time < radarScreen.nightEnd
            } else {
                time >= radarScreen.nightStart && time < radarScreen.nightEnd
            }
        }

    /** Check whether this airport has night operations available */
    @JvmStatic
    val isNightAvailable: Boolean
        get() {
            if (NIGHT_AVAILABLE.size == 0) loadArray()
            return if (TerminalControl.radarScreen == null) false else NIGHT_AVAILABLE.contains(TerminalControl.radarScreen.mainName, false)
        }
}