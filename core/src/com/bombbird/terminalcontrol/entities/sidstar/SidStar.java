package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import org.json.JSONArray;
import org.json.JSONObject;

public class SidStar {
    private final Airport airport;
    private String name;
    private Array<String> runways;
    private Array<Waypoint> waypoints;
    private Array<int[]> restrictions;
    private Array<Boolean> flyOver;

    private final RadarScreen radarScreen;

    private String pronunciation;

    public SidStar(Airport airport, JSONObject jo) {
        this(airport);
        parseInfo(jo);
    }

    public SidStar(Airport airport) {
        radarScreen = TerminalControl.radarScreen;
        this.airport = airport;
    }

    public SidStar(Airport airport, JSONArray wpts, JSONArray restriction, JSONArray fo, String name) {
        this(airport);

        runways = new Array<>();
        waypoints = new Array<>();
        restrictions = new Array<>();
        flyOver = new Array<>();

        pronunciation = "null";
        this.name = name;

        for (int i = 0; i < wpts.length(); i++) {
            waypoints.add(radarScreen.waypoints.get(wpts.getString(i)));
            String[] data = restriction.getString(i).split(" ");
            restrictions.add(new int[] {Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])});
            flyOver.add(fo.getBoolean(i));
        }
    }

    /** Overridden method in SID, STAR to parse relevant information */
    public void parseInfo(JSONObject jo) {
        runways = new Array<>();
        waypoints = new Array<>();
        restrictions = new Array<>();
        flyOver = new Array<>();

        pronunciation = jo.getString("pronunciation");

        JSONArray joWpts = jo.getJSONArray("route");
        for (int i = 0; i < joWpts.length(); i++) {
            String[] data = joWpts.getString(i).split(" ");
            String wptName = data[0];
            waypoints.add(radarScreen.waypoints.get(wptName));
            restrictions.add(new int[] {Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])});
            boolean fo = data.length > 4 && data[4].equals("FO");
            flyOver.add(fo);
            if (fo) Waypoint.flyOverPts.put(wptName, true);
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
