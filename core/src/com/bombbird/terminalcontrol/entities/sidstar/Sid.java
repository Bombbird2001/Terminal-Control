package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Sid extends SidStar {
    private HashMap<String, int[]> initClimb;
    private HashMap<String, Array<Waypoint>> initWpts;
    private HashMap<String, Array<int[]>> initRestrictions;
    private HashMap<String, Array<Boolean>> initFlyOver;
    private Array<Array<String>> transition;
    private String[] centre;

    public Sid(Airport airport, JSONObject jo) {
        super(airport, jo);
    }

    public Sid(Airport airport, JSONArray wpts, JSONArray restrictions, JSONArray fo, String name) {
        super(airport, wpts, restrictions, fo, name);

        initClimb = new HashMap<>();
        initWpts = new HashMap<>();
        initRestrictions = new HashMap<>();
        initFlyOver = new HashMap<>();
    }

    @Override
    public void parseInfo(JSONObject jo) {
        super.parseInfo(jo);

        initClimb = new HashMap<>();
        initWpts = new HashMap<>();
        initRestrictions = new HashMap<>();
        initFlyOver = new HashMap<>();
        transition = new Array<>();

        JSONObject rwys = jo.getJSONObject("rwys");
        for (String rwy: rwys.keySet()) {
            getRunways().add(rwy);
            Array<Waypoint> wpts = new Array<>();
            Array<int[]> restrictions = new Array<>();
            Array<Boolean> flyOver = new Array<>();

            JSONObject rwyObject = rwys.getJSONObject(rwy);
            String[] initClimbData = rwyObject.getString("climb").split(" ");
            JSONArray initWptData = rwyObject.getJSONArray("wpts");
            for (int i = 0; i < initWptData.length(); i++) {
                String[] data = initWptData.getString(i).split(" ");
                String wptName = data[0];
                wpts.add(TerminalControl.radarScreen.waypoints.get(wptName));
                restrictions.add(new int[] {Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])});
                boolean fo = data.length > 4 && data[4].equals("FO");
                flyOver.add(fo);
                if (fo) Waypoint.flyOverPts.put(wptName, true);
            }
            initClimb.put(rwy, new int[] {Integer.parseInt(initClimbData[0]), Integer.parseInt(initClimbData[1]), Integer.parseInt(initClimbData[2])});
            initWpts.put(rwy, wpts);
            initRestrictions.put(rwy, restrictions);
            initFlyOver.put(rwy, flyOver);
        }

        JSONArray transitions = jo.getJSONArray("transitions");
        for (int i = 0; i < transitions.length(); i++) {
            JSONArray trans = transitions.getJSONArray(i);
            Array<String> transData = new Array<>();
            for (int j = 0; j < trans.length(); j++) {
                transData.add(trans.getString(j));
            }
            transition.add(transData);
        }

        JSONArray control = jo.getJSONArray("control");
        centre = new String[2];
        centre[0] = control.getString(0);
        centre[1] = control.getString(1);
    }

    public Array<String> getRandomTransition() {
        return transition.get(MathUtils.random(0, transition.size - 1));
    }

    public int[] getInitClimb(String rwy) {
        return initClimb.get(rwy);
    }

    public String[] getCentre() {
        return centre;
    }

    public Array<Waypoint> getInitWpts(String runway) {
        return initWpts.get(runway);
    }

    public Array<int[]> getInitRestrictions(String runway) {
        return initRestrictions.get(runway);
    }

    public Array<Array<String>> getTransition() {
        return transition;
    }

    public Array<Boolean> getInitFlyOver(String runway) {
        return initFlyOver.get(runway);
    }
}
