package com.bombbird.terminalcontrol.entities.separation.trajectory

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import org.json.JSONObject

class PositionPoint {
    var aircraft: Aircraft? = null
        private set
    var x: Float
    var y: Float
    var altitude: Int

    constructor(aircraft: Aircraft, x: Float, y: Float, altitude: Int) {
        this.aircraft = aircraft
        this.x = x
        this.y = y
        this.altitude = altitude
    }

    constructor(save: JSONObject) {
        //Don't need to save aircraft (not needed for wake turbulence points, trajectory points are not saved)
        aircraft = null
        x = save.getDouble("x").toFloat()
        y = save.getDouble("y").toFloat()
        altitude = save.getInt("altitude")
    }
}