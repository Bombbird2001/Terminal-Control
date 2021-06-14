package com.bombbird.terminalcontrol.ui.datatag

import com.badlogic.gdx.utils.Array
import org.json.JSONArray

class DataTagConfig {
    companion object {
        const val DEFAULT = 0
        const val COMPACT = 1
    }

    private lateinit var onlyShowWhenChanged: HashSet<String>
    private var arrangement = Array<Array<String>>(4)
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

    constructor(option: Int) {
        arrangement[0].add(DataTag.CALLSIGN, DataTag.ICAO_TYPE_WAKE)
        arrangement[1].add(DataTag.ALTITUDE_FULL)
        arrangement[3].add(DataTag.GROUND_SPEED, DataTag.CLEARED_IAS)
        miniArrangement.first[0].add(DataTag.CALLSIGN_RECAT)
        if (option == DEFAULT) {
            arrangement[2].add(DataTag.HEADING, DataTag.LAT_CLEARED, DataTag.SIDSTAR_CLEARED)
            miniArrangement.first[1].add(DataTag.ALTITUDE, DataTag.HEADING)
            miniArrangement.first[2].add(DataTag.GROUND_SPEED)
        } else if (option == COMPACT) {
            arrangement[2].add(DataTag.SIDSTAR_CLEARED, DataTag.SIDSTAR_CLEARED)
            miniArrangement.first[1].add(DataTag.ALTITUDE, DataTag.GROUND_SPEED)
            miniArrangement.second[0].add(DataTag.CALLSIGN_RECAT)
            miniArrangement.second[1].add(DataTag.CLEARED_ALT, DataTag.ICAO_TYPE)
            onlyShowWhenChanged.add(DataTag.LAT_CLEARED)
        }
    }
}