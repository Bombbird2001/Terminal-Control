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
        putData("TCWS", 48, -64, "RAYLU", "BELBY", "MINEL", "PLANE", "NELON", "NASIL")
        putData("TCWS", -32, 0, "AGILE", "VIDAS", "RADIO", "MENNU", "KAYPO", "KANDI")
        //TCTT
        putData("TCTT", 32, 0, "LOBBI", "ABSON")
        putData("TCTT", 20, 0, "SCODI")
        putData("TCTT", 0, -128, "BOMDA", "BALAD", "ADMON", "DOLLY")
        putData("TCTT", 16, 0, "COCOA", "MELAC", "COALL", "ROOLY", "COSTY")
        putData("TCTT", -64, -64, "GHOUL", "RALON")
        putData("TCTT", -48, -64, "CRICK", "NOMIN")
        putData("TCTT", 48, 0, "CLOWN", "SMITE", "EMMMA", "MALTY", "BRIDY")
        putData("TCTT", -88, -64, "RAZOR")
        putData("TCTT", -80, -80, "VALON", "LALLI")
        putData("TCTT", -64, -64, "CONAN", "PEACH", "AGNII", "SMOKE")
        //TCHH
        putData("TCHH", 20, 0, "MUMMY", "CLIPP", "GIVEN")
        putData("TCHH", -32, 0, "IONIC")
        putData("TCHH", 80, -72, "TESLA")
        putData("TCHH", 32, -120, "HOOPP", "LITES")
        putData("TCHH", -80, -16, "ITRF14.1")
        putData("TCHH", 8, -120, "SOLUT")
        putData("TCHH", -72, -120, "AIYOR")
        //TCBB
        putData("TCBB", -20, 0, "BANDE")
        putData("TCBB", 20, 0, "CLIPS", "BAYNO")
        putData("TCBB", 96, -24, "PONCH")
        putData("TCBB", 64, -112, "ERIGE", "GROND")
        putData("TCBB", 56, -100, "RAMBE", "GAFET")
        //TCBD
        putData("TCBD", 0, -96, "LANNI")
        putData("TCBD", 32, 0, "DIVER")
        putData("TCBD", 48, -64, "SOMEL", "SILLY", "CLANO", "SAUCE", "VANNY", "PAYRA", "SIRRE")
        putData("TCBD", -32, 0, "CROWE", "YARUN", "MORRE", "DEPTH", "RYUKA", "NOLLY", "CALOS")
        //TCMD
        putData("TCMD", 16, -96, "DPT")
        putData("TCMD", -20, 0, "TENSI")
        putData("TCMD", 88, -64, "ABSIT", "GLADI")
        putData("TCMD", 20, 0, "VERDE")
        putData("TCMD", -88, -64, "EMDOT", "PEGAS")
        putData("TCMD", -72, -64, "LUCAS")
        //TCPG
        putData("TCPG", 0, -120, "ANDDO", "SUKON")
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