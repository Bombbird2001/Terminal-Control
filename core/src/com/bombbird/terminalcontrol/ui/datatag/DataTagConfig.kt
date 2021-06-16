package com.bombbird.terminalcontrol.ui.datatag

import com.badlogic.gdx.utils.Array
import org.json.JSONArray

class DataTagConfig {
    companion object {
        const val DEFAULT = "Default"
        const val COMPACT = "Compact"
    }

    lateinit var name: String
    private var onlyShowWhenChanged: HashSet<String> = HashSet()
    private var arrangement: Array<Array<String>> = Array()
    private var miniArrangement = Pair<Array<Array<String>>, Array<Array<String>>>(Array(3), Array(3))

    init {
        arrangement.add(Array(), Array(), Array(), Array())
        miniArrangement.first.add(Array(), Array(), Array())
        miniArrangement.second.add(Array(), Array(), Array())
    }

    constructor(showWhenChanged: JSONArray, arrange: JSONArray) {
        for (i in 0 until showWhenChanged.length()) {
            onlyShowWhenChanged.add(showWhenChanged.getString(i))
        }

        for (i in 0 until arrange.length()) {
            if (i > 3) break
            val lineData = arrange.getJSONArray(i)
            for (j in 0 until lineData.length()) {
                arrangement[i].add(lineData.getString(j))
            }

        }
    }

    constructor(option: String) {
        name = option
        when (option) {
            DEFAULT -> {
                arrangement[0].add(DataTag.CALLSIGN, DataTag.ICAO_TYPE_WAKE)
                arrangement[1].add(DataTag.ALTITUDE_FULL)
                arrangement[2].add(DataTag.HEADING, DataTag.LAT_CLEARED, DataTag.SIDSTAR_CLEARED)
                arrangement[3].add(DataTag.GROUND_SPEED, DataTag.CLEARED_IAS)
                miniArrangement.first[0].add(DataTag.CALLSIGN_RECAT)
                miniArrangement.first[1].add(DataTag.ALTITUDE, DataTag.HEADING)
                miniArrangement.first[2].add(DataTag.GROUND_SPEED)
            }
            COMPACT -> {
                arrangement[0].add(DataTag.CALLSIGN, DataTag.ICAO_TYPE_WAKE)
                arrangement[1].add(DataTag.ALTITUDE_FULL)
                arrangement[2].add(DataTag.SIDSTAR_CLEARED, DataTag.SIDSTAR_CLEARED)
                arrangement[3].add(DataTag.GROUND_SPEED, DataTag.CLEARED_IAS)
                miniArrangement.first[0].add(DataTag.CALLSIGN_RECAT)
                miniArrangement.first[1].add(DataTag.ALTITUDE, DataTag.GROUND_SPEED)
                miniArrangement.second[0].add(DataTag.CALLSIGN_RECAT)
                miniArrangement.second[1].add(DataTag.CLEARED_ALT, DataTag.ICAO_TYPE)
                onlyShowWhenChanged.add(DataTag.LAT_CLEARED)
            }
            else -> {
                //TODO Load datatag json file with config
            }
        }
    }

    fun showOnlyWhenChanged(field: String): Boolean {
        return onlyShowWhenChanged.contains(field)
    }

    fun generateTagText(fields: HashMap<String, String>, isMinimized: Boolean): String {
        val arrayToUse = if (isMinimized) (if (System.currentTimeMillis() % 4000 >= 2000) miniArrangement.second else miniArrangement.first) else arrangement
        val sb = StringBuilder()
        for (line in arrayToUse) {
            val sbLine = StringBuilder()
            for (field in line) {
                if (sbLine.isNotEmpty()) sbLine.append(" ")
                val text = fields[field] ?: ""
                if (text.isNotBlank()) sbLine.append(text)
            }
            if (sb.isNotEmpty()) sb.append("\n")
            if (sbLine.isNotBlank()) sb.append(sbLine)
        }
        return sb.toString()
    }
}