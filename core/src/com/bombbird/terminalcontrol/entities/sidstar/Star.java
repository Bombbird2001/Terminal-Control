package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.procedures.HoldProcedure;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class Star extends SidStar {
    private Array<Integer> inboundHdg;

    public Star(Airport airport, String toParse) {
        super(airport, toParse);
    }

    @Override
    public void parseInfo(String toParse) {
        super.parseInfo(toParse);

        String[] starInfo = toParse.split(",");

        inboundHdg = new Array<Integer>();

        int index = 0;
        for (String s1: starInfo) {
            switch (index) {
                case 0: setName(s1); break; //First part is the name of the STAR
                case 1: //Add STAR runways
                    for (String s2: s1.split(">")) {
                        getRunways().add(s2);
                    }
                    break;
                case 2: //Second part is the inbound track(s) of the STAR
                    for (String s2: s1.split(">")) {
                        inboundHdg.add(Integer.parseInt(s2));
                    }
                    break;
                case 3: //Add waypoints to STAR
                    for (String s2: s1.split(">")) {
                        //For each waypoint on the STAR
                        int index1 = 0;
                        int[] altSpdRestrictions = new int[3];
                        for (String s3: s2.split(" ")) {
                            switch (index1) {
                                case 0:
                                    getWaypoints().add(TerminalControl.radarScreen.waypoints.get(s3));
                                    break; //First part is the name of the waypoint
                                case 1:
                                    altSpdRestrictions[0] = Integer.parseInt(s3);
                                    break; //1 is min alt
                                case 2:
                                    altSpdRestrictions[1] = Integer.parseInt(s3);
                                    break; //2 is max alt
                                case 3:
                                    altSpdRestrictions[2] = Integer.parseInt(s3);
                                    break; //3 is max speed
                                default:
                                    Gdx.app.log("Load error", "Unexpected additional waypoint parameter " + s3 + " in " + s2 + " in game/" + TerminalControl.radarScreen.mainName + "/star" + getAirport().getIcao() + ".star");
                            }
                            index1++;
                        }
                        getRestrictions().add(altSpdRestrictions);
                    }
                    break;
                default: Gdx.app.log("Load error", "Unexpected additional waypoint parameter in game/" + TerminalControl.radarScreen.mainName + "/star" + getAirport().getIcao() + ".star");
            }
            index++;
        }
    }

    public float distBetRemainPts(int nextWptIndex) {
        int currentIndex = nextWptIndex;
        float dist = 0;
        while (currentIndex < getWaypoints().size - 1) {
            dist += distBetween(currentIndex, currentIndex + 1);
            currentIndex++;
        }
        return dist;
    }

    public float distBetween(int pt1, int pt2) {
        Waypoint waypoint1 = getWaypoint(pt1);
        Waypoint waypoint2 = getWaypoint(pt2);
        return MathTools.pixelToNm(MathTools.distanceBetween(waypoint1.getPosX(), waypoint1.getPosY(), waypoint2.getPosX(), waypoint2.getPosY()));
    }

    public int getInboundHdg() {
        return inboundHdg.get(MathUtils.random(inboundHdg.size - 1));
    }

    public HoldProcedure getHoldProcedure() {
        return getAirport().getHoldProcedures().get(getName());
    }
}
