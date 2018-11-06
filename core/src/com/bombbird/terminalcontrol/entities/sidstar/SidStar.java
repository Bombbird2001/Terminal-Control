package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class SidStar {
    private Airport airport;
    private String name;
    private Array<String> runways;
    private Array<Waypoint> waypoints;
    private Array<int[]> restrictions;

    public SidStar(Airport airport, String toParse) {
        this.airport = airport;
        parseInfo(toParse);
    }

    /** Overriden method in SID, STAR to parse relevant information */
    public void parseInfo(String toParse) {
        runways = new Array<String>();
        waypoints = new Array<Waypoint>();
        restrictions = new Array<int[]>();
    }

    public void joinLines(int start, int end, int outbound, boolean dontRemove) {
        Waypoint prevPt = null;
        int index = start;
        if (!dontRemove) {
            for (Waypoint waypoint: waypoints) {
                waypoint.setSelected(false);
            }
        }
        while (index < end) {
            Waypoint waypoint = getWaypoint(index);
            waypoint.setSelected(true);
            if (prevPt != null) {
                GameScreen.shapeRenderer.line(prevPt.getPosX(), prevPt.getPosY(), waypoint.getPosX(), waypoint.getPosY());
            }
            prevPt = waypoint;
            index++;
        }
        if (prevPt != null) {
            drawOutbound(prevPt.getPosX(), prevPt.getPosY(), outbound);
        }
    }

    private void drawOutbound(float previousX, float previousY, int outbound) {
        if (outbound != -1) {
            float outboundTrack = outbound - RadarScreen.magHdgDev;
            float x = previousX + 6610 * MathUtils.cosDeg(90 - outboundTrack);
            float y = previousY + 6610 * MathUtils.sinDeg(90 - outboundTrack);
            GameScreen.shapeRenderer.line(previousX, previousY, x, y);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Array<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Array<Waypoint> getRemainingWaypoints(int start, int end) {
        //Returns array of waypoints starting from index
        Array<Waypoint> newRange = new Array<Waypoint>(waypoints);
        if (end >= start) {
            if (start > 0) {
                newRange.removeRange(0, start - 1);
            }
            int newEnd = end - start;
            if (newEnd < newRange.size - 1) {
                newRange.removeRange(newEnd + 1, newRange.size - 1);
            }
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

    public Array<int[]> getRestrictions() {
        return restrictions;
    }

    public Airport getAirport() {
        return airport;
    }
}
