package com.bombbird.terminalcontrol.entities.procedures;

import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import org.json.JSONObject;

public class MissedApproach {
    private String name;
    private Airport airport;
    private ILS ils;
    private int climbAlt;
    private int climbSpd;

    public MissedApproach(String name, Airport airport, JSONObject jo) {
        this.name = name;
        this.airport = airport;

        climbAlt = jo.getInt("altitude");
        climbSpd = jo.getInt("speed");
    }

    public void loadIls() {
        ils = airport.getApproaches().get(name);
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
