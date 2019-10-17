package com.bombbird.terminalcontrol.entities.waketurbulence;

import org.json.JSONObject;

public class WakePoint {
    public float x;
    public float y;
    public int altitude;

    public WakePoint(float x, float y, int altitude) {
        this.x = x;
        this.y = y;
        this.altitude = altitude;
    }

    public WakePoint(JSONObject save) {
        //TODO Load point info from save
    }
}
