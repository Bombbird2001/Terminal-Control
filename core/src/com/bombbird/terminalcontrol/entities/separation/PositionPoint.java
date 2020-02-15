package com.bombbird.terminalcontrol.entities.separation;

import org.json.JSONObject;

public class PositionPoint {
    public float x;
    public float y;
    public int altitude;

    public PositionPoint(float x, float y, int altitude) {
        this.x = x;
        this.y = y;
        this.altitude = altitude;
    }

    public PositionPoint(JSONObject save) {
        x = (float) save.getDouble("x");
        y = (float) save.getDouble("y");
        altitude = save.getInt("altitude");
    }
}
