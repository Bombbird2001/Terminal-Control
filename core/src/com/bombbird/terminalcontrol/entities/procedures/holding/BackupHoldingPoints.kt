package com.bombbird.terminalcontrol.entities.procedures.holding

import java.util.*

object BackupHoldingPoints {
    fun loadBackupPoints(icao: String, points: HashMap<String, HoldingPoints>): HashMap<String, HoldingPoints> {
        val newMap = getHashMap(icao)
        newMap.putAll(points)
        return newMap
    }

    private fun getHashMap(icao: String): HashMap<String, HoldingPoints> {
        //Load old holding waypoints before waypoint overhaul
        return when (icao) {
            "TCTP" -> {
                //TCTP
                val tctp = HashMap<String, HoldingPoints>()
                tctp["BRAVO"] = HoldingPoints("BRAVO", intArrayOf(5000, -1), 230, true, 46, 5f)
                tctp["JAMMY"] = HoldingPoints("JAMMY", intArrayOf(4000, -1), 230, true, 68, 5f)
                tctp["JUNTA"] = HoldingPoints("JUNTA", intArrayOf(4000, -1), 230, true, 54, 5f)
                tctp["AUGUR"] = HoldingPoints("AUGUR", intArrayOf(5000, -1), 230, false, 218, 5f)
                tctp["SEPIA"] = HoldingPoints("SEPIA", intArrayOf(5000, -1), 230, false, 234, 5f)
                tctp["MARCH"] = HoldingPoints("MARCH", intArrayOf(4000, -1), 230, false, 234, 5f)
                tctp
            }
            "TCSS" -> {
                //TCSS
                val tcss = HashMap<String, HoldingPoints>()
                tcss["DUPAR"] = HoldingPoints("DUPAR", intArrayOf(5000, -1), 230, true, 85, 5f)
                tcss["BESOM"] = HoldingPoints("BESOM", intArrayOf(4500, -1), 230, false, 289, 5f)
                tcss["SASHA"] = HoldingPoints("SASHA", intArrayOf(6000, -1), 230, false, 228, 5f)
                tcss["ZONLI"] = HoldingPoints("ZONLI", intArrayOf(5000, -1), 230, false, 51, 5f)
                tcss["YILAN"] = HoldingPoints("YILAN", intArrayOf(6000, -1), 230, false, 335, 5f)
                tcss["PINSI"] = HoldingPoints("PINSI", intArrayOf(5000, -1), 230, true, 299, 5f)
                tcss["KUDOS"] = HoldingPoints("KUDOS", intArrayOf(6000, -1), 230, false, 280, 5f)
                tcss["FUSIN"] = HoldingPoints("FUSIN", intArrayOf(9000, -1), 230, false, 70, 5f)
                tcss
            }
            "TCWS" -> {
                //TCWS
                val tcws = HashMap<String, HoldingPoints>()
                tcws["BOBAG"] = HoldingPoints("BOBAG", intArrayOf(6000, 18000), 220, false, 82, 5f)
                tcws["SAMKO"] = HoldingPoints("SAMKO", intArrayOf(4000, 14000), 220, true, 348, 5f)
                tcws["NYLON"] = HoldingPoints("NYLON", intArrayOf(3000, 14000), 220, true, 203, 5f)
                tcws["LAVAX"] = HoldingPoints("LAVAX", intArrayOf(7000, 14000), 220, true, 269, 5f)
                tcws["REMES"] = HoldingPoints("REMES", intArrayOf(6000, 14000), 220, false, 348, 5f)
                tcws
            }
            else -> HashMap()
        }
    }
}