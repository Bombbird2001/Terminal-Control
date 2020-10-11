package com.bombbird.terminalcontrol.utilities

import java.util.*

object RenameManager {
    private val icaoMap = HashMap<String, String>()
    private val reverseIcaoMap = HashMap<String, String>()

    @JvmStatic
    fun loadMaps() {
        if (icaoMap.size == 0) {
            icaoMap["RCTP"] = "TCTP"
            icaoMap["RCSS"] = "TCSS"
            icaoMap["WSSS"] = "TCWS"
            icaoMap["RJTT"] = "TCTT"
            icaoMap["RJAA"] = "TCAA"
            icaoMap["VHHH"] = "TCHH"
            icaoMap["VMMC"] = "TCMC"
            icaoMap["RJBB"] = "TCBB"
            icaoMap["RJOO"] = "TCOO"
            icaoMap["RJBE"] = "TCBE"
            icaoMap["VTBS"] = "TCBS"
            icaoMap["VTBD"] = "TCBD"
            icaoMap["LEMD"] = "TCMD"
            icaoMap["LFPG"] = "TCPG"
            icaoMap["LFPO"] = "TCPO"
            icaoMap["VHHX"] = "TCHX"
        }
        if (reverseIcaoMap.size == 0) {
            reverseIcaoMap["TCTP"] = "RCTP"
            reverseIcaoMap["TCSS"] = "RCSS"
            reverseIcaoMap["TCWS"] = "WSSS"
            reverseIcaoMap["TCTT"] = "RJTT"
            reverseIcaoMap["TCAA"] = "RJAA"
            reverseIcaoMap["TCHH"] = "VHHH"
            reverseIcaoMap["TCMC"] = "VMMC"
            reverseIcaoMap["TCBB"] = "RJBB"
            reverseIcaoMap["TCOO"] = "RJOO"
            reverseIcaoMap["TCBE"] = "RJBE"
            reverseIcaoMap["TCBS"] = "VTBS"
            reverseIcaoMap["TCBD"] = "VTBD"
            reverseIcaoMap["TCMD"] = "LEMD"
            reverseIcaoMap["TCPG"] = "LFPG"
            reverseIcaoMap["TCPO"] = "LFPO"
            reverseIcaoMap["TCHX"] = "VHHX"
        }
    }

    /** Changes old ICAO codes to new ICAO codes  */
    @JvmStatic
    fun renameAirportICAO(icao: String): String {
        icaoMap[icao]?.let {
            return it
        }
        return icao
    }

    /** Changes new ICAO to real ICAO  */
    @JvmStatic
    fun reverseNameAirportICAO(icao: String): String {
        reverseIcaoMap[icao]?.let {
            return it
        }
        return icao
    }
}