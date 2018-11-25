package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.utilities.MathTools;
import org.json.JSONObject;

import java.util.HashMap;

public class ArrivalManager {
    private HashMap<Waypoint, Aircraft> entryPoint;

    public ArrivalManager() {
        entryPoint = new HashMap<Waypoint, Aircraft>();

        for (Airport airport: TerminalControl.radarScreen.airports.values()) {
            for (Star star: airport.getStars().values()) {
                entryPoint.put(star.getWaypoint(0), null);
            }
        }
    }

    public ArrivalManager(JSONObject save) {
        this();

        for (String waypoint: save.keySet()) {
            Aircraft aircraft = save.isNull(waypoint) ? null : TerminalControl.radarScreen.aircrafts.get(save.getString(waypoint));
            entryPoint.put(TerminalControl.radarScreen.waypoints.get(waypoint), aircraft);
        }
    }

    public void checkArrival(Arrival arrival) {
        Waypoint entryPt = arrival.getSidStar().getWaypoint(0);
        Aircraft prevAcft = entryPoint.get(entryPt);
        if (prevAcft != null && arrival.getAltitude() - prevAcft.getAltitude() < 2500 && MathTools.pixelToNm(MathTools.distanceBetween(arrival.getX(), arrival.getY(), prevAcft.getX(), prevAcft.getY())) < 3) {
            if (arrival.getTypDes() - prevAcft.getTypDes() > 300) {
                arrival.setAltitude(prevAcft.getAltitude() + 3500);
            } else {
                arrival.setAltitude(prevAcft.getAltitude() + 2500);
            }
            arrival.setClearedIas(prevAcft.getClearedIas() > 250 ? 250 : prevAcft.getClearedIas() - 10);
            arrival.getNavState().getClearedSpd().removeFirst();
            arrival.getNavState().getClearedSpd().addFirst(arrival.getClearedIas());

            arrival.setClearedAltitude(prevAcft.getClearedAltitude() + 1000);
            arrival.getNavState().getClearedAlt().removeFirst();
            arrival.getNavState().getClearedAlt().addFirst(arrival.getClearedAltitude());
        }
        entryPoint.put(entryPt, arrival);
    }

    public HashMap<Waypoint, Aircraft> getEntryPoint() {
        return entryPoint;
    }
}
