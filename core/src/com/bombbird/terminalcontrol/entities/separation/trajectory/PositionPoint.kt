package com.bombbird.terminalcontrol.entities.separation.trajectory;

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import org.json.JSONObject;

public class PositionPoint {
    private Aircraft aircraft;
    public float x;
    public float y;
    public int altitude;

    public PositionPoint(Aircraft aircraft, float x, float y, int altitude) {
        this.aircraft = aircraft;
        this.x = x;
        this.y = y;
        this.altitude = altitude;
    }

    public PositionPoint(JSONObject save) {
        //Don't need to save aircraft (not needed for wake turbulence points, trajectory points are not saved)
        x = (float) save.getDouble("x");
        y = (float) save.getDouble("y");
        altitude = save.getInt("altitude");
    }

    public Aircraft getAircraft() {
        return aircraft;
    }
}
