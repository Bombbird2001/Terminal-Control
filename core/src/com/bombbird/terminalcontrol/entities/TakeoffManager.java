package com.bombbird.terminalcontrol.entities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.AircraftType;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

import java.util.HashMap;

public class TakeoffManager {
    private Airport airport;

    private HashMap<String, String[]> nextAircraft;
    private HashMap<String, Aircraft> prevAircraft;
    private HashMap<String, Float> timers;

    public TakeoffManager(Airport airport) {
        this.airport = airport;
        nextAircraft = new HashMap<String, String[]>();
        prevAircraft = new HashMap<String, Aircraft>();
        timers = new HashMap<String, Float>();
        if ("RCTP".equals(airport.getIcao())) {
            timers.put("05L", 180f);
            timers.put("05R", 180f);
            timers.put("23L", 180f);
            timers.put("23R", 180f);
        }
    }

    public void update() {
        //Request takeoffs if takeoffs are less than 5 more than landings
        //Update the timers & next aircrafts to take off
        for (String rwy: timers.keySet()) {
            timers.put(rwy, timers.get(rwy) + Gdx.graphics.getDeltaTime());
            if (nextAircraft.get(rwy) == null) {
                nextAircraft.put(rwy, randomPlane());
            }
        }
        if (airport.getAirborne() - airport.getLandings() < 5) {
            if ("RCTP".equals(airport.getIcao())) {
                updateRCTP();
            }
        }
    }

    /** Generates a random plane (with callsign, aircraft type) */
    private String[] randomPlane() {
        //TODO Load airline callsigns from file
        String[] callsigns = new String[] {"EVA", "CAL", "UIA", "MDA"};
        String[] aircrafts = AircraftType.aircraftTypes.keySet().toArray(new String[0]);
        return new String[] {callsigns[MathUtils.random(callsigns.length - 1)] + Integer.toString(MathUtils.random(1, 999)), aircrafts[MathUtils.random(aircrafts.length - 1)]};
    }

    private void updateRCTP() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getLandingRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 6;
            if ("05L".equals(runway1.getName()) && checkPreceding("05L") && checkPreceding("05R") && checkPreceding("23R") && checkLanding(runway1) && checkLanding(runway1.getOppRwy()) && distance > dist) {
                runway = runway1;
                dist = distance;
            } else if ("05R".equals(runway1.getName()) && checkPreceding("05L") && checkPreceding("05R") && checkPreceding("23L") && checkLanding(runway1) && checkLanding(runway1.getOppRwy()) && distance > dist) {
                runway = runway1;
                dist = distance;
            } else if ("23L".equals(runway1.getName()) && checkPreceding("23L") && checkPreceding("23R") && checkPreceding("05R") && checkLanding(runway1) && checkLanding(runway1.getOppRwy()) && distance > dist) {
                runway = runway1;
                dist = distance;
            } else if ("23R".equals(runway1.getName()) && checkPreceding("23L") && checkPreceding("23R") && checkPreceding("05L") && checkLanding(runway1) && checkLanding(runway1.getOppRwy()) && distance > dist) {
                runway = runway1;
                dist = distance;
            }
        }
        if (runway != null) {
            String callsign = nextAircraft.get(runway.getName())[0];
            RadarScreen.newDeparture(callsign, nextAircraft.get(runway.getName())[1], airport, runway);
            prevAircraft.put(runway.getName(), RadarScreen.AIRCRAFTS.get(callsign));
            nextAircraft.put(runway.getName(), null);
            timers.put(runway.getName(), 0f);
        }
    }

    private boolean checkPreceding(String runway) {
        if (prevAircraft.get(runway) == null) {
            //If no aircraft has taken off before
            return true;
        } else if (nextAircraft.get(runway)[1].equals("M")) {
            if (prevAircraft.get(runway).getWakeCat() == 'H') {
                //Previous is heavy, minimum 120 sec
                return timers.get(runway) >= 120;
            } else {
                //Previous is super, minimum 180 sec
                return timers.get(runway) >= 180;
            }
        } else if (nextAircraft.get(runway)[1].equals("H")) {
            if (prevAircraft.get(runway).getWakeCat() == 'J') {
                //Previous is super, minimum 120 sec
                return timers.get(runway) >= 120;
            }
        }
        return timers.get(runway) >= 90;
    }

    private boolean checkLanding(Runway runway) {
        if (runway.getAircraftsOnAppr().size == 0) {
            //No aircraft on approach
            return true;
        } else {
            Aircraft aircraft = runway.getAircraftsOnAppr().first();
            return MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), runway.getX(), runway.getY())) >= 5 && !aircraft.isOnGround();
        }
    }
}
