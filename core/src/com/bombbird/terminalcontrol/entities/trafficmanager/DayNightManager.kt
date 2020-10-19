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
            TerminalControl.radarScreen?.let {
                if (!isNightAvailable || !it.allowNight) return false
                val calendar = Calendar.getInstance(TimeZone.getDefault())
                val additional = if (calendar[Calendar.AM_PM] == Calendar.PM) 12 else 0
                val time = (calendar[Calendar.HOUR] + additional) * 100 + calendar[Calendar.MINUTE]
                return if (it.nightEnd <= it.nightStart) {
                    //Cross midnight
                    time >= it.nightStart || time < it.nightEnd
                } else {
                    time >= it.nightStart && time < it.nightEnd
                }
            }
            return false
        }

    /** Check whether this airport has night operations available */
    @JvmStatic
    val isNightAvailable: Boolean
        get() {
            if (NIGHT_AVAILABLE.size == 0) loadArray()
            return TerminalControl.radarScreen?.let { NIGHT_AVAILABLE.contains(it.mainName, false) } ?: false
        }
}