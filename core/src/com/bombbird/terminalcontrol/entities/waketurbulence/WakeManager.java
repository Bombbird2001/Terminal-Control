package com.bombbird.terminalcontrol.entities.waketurbulence;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONObject;

import java.util.HashMap;

public class WakeManager {
    private HashMap<String, Array<WakePoint>> aircraftWakes;

    public WakeManager() {
        aircraftWakes = new HashMap<>();
    }

    public WakeManager(JSONObject save) {
        //TODO Get data from save
        this();
    }

    /** Initialises aircraft array for new aircraft */
    public void addAircraft(String callsign) {
        aircraftWakes.put(callsign, new Array<WakePoint>());
    }

    /** Removes array for aircraft */
    public void removeAircraft(String callsign) {
        aircraftWakes.remove(callsign);
    }

    /** Adds a new 1nm point from aircraft, updates subsequent points to decrement distance */
    public void addPoint(Aircraft aircraft) {

    }

    /** Removes last point from aircraft wake points */
    public void removeLastPoint(Aircraft aircraft) {

    }

    /** Checks for aircraft separation from wake turbulence of other aircraft, returns true or false depending on whether separation infringed */
    public boolean checkAircraftWake(Aircraft aircraft) {
        for (String callsign: aircraftWakes.keySet()) {
            if (callsign.equals(aircraft.getCallsign())) continue; //Skip if is itself
            if (aircraft.getEmergency().isActive()) continue; //Ignore if aircraft is an active emergency
            Aircraft aircraft2 = TerminalControl.radarScreen.aircrafts.get(callsign);
            if (MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), aircraft2.getX(), aircraft2.getY()) > 8 * 32.4) continue; //Skip if aircraft is more than 8nm away
            //TODO Check the wake points for distance (depends on recat category), altitude (+0 feet to -1000 feet)
        }
        return false;
    }

    /** Returns a jsonobject used to save data for this wake manager */
    public JSONObject getSave() {
        JSONObject save = new JSONObject();
        //TODO Add save in gameSaver, gameLoader
        return save;
    }
}
