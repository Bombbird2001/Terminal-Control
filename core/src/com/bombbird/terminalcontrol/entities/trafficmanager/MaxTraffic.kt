package com.bombbird.terminalcontrol.entities.trafficmanager

import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager.isNight
import java.util.HashMap

object MaxTraffic {
    private val maxTraffic = HashMap<String, Float>()
    private val nightMaxTraffic = HashMap<String, Float>()
    @JvmStatic
    fun loadHashmaps() {
        maxTraffic["TCTP"] = 20f
        maxTraffic["TCWS"] = 16f
        maxTraffic["TCTT"] = 26f
        maxTraffic["TCBB"] = 18f
        maxTraffic["TCHH"] = 20f
        maxTraffic["TCBD"] = 20f
        maxTraffic["TCMD"] = 22f
        maxTraffic["TCPG"] = 24f
        maxTraffic["TCHX"] = 10f
        nightMaxTraffic["TCTT"] = 6f
        nightMaxTraffic["TCBB"] = 8f
        nightMaxTraffic["TCHH"] = 14f
        nightMaxTraffic["TCMD"] = 14f
    }

    @JvmStatic
    fun getMaxTraffic(icao: String): Float {
        return if (isNight && nightMaxTraffic.containsKey(icao)) nightMaxTraffic[icao]!! else maxTraffic[icao]!!
    }
}