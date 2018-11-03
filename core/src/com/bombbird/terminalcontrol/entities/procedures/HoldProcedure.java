package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class HoldProcedure {
    private String name;
    private Airport airport;
    private Array<Waypoint> waypoints;
    private Array<int[]> altRestrictions;
    private Array<Integer> maxSpd;
    private Array<Boolean> left; //If false then direction is right
    private Array<Integer> entryProcedure;
    private Array<Integer> inboundHdg;
    private Array<Integer> legDist;

    public HoldProcedure(String name, Airport airport, String info) {
        this.name = name;
        this.airport = airport;
        waypoints = new Array<Waypoint>();
        altRestrictions = new Array<int[]>();
        maxSpd = new Array<Integer>();
        left = new Array<Boolean>();
        entryProcedure = new Array<Integer>();
        inboundHdg = new Array<Integer>();
        legDist = new Array<Integer>();

        parseInfo(info);
    }

    private void parseInfo(String info) {
        for (String indivWpt: info.split(",")) {
            //For each individual holding waypoint specified
            int index = 0;
            int[] altRest = new int[2];
            for (String wptInfo: indivWpt.split(" ")) {
                switch (index) {
                    case 0: waypoints.add(RadarScreen.waypoints.get(wptInfo)); break;
                    case 1: altRest[0] = Integer.parseInt(wptInfo); break;
                    case 2: altRest[1] = Integer.parseInt(wptInfo); break;
                    case 3: maxSpd.add(Integer.parseInt(wptInfo));
                    case 4: left.add(wptInfo.equals("LEFT")); break;
                    case 5: entryProcedure.add(Integer.parseInt(wptInfo)); break;
                    case 6: inboundHdg.add(Integer.parseInt(wptInfo)); break;
                    case 7: legDist.add(Integer.parseInt(wptInfo)); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + RadarScreen.mainName + "/hold" + airport.getIcao() + ".hold");
                }
                index++;
            }
            altRestrictions.add(altRest);
        }
    }
}
