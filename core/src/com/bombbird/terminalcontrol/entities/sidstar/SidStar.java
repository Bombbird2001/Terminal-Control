package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;

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
        //Overriden method for SID
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

    public int findWptIndex(String wptName) {
        return waypoints.indexOf(RadarScreen.waypoints.get(wptName), false);
    }

    public int getWptMinAlt(String wptName) {
        return restrictions.get(findWptIndex(wptName))[0];
    }

    public int getWptMaxAlt(String wptName) {
        return restrictions.get(findWptIndex(wptName))[1];
    }

    public int getWptMaxSpd(String wptName) {
        return restrictions.get(findWptIndex(wptName))[2];
    }
}
