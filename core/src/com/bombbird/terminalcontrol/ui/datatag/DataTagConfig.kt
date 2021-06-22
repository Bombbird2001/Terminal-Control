package com.bombbird.terminalcontrol.ui.datatag

import com.badlogic.gdx.utils.Array
import org.json.JSONArray
import org.json.JSONObject

class DataTagConfig() {
    companion object {
        const val DEFAULT = "Default"
        const val COMPACT = "Compact"
        val CAN_BE_HIDDEN = HashSet<String>(listOf(DataTag.CLEARED_ALT, DataTag.LAT_CLEARED, DataTag.SIDSTAR_CLEARED, DataTag.CLEARED_IAS))
    }

    lateinit var name: String
    var onlyShowWhenChanged: HashSet<String> = HashSet()
    var arrangement: Array<Array<String>> = Array()
    var miniArrangement = Pair<Array<Array<String>>, Array<Array<String>>>(Array(3), Array(3))
    var firstEmpty = true
    var secondEmpty = true

    init {
        arrangement.add(Array(4), Array(4), Array(4), Array(4))
        miniArrangement.first.add(Array(2), Array(2), Array(2))
        miniArrangement.second.add(Array(2), Array(2), Array(2))
    }

    constructor(jsonObject: JSONObject): this() {
        val showWhenChanged = jsonObject.getJSONArray("showWhenChanged")
        for (i in 0 until showWhenChanged.length()) {
            onlyShowWhenChanged.add(showWhenChanged.getString(i))
        }

        val arrange = jsonObject.getJSONArray("arrange")
        for (i in 0 until 4) {
            val lineData = arrange.getJSONArray(i)
            for (j in 0 until 4) {
                arrangement[i].add(lineData.getString(j))
            }
        }

        val miniArrange = jsonObject.getJSONArray("miniArrange")
        val firstMini = miniArrange.getJSONArray(0)
        for (i in 0 until 3) {
            val lineData = firstMini.getJSONArray(i)
            for (j in 0 until 2) {
                miniArrangement.first[i].add(lineData.getString(j))
            }
        }

        val secondMini = miniArrange.getJSONArray(1)
        for (i in 0 until 3) {
            val lineData = secondMini.getJSONArray(i)
            for (j in 0 until 2) {
                miniArrangement.second[i].add(lineData.getString(j))
            }
        }

        firstEmpty = miniArrangement.first[0].isEmpty && miniArrangement.first[1].isEmpty && miniArrangement.first[2].isEmpty
        secondEmpty = miniArrangement.second[0].isEmpty && miniArrangement.second[1].isEmpty && miniArrangement.second[2].isEmpty
    }

    constructor(option: String): this() {
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
                firstEmpty = false
            }
            COMPACT -> {
                arrangement[0].add(DataTag.CALLSIGN, DataTag.ICAO_TYPE_WAKE)
                arrangement[1].add(DataTag.ALTITUDE_FULL)
                arrangement[2].add(DataTag.LAT_CLEARED, DataTag.SIDSTAR_CLEARED)
                arrangement[3].add(DataTag.GROUND_SPEED, DataTag.CLEARED_IAS)
                miniArrangement.first[0].add(DataTag.CALLSIGN_RECAT)
                miniArrangement.first[1].add(DataTag.ALTITUDE, DataTag.GROUND_SPEED)
                miniArrangement.second[0].add(DataTag.CALLSIGN_RECAT)
                miniArrangement.second[1].add(DataTag.CLEARED_ALT, DataTag.ICAO_TYPE)
                onlyShowWhenChanged.add(DataTag.LAT_CLEARED)
                firstEmpty = false
                secondEmpty = false
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
        val arrayToUse = if (isMinimized) {
            when {
                firstEmpty -> miniArrangement.second
                secondEmpty -> miniArrangement.first
                else -> if (System.currentTimeMillis() % 4000 >= 2000) miniArrangement.second else miniArrangement.first
            }
        } else arrangement
        val sb = StringBuilder()
        for (line in arrayToUse) {
            val sbLine = StringBuilder()
            for (field in line) {
                val text = fields[field] ?: ""
                if (text.isNotBlank() && sbLine.isNotEmpty()) sbLine.append(" ")
                if (text.isNotBlank()) sbLine.append(text)
            }
            if (sbLine.isNotBlank() && sb.isNotEmpty()) sb.append("\n")
            if (sbLine.isNotBlank()) sb.append(sbLine)
        }
        return sb.toString()
    }
}