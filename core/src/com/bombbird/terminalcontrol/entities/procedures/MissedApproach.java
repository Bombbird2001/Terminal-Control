package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class MissedApproach {
    private String name;
    private Airport airport;
    private ILS ils;
    private int climbAlt;
    private int climbSpd;
    private String transition;
    private float transInfo;
    private Queue<Boolean> turnDir;
    private Queue<Waypoint> waypoints;
    private Queue<Integer> heading;
    private HoldProcedure holdProcedure;

    public MissedApproach(String name, Airport airport, String info) {
        this.name = name;
        this.airport = airport;
        turnDir = new Queue<Boolean>();
        waypoints = new Queue<Waypoint>();
        heading = new Queue<Integer>();

        parseInfo(info);
    }

    private void parseInfo(String info) {
        int index = 0;
        for (String s: info.split(",")) {
            switch (index) {
                case 0: climbAlt = Integer.parseInt(s); break;
                case 1: climbSpd = Integer.parseInt(s); break;
                case 2:
                    String[] trans = s.split(" ");
                    transition = trans[0];
                    transInfo = Float.parseFloat(trans[1]);
                    break;
                default:
                    String[] next = s.split(" ");
                    turnDir.addLast(next[1].equals("LEFT"));
                    if (next[0].equals("DIR")) {
                        waypoints.addLast(RadarScreen.waypoints.get(next[2]));
                        heading.addLast(null);
                    } else if (next[0].equals("HDG")) {
                        waypoints.addLast(null);
                        heading.addLast(Integer.parseInt(next[2]));
                    } else if (next[0].equals("HOLD")) {
                        waypoints.addLast(RadarScreen.waypoints.get(next[2]));
                        heading.addLast(null);
                        holdProcedure = airport.getHoldProcedures().get(name);
                    }
            }
            index++;
        }
    }

    public int getClimbAlt() {
        return climbAlt;
    }

    public int getClimbSpd() {
        return climbSpd;
    }

    public String getTransition() {
        return transition;
    }

    public float getTransInfo() {
        return transInfo;
    }

    public Queue<Boolean> getTurnDir() {
        return turnDir;
    }

    public Queue<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Queue<Integer> getHeading() {
        return heading;
    }

    public HoldProcedure getHoldProcedure() {
        return holdProcedure;
    }
}
