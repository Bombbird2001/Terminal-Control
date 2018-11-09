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
    private Queue<String> procedure;

    public MissedApproach(String name, Airport airport, String info) {
        this.name = name;
        this.airport = airport;
        procedure = new Queue<String>();

        parseInfo(info);
    }

    public void loadIls() {
        ils = airport.getApproaches().get(name);
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
                    procedure.addLast(s);
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

    public HoldProcedure getHoldProcedure() {
        return airport.getHoldProcedures().get(name);
    }

    public ILS getIls() {
        return ils;
    }

    public Queue<String> getProcedure() {
        return procedure;
    }
}
