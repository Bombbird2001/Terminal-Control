package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONObject;

import java.util.HashMap;

public class ArrivalManager {
    private HashMap<String, Aircraft> entryPoint;

    public ArrivalManager() {
        entryPoint = new HashMap<String, Aircraft>();

        for (Airport airport: TerminalControl.radarScreen.airports.values()) {
            for (Star star: airport.getStars().values()) {
                Array<String> inboundWpts = star.getAllInboundWpt();
                for (int i = 0; i < inboundWpts.size; i++) {
                    entryPoint.put(inboundWpts.get(0), null);
                }
            }
        }
    }

    public ArrivalManager(JSONObject save) {
        this();

        for (String waypoint: save.keySet()) {
            Aircraft aircraft = save.isNull(waypoint) ? null : TerminalControl.radarScreen.aircrafts.get(save.getString(waypoint));
            entryPoint.put(waypoint, aircraft);
        }
    }

    public void checkArrival(Arrival arrival) {
        String entryPt = arrival.getRoute().getWaypoint(0).getName();
        Aircraft prevAcft = entryPoint.get(entryPt);
        if (prevAcft != null) {
            if (TerminalControl.radarScreen.aircrafts.get(prevAcft.getCallsign()) == null) {
                entryPoint.put(entryPt, null);
            } else if (arrival.getAltitude() - prevAcft.getAltitude() < 2500 && MathTools.pixelToNm(MathTools.distanceBetween(arrival.getX(), arrival.getY(), prevAcft.getX(), prevAcft.getY())) < 6) {
                if (arrival.getTypDes() - prevAcft.getTypDes() > 300) {
                    arrival.setAltitude(prevAcft.getAltitude() + 3500);
                } else {
                    arrival.setAltitude(prevAcft.getAltitude() + 2500);
                }
                arrival.setClearedIas(prevAcft.getClearedIas() > 250 ? 250 : prevAcft.getClearedIas() - 10);
                arrival.getNavState().getClearedSpd().removeFirst();
                arrival.getNavState().getClearedSpd().addFirst(arrival.getClearedIas());

                if (arrival.getClearedAltitude() < prevAcft.getClearedAltitude() + 1000)
                    arrival.setClearedAltitude(prevAcft.getClearedAltitude() + 1000);
                arrival.getNavState().getClearedAlt().removeFirst();
                arrival.getNavState().getClearedAlt().addFirst(arrival.getClearedAltitude());
            }
        }
        entryPoint.put(entryPt, arrival);
    }

    public HashMap<String, Aircraft> getEntryPoint() {
        return entryPoint;
    }
}
