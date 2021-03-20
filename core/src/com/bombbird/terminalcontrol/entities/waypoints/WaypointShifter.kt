package com.bombbird.terminalcontrol.entities.waypoints

import java.util.*

object WaypointShifter {

    val movementData = HashMap<String, HashMap<String, IntArray>>()


    fun loadData() {
        if (movementData.size > 0) return
        //TCTP
        putData("TCTP", -48, 0, "RAKAN", "SOLEE", "CRANK", "MILEY", "OMORI", "AUBRY")
        putData("TCTP", 64, -96, "HOKOU", "PILUT", "SLASH", "KINDY", "HERRO", "KEELL")
        //TCWS
        putData("TCWS", 80, -72, "GOBIS", "VAMOV", "VULUV", "SOPMI")
        //TCTT
        putData("TCTT", 32, 0, "LOBBI", "ABSON")
        putData("TCTT", 20, 0, "SCODI")
        putData("TCTT", 0, -96, "BOMDA", "BALAD", "ADMON")
        putData("TCTT", 16, 0, "COCOA", "MELAC", "COALL", "ROOLY", "COSTY")
        putData("TCTT", -64, -64, "GHOUL", "RALON")
        putData("TCTT", -48, -64, "CRICK", "NOMIN")
        putData("TCTT", 48, 0, "CLOWN", "SMITE", "EMMMA")
        putData("TCTT", -88, -64, "RAZOR")
        putData("TCTT", -80, -80, "VALON", "LALLI")
        //TCHH
        putData("TCHH", 20, 0, "MUMMY", "CLIPP")
        //TCBB
        putData("TCBB", -20, 0, "BANDE")
        putData("TCBB", 20, 0, "CLIPS", "BAYNO")
        putData("TCBB", 96, -24, "PONCH")
        putData("TCBB", 64, -112, "ERIGE", "GROND")
        //TCBD
        putData("TCBD", 0, -96, "LANNI")
        putData("TCBD", 32, 0, "DIVER")
        //TCMD
        putData("TCMD", 16, -96, "DPT")
        putData("TCMD", -20, 0, "TENSI")
        putData("TCMD", 88, -64, "ABSIT", "GLADI")
        putData("TCMD", 20, 0, "VERDE")
        putData("TCMD", -88, -64, "EMDOT", "PEGAS")
        //TCPG - No adjustments needed
        //TCHX
        putData("TCHX", -8, 0, "CS")
        putData("TCHX", 8, 0, "WR")
    }

    private fun putData(icao: String, xShift: Int, yShift: Int, vararg wpts: String) {
        if (!movementData.containsKey(icao)) movementData[icao] = HashMap()
        for (wpt in wpts) {
            movementData[icao]?.set(wpt, intArrayOf(xShift, yShift))
        }
    }
}