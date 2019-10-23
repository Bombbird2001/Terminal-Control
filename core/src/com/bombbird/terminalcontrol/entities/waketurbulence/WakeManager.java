package com.bombbird.terminalcontrol.entities.waketurbulence;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class WakeManager {
    private HashMap<String, Array<WakePoint>> aircraftWakes;

    public WakeManager() {
        aircraftWakes = new HashMap<>();
    }

    public WakeManager(JSONObject save) {
        this();
        for (int i = 0; i < save.length(); i++) {
            String callsign = save.names().getString(i);
            JSONArray array = save.getJSONArray(callsign);
            Array<WakePoint> wakePoints = new Array<>();
            for (int j = 0; j < array.length(); j++) {
                wakePoints.add(new WakePoint(array.getJSONObject(j)));
            }
            aircraftWakes.put(callsign, wakePoints);
        }
    }

    /** Initialises aircraft array for new aircraft */
    public void addAircraft(String callsign) {
        aircraftWakes.put(callsign, new Array<WakePoint>());
    }

    /** Removes array for aircraft */
    public void removeAircraft(String callsign) {
        aircraftWakes.remove(callsign);
    }

    /** Called after 0.5nm travelled, adds a new point from aircraft, updates subsequent points to decrement distance, total maximum 16 points for 16nm */
    public void addPoint(Aircraft aircraft) {
        aircraftWakes.get(aircraft.getCallsign()).add(new WakePoint(aircraft.getX(), aircraft.getY(), (int) aircraft.getAltitude()));
        int extra = aircraftWakes.get(aircraft.getCallsign()).size - 16;
        if (extra > 0) aircraftWakes.get(aircraft.getCallsign()).removeRange(16, 16 + extra - 1);
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
        for (String callsign: aircraftWakes.keySet()) {
            JSONArray array = new JSONArray();
            Array<WakePoint> points = aircraftWakes.get(callsign);
            for (int i = 0; i < points.size; i++) {
                JSONObject point = new JSONObject();
                WakePoint pt = points.get(i);
                point.put("x", (double) pt.x);
                point.put("y", (double) pt.y);
                point.put("altitude", pt.altitude);
                array.put(point);
            }
        }
        return save;
    }
}
