package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class SidStar {
    private Airport airport;
    private String name;
    private Array<String> runways;
    private Array<Waypoint> waypoints;
    private Array<int[]> restrictions;

    private RadarScreen radarScreen;

    public SidStar(Airport airport, String toParse) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        parseInfo(toParse);
    }

    /** Overriden method in SID, STAR to parse relevant information */
    public void parseInfo(String toParse) {
        runways = new Array<String>();
        waypoints = new Array<Waypoint>();
        restrictions = new Array<int[]>();
    }

    public void joinLines(int start, int end, int outbound) {
        Waypoint prevPt = null;
        int index = start;
        while (index < end) {
            Waypoint waypoint = getWaypoint(index);
            if (prevPt != null) {
                radarScreen.shapeRenderer.line(prevPt.getPosX(), prevPt.getPosY(), waypoint.getPosX(), waypoint.getPosY());
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
            float outboundTrack = outbound - radarScreen.magHdgDev;
            float[] point = MathTools.pointsAtBorder(new float[] {1260, 4500}, new float[] {0, 3240}, previousX, previousY, outboundTrack);
            radarScreen.shapeRenderer.line(previousX, previousY, point[0], point[1]);
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
        return waypoints.indexOf(radarScreen.waypoints.get(wptName), false);
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
