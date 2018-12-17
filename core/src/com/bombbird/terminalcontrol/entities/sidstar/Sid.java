package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;

public class Sid extends SidStar {
    private int[] initClimb;
    private Array<Integer> outboundHdg;
    private String[] centre;

    public Sid(Airport airport, String toParse) {
        super(airport, toParse);
    }

    @Override
    public void parseInfo(String toParse) {
        super.parseInfo(toParse);

        String[] sidInfo = toParse.split(",");

        outboundHdg = new Array<Integer>();
        initClimb = new int[3];

        int index = 0;
        for (String s1: sidInfo) {
            switch (index) {
                case 0: setName(s1); break; //First part is the name of the SID
                case 1: //Add SID runways
                    for (String s2: s1.split(">")) {
                        getRunways().add(s2);
                    }
                    break;
                case 2: //Second part is the initial climb of the SID
                    String[] info = s1.split(" ");
                    for (int i = 0; i < 3; i++) {
                        initClimb[i] = Integer.parseInt(info[i]);
                    }
                    break;
                case 3: //Add waypoints to SID
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
                                    Gdx.app.log("Load error", "Unexpected additional waypoint parameter " + s3 + " in " + s2 + " in game/" + TerminalControl.radarScreen.mainName + "/sid" + getAirport().getIcao() + ".sid");
                            }
                            index1++;
                        }
                        getRestrictions().add(altSpdRestrictions);
                    }
                    break;
                case 4: //Outbound heading after last waypoint
                    for (String s2: s1.split(">")) {
                        outboundHdg.add(Integer.parseInt(s2));
                    }
                    break;
                case 5: //Centre callsign & frequency
                    centre = s1.split(">");
                    break;
                default: Gdx.app.log("Load error", "Unexpected additional waypoint parameter in game/" + TerminalControl.radarScreen.mainName + "/sid" + getAirport().getIcao() + ".sid");
            }
            index++;
        }
    }

    public int getOutboundHdg() {
        return outboundHdg.get(MathUtils.random(outboundHdg.size - 1));
    }

    public int[] getInitClimb() {
        return initClimb;
    }

    public String[] getCentre() {
        return centre;
    }
}
