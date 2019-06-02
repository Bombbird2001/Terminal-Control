package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import org.json.JSONArray;
import org.json.JSONObject;

public class SidStar {
    private Airport airport;
    private String name;
    private Array<String> runways;
    private Array<Waypoint> waypoints;
    private Array<int[]> restrictions;
    private Array<Boolean> flyOver;

    private RadarScreen radarScreen;

    private String pronunciation;

    public SidStar(Airport airport, JSONObject jo) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        parseInfo(jo);
    }

    /** Overriden method in SID, STAR to parse relevant information */
    public void parseInfo(JSONObject jo) {
        runways = new Array<String>();
        waypoints = new Array<Waypoint>();
        restrictions = new Array<int[]>();
        flyOver = new Array<Boolean>();

        setPronunciation(jo.getString("pronunciation"));

        JSONArray joWpts = jo.getJSONArray("route");
        for (int i = 0; i < joWpts.length(); i++) {
            String[] data = joWpts.getString(i).split(" ");
            waypoints.add(radarScreen.waypoints.get(data[0]));
            restrictions.add(new int[] {Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])});
            flyOver.add(data.length > 4 && data[4].equals("FO"));
        }
    }

    public Array<String> getRunways() {
        return runways;
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

    public Array<int[]> getRestrictions() {
        return restrictions;
    }

    public Airport getAirport() {
        return airport;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public Array<Boolean> getFlyOver() {
        return flyOver;
    }
}
