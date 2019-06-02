package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import org.json.JSONObject;

public class Route {
    private Array<Waypoint> wpts;
    private Array<int[]> restrictions;

    public Route() {

    }

    public Route(JSONObject jo) {
        //TODO Load route
    }
}
