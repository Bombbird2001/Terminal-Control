package com.bombbird.terminalcontrol.utilities

import com.bombbird.terminalcontrol.TerminalControl
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

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
            expiry.add(Calendar.SECOND, hours) //TODO change
            return "${expiry.get(Calendar.YEAR)}-${expiry.get(Calendar.MONTH)}-${expiry.get(Calendar.DATE)}-${expiry.get(Calendar.HOUR_OF_DAY)}-${expiry.get(Calendar.MINUTE)}"
        }
    }
}