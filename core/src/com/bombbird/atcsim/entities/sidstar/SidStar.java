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

    SidStar(String name, Array<String> runways, Array<Waypoint> waypoints, Array<int[]> restrictions) {
        this.setName(name);
        this.setRunways(runways);
        this.setWaypoints(waypoints);
        this.setRestrictions(restrictions);
    }

    public void printWpts() {
        System.out.println(name);
        for (Waypoint waypoint: getWaypoints()) {
            System.out.println(waypoint.getName());
        }
    }

    public void joinLines(int start, int outbound) {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        float previousX = -1;
        float previousY = -1;
        int index = 0;
        for (Waypoint waypoint: getWaypoints()) {
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

    void drawOutbound(float previousX, float previousY, int outbound) {

    }

    public Array<String> getRunways() {
        return runways;
    }

    public Waypoint getWaypoint(int index) {
        if (index >= getWaypoints().size) {
            return null;
        }
        return getWaypoints().get(index);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRunways(Array<String> runways) {
        this.runways = runways;
    }

    public Array<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(Array<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public Array<int[]> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Array<int[]> restrictions) {
        this.restrictions = restrictions;
    }
}
