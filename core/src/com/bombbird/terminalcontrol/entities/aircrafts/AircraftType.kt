package com.bombbird.terminalcontrol.entities.aircrafts

import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadAircraftData

object AircraftType {
    private val aircraftTypes = loadAircraftData()
    fun getWakeCat(type: String): Char {
        return if (!aircraftTypes.containsKey(type)) 'M' else aircraftTypes[type]!![0].toChar()
    }

    @JvmStatic
    fun getRecat(type: String): Char {
        return if (!aircraftTypes.containsKey(type)) 'D' else aircraftTypes[type]!![5].toChar()
    }

    fun getV2(type: String): Int {
        return if (!aircraftTypes.containsKey(type)) 150 else aircraftTypes[type]!![1]
    }

    fun getTypClimb(type: String): Int {
        return if (!aircraftTypes.containsKey(type)) 2000 else aircraftTypes[type]!![2]
    }

    fun getTypDes(type: String): Int {
        return if (!aircraftTypes.containsKey(type)) 2000 else aircraftTypes[type]!![3]
    }

    fun getApchSpd(type: String): Int {
        return if (!aircraftTypes.containsKey(type)) 150 else aircraftTypes[type]!![4]
    }

    fun getMaxCruiseSpd(type: String): Int {
        return if (!aircraftTypes.containsKey(type)) -1 else aircraftTypes[type]!![6]
    }

    fun getMaxCruiseAlt(type: String): Int {
        return if (!aircraftTypes.containsKey(type)) 43000 else aircraftTypes[type]!![7]
    }
}