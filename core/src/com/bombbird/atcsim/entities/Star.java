package com.bombbird.atcsim.entities;

import com.badlogic.gdx.utils.Array;

public class Star {
    private String name;
    private int inboundTrack;
    private Array<Waypoint> waypoints;
    private Array<int[]> restrictions;
    private Array<Waypoint> holdingPoints;
    private Array<int[]> holdingInfo;

    Star(String name, int inboundTrack, Array<Waypoint> waypoints, Array<int[]> restrictions, Array<Waypoint> holdingPoints, Array<int[]> holdingInfo) {
        this.name = name;
        this.inboundTrack = inboundTrack;
        this.waypoints = waypoints;
        this.restrictions = restrictions;
        this.holdingPoints = holdingPoints;
        this.holdingInfo = holdingInfo;
    }

    public void printWpts() {
        for (Waypoint waypoint: waypoints) {
            System.out.println(waypoint.name);
        }
    }
}
