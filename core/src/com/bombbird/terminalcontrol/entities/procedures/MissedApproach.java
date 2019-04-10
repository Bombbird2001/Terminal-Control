package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.Gdx;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.approaches.ILS;

public class MissedApproach {
    private String name;
    private Airport airport;
    private ILS ils;
    private int climbAlt;
    private int climbSpd;

    public MissedApproach(String name, Airport airport, String info) {
        this.name = name;
        this.airport = airport;

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
                default:
                    Gdx.app.log("Missed approach error", "Unexpected additional parameter for " + ils.getName() + " approach!");
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

    public ILS getIls() {
        return ils;
    }
}
