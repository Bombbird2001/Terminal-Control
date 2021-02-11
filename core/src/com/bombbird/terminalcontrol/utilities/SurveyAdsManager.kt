package com.bombbird.terminalcontrol.utilities

import com.bombbird.terminalcontrol.TerminalControl
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.ceil
import kotlin.math.roundToInt

class SurveyAdsManager {
    companion object {
        val unlockableAirports = HashSet<String>()
        val airportTimings = HashMap<String, String>()

        fun loadData() {
            airportTimings.putAll(TerminalControl.playServicesInterface.getAirportRewardTiming())

            if (unlockableAirports.isNotEmpty()) return
            unlockableAirports.add("TCTT")
            unlockableAirports.add("TCHH")
            unlockableAirports.add("TCBB")
            unlockableAirports.add("TCBD")
            unlockableAirports.add("TCMD")
            unlockableAirports.add("TCPG")
        }

        fun getExpiryDateTime(hours: Int): String {
            val expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            expiry.add(Calendar.MINUTE, 4) //TODO change
            return "${expiry.get(Calendar.YEAR)}-${expiry.get(Calendar.MONTH)}-${expiry.get(Calendar.DATE)}-${expiry.get(Calendar.HOUR_OF_DAY)}-${expiry.get(Calendar.MINUTE)}"
        }

        /** Checks the remaining time in minutes and returns it as an integer rounded up, returns true if expired or entry is missing from hashMap, else false */
        fun remainingTime(airport: String): Int {
            airportTimings[airport]?.let {
                val dateData = it.split("-").map { it2 -> it2.toInt() }
                val expiryDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                expiryDate.clear()
                expiryDate.set(dateData[0], dateData[1], dateData[2], dateData[3], dateData[4])
                return ceil((expiryDate.timeInMillis - Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis) / 60000f).roundToInt()
            } ?: return -1
        }
    }
}