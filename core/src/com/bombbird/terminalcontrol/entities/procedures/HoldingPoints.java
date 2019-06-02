package com.bombbird.terminalcontrol.entities.procedures;

import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import org.json.JSONObject;

public class HoldingPoints {
    private Waypoint waypoint;
    private int[] altRestrictions;
    private int maxSpd;
    private boolean left;
    private int inboundHdg;
    private float legDist;
    private float[] oppPoint;

    public HoldingPoints(String wpt, JSONObject jo) {
        waypoint = TerminalControl.radarScreen.waypoints.get(wpt);

    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public int[] getAltRestrictions() {
        return altRestrictions;
    }

    public int getMaxSpd() {
        return maxSpd;
    }

    public boolean isLeft() {
        return left;
    }

    public int getInboundHdg() {
        return inboundHdg;
    }

    public float getLegDist() {
        return legDist;
    }

    public float[] getOppPoint() {
        return oppPoint;
    }
}
