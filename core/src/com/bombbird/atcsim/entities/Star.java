package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.screens.GameScreen;

public class Star {
    public String name;
    private Array<String> runways;
    private Array<Integer> inboundHdg;
    private Array<Waypoint> waypoints;
    private Array<int[]> restrictions;
    private Array<Waypoint> holdingPoints;
    private Array<int[]> holdingInfo;

    Star(String name, Array<String>runways, Array<Integer> inboundHdg, Array<Waypoint> waypoints, Array<int[]> restrictions, Array<Waypoint> holdingPoints, Array<int[]> holdingInfo) {
        this.name = name;
        this.runways = runways;
        this.inboundHdg = inboundHdg;
        this.waypoints = waypoints;
        this.restrictions = restrictions;
        this.holdingPoints = holdingPoints;
        this.holdingInfo = holdingInfo;
    }

    public void printWpts() {
        System.out.println(name);
        for (Waypoint waypoint: waypoints) {
            System.out.println(waypoint.name);
        }
    }

    public void joinLines(int start) {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        float previousX = -1;
        float previousY = -1;
        int index = 0;
        for (Waypoint waypoint: waypoints) {
            if (index >= start) {
                waypoint.setSelected(true);
                if (previousX != -1 && previousY != -1)
                    GameScreen.shapeRenderer.line(previousX, previousY, waypoint.x, waypoint.y);
                previousX = waypoint.x;
                previousY = waypoint.y;
            }
            index++;
        }
    }

    public Array<String> getRunways() {
        return runways;
    }

    public int getInboundHdg() {
        if (inboundHdg.size == 1) {
            return inboundHdg.get(0);
        } else {
            return MathUtils.random(inboundHdg.get(0), inboundHdg.get(1));
        }
    }

    public Waypoint getWaypoint(int index) {
        return waypoints.get(index);
    }
}
