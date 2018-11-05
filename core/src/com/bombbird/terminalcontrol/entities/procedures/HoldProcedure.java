package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class HoldProcedure {
    //Constant turn diameter
    private static final float turnDiameterNm = 2.8f;

    private String name;
    private Airport airport;
    private Array<Waypoint> waypoints;
    private Array<int[]> altRestrictions;
    private Array<Integer> maxSpd;
    private Array<Boolean> left; //If false then direction is right
    private Array<Integer> entryProcedure;
    private Array<Integer> inboundHdg;
    private Array<Integer> legDist;
    private Array<float[]> oppPoint;



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

        calculateOppPoint();
    }

    /** Parses input string into the relevant information for the holding procedure */
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

    /** Calculates the coordinates of the point opposite to the fix in the holding pattern, using the given turn diameter and leg distance */
    private void calculateOppPoint() {
        oppPoint = new Array<float[]>();

        int index = 0;
        for (Waypoint waypoint: waypoints) {
            float inboundTrack = inboundHdg.get(index) - RadarScreen.magHdgDev;
            float legPxDist = MathTools.nmToPixel(legDist.get(index));

            float xOffset1 = legPxDist * MathUtils.cosDeg(270 - inboundTrack);
            float yOffset1 = legPxDist * MathUtils.sinDeg(270 - inboundTrack);
            float xOffset2 = turnDiameterNm * MathUtils.cosDeg(-inboundTrack);
            float yOffset2 = turnDiameterNm * MathUtils.sinDeg(-inboundTrack);

            oppPoint.add(new float[] {waypoint.getPosX() + xOffset1 + xOffset2, waypoint.getPosY() + yOffset1 + yOffset2});

            index++;
        }
    }

    /** Gets the point at a distance (nautical mile) away inbound the fix or the opposite fix */
    public float[] getInboundPoint(String waypoint, boolean opposite, float dist) {
        int index = waypoints.indexOf(RadarScreen.waypoints.get(waypoint), false);

        float[] point = new float[2];
        float ptX;
        float ptY;
        float inboundTrack = inboundHdg.get(index) - RadarScreen.magHdgDev;

        if (opposite) {
            ptX = oppPoint.get(index)[0];
            ptY = oppPoint.get(index)[1];
            inboundTrack += 180;
        } else {
            ptX = waypoints.get(index).getPosX();
            ptY = waypoints.get(index).getPosY();
        }

        float distPx = MathTools.nmToPixel(dist);
        point[0] = ptX + distPx * MathUtils.cosDeg(270 - inboundTrack);
        point[1] = ptY + distPx * MathUtils.sinDeg(270 - inboundTrack);

        return point;
    }

    public Array<Waypoint> getWaypoints() {
        return waypoints;
    }

    public int getMaxSpdAtWpt(Waypoint waypoint) {
        int index = waypoints.indexOf(waypoint, false);
        return maxSpd.get(index);
    }
}
