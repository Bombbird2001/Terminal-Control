package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;

import java.util.HashMap;

public class HoldProcedure {
    private HashMap<String, HoldingPoints> holdingPoints;

    public HoldProcedure() {
        holdingPoints = new HashMap<String, HoldingPoints>();
    }

    public HoldProcedure(Star star) {
        this();

        Array<Waypoint> waypoints = star.getWaypoints();
        for (int i = 0; i < waypoints.size; i++) {
            String wptName = waypoints.get(i).getName();
            HashMap<String, HoldingPoints> holdingPoint = star.getAirport().getHoldingPoints();
            if (holdingPoint.containsKey(wptName)) {
                holdingPoints.put(wptName, holdingPoint.get(wptName));
            }
        }
    }

    public int getEntryProcAtWpt(Waypoint waypoint, double heading) {
        return holdingPoints.get(waypoint.getName()).getEntryProc(heading);
    }

    public void renderShape(Waypoint waypoint) {
        holdingPoints.get(waypoint.getName()).renderShape();
    }

    public int getMaxSpdAtWpt(Waypoint waypoint) {
        return holdingPoints.get(waypoint.getName()).getMaxSpd();
    }

    public int getInboundHdgAtWpt(Waypoint waypoint) {
        return holdingPoints.get(waypoint.getName()).getInboundHdg();
    }

    public float getLegDistAtWpt(Waypoint waypoint) {
        return holdingPoints.get(waypoint.getName()).getLegDist();
    }

    public float[] getOppPtAtWpt(Waypoint waypoint) {
        return holdingPoints.get(waypoint.getName()).getOppPoint();
    }

    public boolean isLeftAtWpt(Waypoint waypoint) {
        return holdingPoints.get(waypoint.getName()).isLeft();
    }

    public int[] getAltRestAtWpt(Waypoint waypoint) {
        return holdingPoints.get(waypoint.getName()).getAltRestrictions();
    }

    public HashMap<String, HoldingPoints> getHoldingPoints() {
        return holdingPoints;
    }
}
