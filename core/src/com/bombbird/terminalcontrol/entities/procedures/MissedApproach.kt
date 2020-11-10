package com.bombbird.terminalcontrol.entities.procedures

import org.json.JSONObject

class MissedApproach(jo: JSONObject) {
    val climbAlt: Int = jo.getInt("altitude")
    val climbSpd: Int = jo.getInt("speed")
}