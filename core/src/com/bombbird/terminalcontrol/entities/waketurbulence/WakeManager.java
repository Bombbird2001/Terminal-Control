package com.bombbird.terminalcontrol.entities.waketurbulence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.separation.trajectory.PositionPoint;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class WakeManager {
    private final HashMap<String, Array<PositionPoint>> aircraftWakes;

    public WakeManager() {
        aircraftWakes = new HashMap<>();
    }

    public WakeManager(JSONObject save) {
        this();
        for (int i = 0; i < save.length(); i++) {
            String callsign = save.names().getString(i);
            JSONArray array = save.getJSONArray(callsign);
            Array<PositionPoint> wakePoints = new Array<>();
            for (int j = 0; j < array.length(); j++) {
                wakePoints.add(new PositionPoint(array.getJSONObject(j)));
            }
            aircraftWakes.put(callsign, wakePoints);
        }
    }

    /** Initialises aircraft array for new aircraft */
    public void addAircraft(String callsign) {
        aircraftWakes.put(callsign, new Array<>());
    }

    /** Removes array for aircraft */
    public void removeAircraft(String callsign) {
        aircraftWakes.remove(callsign);
    }

    /** Called after 0.5nm travelled, adds a new point from aircraft, updates subsequent points to decrement distance, total maximum 16 points for 8nm */
    public void addPoint(Aircraft aircraft) {
        if (!aircraftWakes.containsKey(aircraft.getCallsign())) aircraftWakes.put(aircraft.getCallsign(), new Array<>());
        aircraftWakes.get(aircraft.getCallsign()).add(new PositionPoint(aircraft, aircraft.getX(), aircraft.getY(), (int) aircraft.getAltitude()));
        int extra = aircraftWakes.get(aircraft.getCallsign()).size - 16;
        if (extra > 0) aircraftWakes.get(aircraft.getCallsign()).removeRange(0, extra - 1);
    }

    /** Checks for aircraft separation from wake turbulence of other aircraft, returns -1 if safe separation, else a positive float which is the difference between required, actual separation */
    public float checkAircraftWake(Aircraft aircraft) {
        if (aircraft.isOnGround()) {
            //Remove wake array, ignore if aircraft has landed
            removeAircraft(aircraft.getCallsign());
            return -1;
        }
        if (aircraft instanceof Departure && aircraft.getAltitude() <= aircraft.getAirport().getElevation() + 3000) return -1; //Temporarily ignore if departure is still below 3000 feet AGL
        if (aircraft.getEmergency().isActive()) return -1; //Ignore if aircraft is an active emergency
        for (String callsign: aircraftWakes.keySet()) {
            if (callsign.equals(aircraft.getCallsign())) continue; //Skip if is itself
            Aircraft aircraft2 = TerminalControl.radarScreen.aircrafts.get(callsign); //Front plane
            if (MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), aircraft2.getX(), aircraft2.getY()) > 8 * 32.4) continue; //Skip if aircraft is more than 8nm away
            int reqDist = SeparationMatrix.getWakeSepDist(aircraft2.getRecat(), aircraft.getRecat());
            if (reqDist < 3) continue; //Skip if required separation is less than 3
            float dist = 0;
            Array<PositionPoint> wakePoints = aircraftWakes.get(callsign);
            for (int i = wakePoints.size - 1; i >= 0; i--) {
                if (i == wakePoints.size - 1) {
                    dist += MathTools.pixelToNm(MathTools.distanceBetween(aircraft2.getX(), aircraft2.getY(), wakePoints.get(i).x, wakePoints.get(i).y));
                } else {
                    dist += 0.5;
                }
                if (!MathTools.withinRange(aircraft.getAltitude() - wakePoints.get(i).altitude, -950, 50)) continue; //If altitude difference between wake point and aircraft does not fulfill 0 to less than 1000feet, no conflict, continue
                float distBetPoint = MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), wakePoints.get(i).x, wakePoints.get(i).y));
                if (distBetPoint > 0.4f) continue; //If distance between the current point and aircraft is more than 0.4nm, no conflict, continue
                if (dist + distBetPoint < reqDist - 0.2) return reqDist - dist - distBetPoint; //If cumulative distance + point distance is less than required distance, conflict (0.2nm leeway)
            }
        }
        return -1;
    }

    /** Renders the wake lines when aircraft is selected, call in shape renderer render method only */
    public void renderWake(Aircraft aircraft) {
        if (!aircraftWakes.containsKey(aircraft.getCallsign())) return;
        float prevX = aircraft.getRadarX();
        float prevY = aircraft.getRadarY();
        TerminalControl.radarScreen.shapeRenderer.setColor(Color.ORANGE);
        Array<PositionPoint> points = aircraftWakes.get(aircraft.getCallsign());
        for (int i = points.size - 2; i >= 0; i--) {
            TerminalControl.radarScreen.shapeRenderer.line(prevX, prevY, points.get(i).x, points.get(i).y);
            prevX = points.get(i).x;
            prevY = points.get(i).y;
            if ((points.size - 1 - i) % 2 == 0) TerminalControl.radarScreen.shapeRenderer.circle(prevX, prevY, 8); //Draw only for 1nm intervals
        }
    }

    /** Draws letter representing separation required for each recat category, call in draw method only */
    public void drawSepRequired(Batch batch, Aircraft aircraft) {
        if (!aircraftWakes.containsKey(aircraft.getCallsign())) return;
        Array<PositionPoint> points = aircraftWakes.get(aircraft.getCallsign());
        int prevDist = 0;
        for (int i = 0; i < 6; i++) {
            int reqDist = SeparationMatrix.getWakeSepDist(aircraft.getRecat(), (char)(i + 'A'));
            if (reqDist == 0) continue;
            if (reqDist == prevDist) continue;
            int index = points.size - 1 - reqDist * 2;
            if (index < 0) break;
            String thing = Character.toString((char)(i + 'A'));
            if (i < 5 && SeparationMatrix.getWakeSepDist(aircraft.getRecat(), (char)(i + 1 + 'A')) == reqDist) thing += "/" + (char)(i + 1 + 'A');
            int offset = thing.length() > 1 ? 20 : 6;
            Fonts.defaultFont6.draw(batch, thing, points.get(index).x - offset, points.get(index).y);
            prevDist = reqDist;
        }
    }

    /** Renders the wake lines/arc when aircraft is on the ILS with aircraft behind it, call in shape renderer render method only */
    public void renderIlsWake() {
        for (Aircraft aircraft: TerminalControl.radarScreen.aircrafts.values()) {
            if (aircraft == null) continue;
            if (aircraft instanceof Departure) continue;
            if (!aircraft.isLocCap()) continue;
            if (aircraft.isOnGround()) continue;
            Aircraft aircraft1 = null;
            int index = aircraft.getIls().getRwy().getAircraftsOnAppr().size - 1;
            while (true) {
                if (index < 0) break;
                Aircraft nextAircraft = aircraft.getIls().getRwy().getAircraftsOnAppr().get(index);
                if (nextAircraft == null || nextAircraft.getCallsign().equals(aircraft.getCallsign())) break;
                aircraft1 = aircraft.getIls().getRwy().getAircraftsOnAppr().get(index);
                index--;
            }
            if (aircraft1 == null) continue;
            int reqDist = SeparationMatrix.getWakeSepDist(aircraft.getRecat(), aircraft1.getRecat());
            if (reqDist < 3) continue;
            Vector2 centre = aircraft.getIls().getPointAtDist(aircraft.getIls().getDistFrom(aircraft.getRadarX(), aircraft.getRadarY()) + reqDist);
            int halfWidth = aircraft.isSelected() ? 50 : 30;
            double trackRad = Math.toRadians(aircraft.getIls().getHeading() - TerminalControl.radarScreen.magHdgDev);
            float xOffset = halfWidth * (float) Math.cos(trackRad);
            float yOffset = halfWidth * (float) Math.sin(trackRad);
            TerminalControl.radarScreen.shapeRenderer.setColor(aircraft.isSelected() ? Color.YELLOW : Color.ORANGE);
            TerminalControl.radarScreen.shapeRenderer.line(centre.x - xOffset, centre.y + yOffset, centre.x + xOffset, centre.y - yOffset);
        }
    }

    /** Returns a jsonobject used to save data for this wake manager */
    public JSONObject getSave() {
        JSONObject save = new JSONObject();
        for (String callsign: aircraftWakes.keySet()) {
            JSONArray array = new JSONArray();
            Array<PositionPoint> points = aircraftWakes.get(callsign);
            for (int i = 0; i < points.size; i++) {
                JSONObject point = new JSONObject();
                PositionPoint pt = points.get(i);
                point.put("x", (double) pt.x);
                point.put("y", (double) pt.y);
                point.put("altitude", pt.altitude);
                array.put(point);
            }
            save.put(callsign, array);
        }
        return save;
    }
}
