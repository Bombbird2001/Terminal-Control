package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Star extends SidStar {
    private Array<Array<String>> inbound;
    private HashMap<String, Array<Waypoint>> rwyWpts;
    private HashMap<String, Array<int[]>> rwyRestrictions;
    private HashMap<String, Array<Boolean>> rwyFlyOver;

    public Star(Airport airport, JSONObject jo) {
        super(airport, jo);
    }

    public Star(Airport airport, JSONArray wpts, JSONArray restrictions, JSONArray fo, String name) {
        super(airport, wpts, restrictions, fo, name);

        inbound = new Array<>();
        rwyWpts = new HashMap<>();
        rwyRestrictions = new HashMap<>();
        rwyFlyOver = new HashMap<>();

        Array<String> inbound1 = new Array<>();
        inbound1.add("HDG 360");
        inbound.add(inbound1);
    }

    @Override
    public void parseInfo(JSONObject jo) {
        super.parseInfo(jo);

        inbound = new Array<>();
        rwyWpts = new HashMap<>();
        rwyRestrictions = new HashMap<>();
        rwyFlyOver = new HashMap<>();

        JSONObject rwys = jo.getJSONObject("rwys");
        for (String rwy: rwys.keySet()) {
            getRunways().add(rwy);
            Array<Waypoint> wpts = new Array<>();
            Array<int[]> restrictions = new Array<>();
            Array<Boolean> flyOver = new Array<>();

            JSONArray rwyObject = rwys.getJSONArray(rwy);
            for (int i = 0; i < rwyObject.length(); i++) {
                String[] data = rwyObject.getString(i).split(" ");
                String wptName = data[0];
                wpts.add(TerminalControl.radarScreen.waypoints.get(wptName));
                restrictions.add(new int[] {Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])});
                boolean fo = data.length > 4 && data[4].equals("FO");
                flyOver.add(fo);
                if (fo) Waypoint.flyOverPts.put(wptName, true);
            }
            rwyWpts.put(rwy, wpts);
            rwyRestrictions.put(rwy, restrictions);
            rwyFlyOver.put(rwy, flyOver);
        }

        JSONArray inbounds = jo.getJSONArray("inbound");
        for (int i = 0; i < inbounds.length(); i++) {
            JSONArray trans = inbounds.getJSONArray(i);
            Array<String> transData = new Array<>();
            for (int j = 0; j < trans.length(); j++) {
                transData.add(trans.getString(j));
            }
            inbound.add(transData);
        }
    }

    public Array<String> getAllInboundWpt() {
        Array<String> wpts = new Array<>();
        for (int i = 0; i < inbound.size; i++) {
            Array<String> inboundPts = inbound.get(i);
            if (inboundPts.size > 1) {
                wpts.add(inboundPts.get(1).split(" ")[1]);
            }
        }
        wpts.add(getWaypoints().get(0).getName());

        return wpts;
    }

    public Array<String> getRandomInbound() {
        return inbound.get(MathUtils.random(inbound.size - 1));
    }

    public Array<Waypoint> getRwyWpts(String runway) {
        return rwyWpts.get(runway);
    }

    public Array<int[]> getRwyRestrictions(String runway) {
        return rwyRestrictions.get(runway);
    }

    public Array<Boolean> getRwyFlyOver(String runway) {
        return rwyFlyOver.get(runway);
    }
}