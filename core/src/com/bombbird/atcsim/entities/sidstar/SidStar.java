package com.bombbird.atcsim.entities.sidstar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.screens.GameScreen;

public class SidStar {
    private String name;
    private Array<String> runways;
    private Array<Waypoint> waypoints;
    private Array<int[]> restrictions;

    public SidStar(String name, Array<String> runways, Array<Waypoint> waypoints, Array<int[]> restrictions) {
        this.name = name;
        this.runways = runways;
        this.waypoints = waypoints;
        this.restrictions = restrictions;
    }

    public void printWpts() {
        System.out.println(name);
        for (Waypoint waypoint: waypoints) {
            System.out.println(waypoint.getName());
        }
    }

    public void joinLines(int start, int outbound) {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        float previousX = -1;
        float previousY = -1;
        int index = 0;
        for (Waypoint waypoint: waypoints) {
            if (index >= start) {
                waypoint.setSelected(true);
                if (previousX != -1 && previousY != -1) {
                    GameScreen.shapeRenderer.line(previousX, previousY, waypoint.getPosX(), waypoint.getPosY());
                }
                previousX = waypoint.getPosX();
                previousY = waypoint.getPosY();
            }
            index++;
        }
        drawOutbound(previousX, previousY, outbound);
    }

    public void drawOutbound(float previousX, float previousY, int outbound) {

    }

    public Array<String> getRunways() {
        return runways;
    }

    public Waypoint getWaypoint(int index) {
        if (index >= waypoints.size) {
            return null;
        }
        return waypoints.get(index);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Array<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Array<Waypoint> getRemainingWaypoints(int index) {
        //Returns array of waypoints starting from index
        Array<Waypoint> newRange = new Array<Waypoint>(waypoints);
        if (index > 0) {
            newRange.removeRange(0, index - 1);
        }
        return newRange;
    }

    public Array<int[]> getRestrictions() {
        //Returns array of altitude, speed restrictions
        return restrictions;
    }

    public int findWptIndex(Waypoint waypoint) {
        return waypoints.indexOf(waypoint, false);
    }
}
