package com.bombbird.atcsim.entities.sidstar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.screens.GameScreen;

public class SidStar {
    public String name;
    Array<String> runways;
    Array<Waypoint> waypoints;
    Array<int[]> restrictions;

    SidStar(String name, Array<String> runways, Array<Waypoint> waypoints, Array<int[]> restrictions) {
        this.name = name;
        this.runways = runways;
        this.waypoints = waypoints;
        this.restrictions = restrictions;
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

    public Waypoint getWaypoint(int index) {
        if (index >= waypoints.size) {
            return null;
        }
        return waypoints.get(index);
    }
}
