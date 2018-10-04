package com.bombbird.atcsim.entities.aircrafts;

public class NavState {
    private String[] latModes;
    private String latMode;
    private String[] altModes;
    private String altMode;
    private String[] spdModes;
    private String spdMode;

    NavState(int type, Aircraft aircraft) {
        altModes = new String[5];
        spdModes = new String[3];
        if (type == 1) {
            //Arrival
            latModes = new String[6];
            latModes[0] = aircraft.getSidStar().getName() + " arrival";
            latModes[1] = "After waypoint, fly heading";
            latModes[2] = "Hold at";
            latModes[3] = "Fly heading";
            latModes[4] = "Turn left heading";
            latModes[5] = "Turn right heading";

            altModes[0] = "Descend via STAR";

            spdModes[0] = "STAR speed restrictions";
        } else if (type == 2) {
            //Departure
            latModes = new String[4];
            latModes[0] = aircraft.getSidStar().getName() + " departure";
            latModes[1] = "Fly heading";
            latModes[2] = "Turn left heading";
            latModes[3] = "Turn right heading";

            altModes[0] = "Climb via SID";

            spdModes[0] = "SID speed restrictions";
        }
        altModes[1] = "Climb to";
        altModes[2] = "Expedite climb to";
        altModes[3] = "Descend to";
        altModes[4] = "Expedite descend to";

        spdModes[1] = "No speed restrictions";

        latMode = latModes[0];
        altMode = altModes[0];
        spdMode = spdModes[0];
    }

    public String[] getLatModes() {
        return latModes;
    }

    public void setLatModes(String[] latModes) {
        this.latModes = latModes;
    }

    public String getLatMode() {
        return latMode;
    }

    public void setLatMode(String latMode) {
        this.latMode = latMode;
    }

    public String[] getAltModes() {
        return altModes;
    }

    public void setAltModes(String[] altModes) {
        this.altModes = altModes;
    }

    public String getAltMode() {
        return altMode;
    }

    public void setAltMode(String altMode) {
        this.altMode = altMode;
    }

    public String[] getSpdModes() {
        return spdModes;
    }

    public void setSpdModes(String[] spdModes) {
        this.spdModes = spdModes;
    }

    public String getSpdMode() {
        return spdMode;
    }

    public void setSpdMode(String spdMode) {
        this.spdMode = spdMode;
    }
}
