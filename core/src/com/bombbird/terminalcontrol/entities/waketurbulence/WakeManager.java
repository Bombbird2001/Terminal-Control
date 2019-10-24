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

    /** Called after 0.5nm travelled, adds a new point from aircraft, updates subsequent points to decrement distance, total maximum 16 points for 8nm */
    public void addPoint(Aircraft aircraft) {
        if (!aircraftWakes.containsKey(aircraft.getCallsign())) aircraftWakes.put(aircraft.getCallsign(), new Array<>());
        aircraftWakes.get(aircraft.getCallsign()).add(new WakePoint(aircraft.getX(), aircraft.getY(), (int) aircraft.getAltitude()));
        int extra = aircraftWakes.get(aircraft.getCallsign()).size - 16;
        if (extra > 0) aircraftWakes.get(aircraft.getCallsign()).removeRange(16, 16 + extra - 1);
    }

    /** Checks for aircraft separation from wake turbulence of other aircraft, returns true or false depending on whether separation infringed */
    public boolean checkAircraftWake(Aircraft aircraft) {
        if (aircraft.getEmergency().isActive()) return false; //Ignore if aircraft is an active emergency
        for (String callsign: aircraftWakes.keySet()) {
            if (callsign.equals(aircraft.getCallsign())) continue; //Skip if is itself
            Aircraft aircraft2 = TerminalControl.radarScreen.aircrafts.get(callsign); //Front plane
            if (MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), aircraft2.getX(), aircraft2.getY()) > 8 * 32.4) continue; //Skip if aircraft is more than 8nm away
            int reqDist = getReqDist(aircraft2, aircraft);
            if (reqDist < 3) continue; //Skip if required separation is very low
            float dist = 0;
            Array<WakePoint> wakePoints = aircraftWakes.get(callsign);
            for (int i = 0; i < wakePoints.size; i++) {
                if (i == 0) {
                    dist += MathTools.pixelToNm(MathTools.distanceBetween(aircraft2.getX(), aircraft2.getY(), wakePoints.get(i).x, wakePoints.get(i).y));
                } else {
                    dist += 0.5;
                }
                if (!MathTools.withinRange(aircraft.getAltitude() - wakePoints.get(i).altitude, -1010, 10)) continue; //If altitude difference between wake point and aircraft does not fulfill 0 to 1000feet, no conflict, continue
                float distBetPoint = MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), wakePoints.get(i).x, wakePoints.get(i).y));
                if (distBetPoint > 0.4f) continue; //If distance between the current point and aircraft is more than 0.4nm, no conflict, continue
                if (dist + distBetPoint < reqDist) return true; //If cumulative distance + point distance is less than required distance, conflict
            }
        }
        return false;
    }

    /** Renders the wake lines when aircraft is selected */
    public void renderWake(String callsign) {
        //TODO Render wake lines
    }

    /** Draws letter representing separation required for each recat category */
    public void drawSepRequired(Aircraft aircraft) {
        //TODO Draw letter
    }

    /** Renders the wake lines/arc when aircraft is on the ILS with aircraft behind it */
    public void renderIlsWake(Aircraft aircraft1, Aircraft aircraft2) {
        //TODO Render ILS wake line
    }

    /** Returns the minimum wake separation between 2 aircraft depending on their recat category */
    private int getReqDist(Aircraft aircraftFront, Aircraft aircraftBack) {
        //Return appropriate dist required
        switch (aircraftFront.getRecat()) {
            case 'A':
                switch (aircraftBack.getRecat()) {
                    case 'A': return 3;
                    case 'B': return 4;
                    case 'C':
                    case 'D':
                        return 5;
                    case 'E': return 6;
                    case 'F': return 8;
                }
                break;
            case 'B':
                switch (aircraftBack.getRecat()) {
                    case 'B': return 3;
                    case 'C':
                    case 'D':
                        return 4;
                    case 'E': return 5;
                    case 'F': return 7;
                }
                break;
            case 'C':
                switch (aircraftBack.getRecat()) {
                    case 'C':
                    case 'D':
                        return 3;
                    case 'E': return 4;
                    case 'F': return 6;
                }
                break;
            case 'D': if (aircraftBack.getRecat() == 'F') return 5; break;
            case 'E': if (aircraftBack.getRecat() == 'F') return 4; break;
        }
        return 3; //Default 3
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
