package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class NavState {
    private Array<String> latModes;
    private String latMode;
    private Array<String> altModes;
    private String altMode;
    private Array<String> spdModes;
    private String spdMode;

    public NavState(int type, Aircraft aircraft) {
        altModes = new Array<String>(5);
        spdModes = new Array<String>(3);
        if (type == 1) {
            //Arrival
            latModes = new Array<String>(6);
            latModes.add(aircraft.getSidStar().getName() + " arrival", "After waypoint, fly heading", "Hold at", "Fly heading");
            latModes.add("Turn left heading", "Turn right heading");

            altModes.add("Descend via STAR");

            spdModes.add("STAR speed restrictions");
        } else if (type == 2) {
            //Departure
            latModes = new Array<String>(4);
            latModes.add(aircraft.getSidStar().getName() + " departure", "Fly heading", "Turn left heading", "Turn right heading");

            altModes.add("Climb via SID");

            spdModes.add("SID speed restrictions");
        } else {
            //Nani
            Gdx.app.log("Navstate type error", "Unknown navstate type specified!");
            latModes = new Array<String>(1);
        }
        altModes.add("Climb to", "Expedite climb to", "Descend to", "Expedite descend to");

        spdModes.add("No speed restrictions");

        latMode = latModes.get(0);
        altMode = altModes.get(0);
        spdMode = spdModes.get(0);
    }

    public Array<String> getLatModes() {
        return latModes;
    }

    public void setLatModes(Array<String> latModes) {
        this.latModes = latModes;
    }

    public String getLatMode() {
        return latMode;
    }

    public void setLatMode(String latMode) {
        this.latMode = latMode;
    }

    public Array<String> getAltModes() {
        return altModes;
    }

    public void setAltModes(Array<String> altModes) {
        this.altModes = altModes;
    }

    public String getAltMode() {
        return altMode;
    }

    public void setAltMode(String altMode) {
        this.altMode = altMode;
    }

    public Array<String> getSpdModes() {
        return spdModes;
    }

    public void setSpdModes(Array<String> spdModes) {
        this.spdModes = spdModes;
    }

    public String getSpdMode() {
        return spdMode;
    }

    public void setSpdMode(String spdMode) {
        this.spdMode = spdMode;
    }
}
