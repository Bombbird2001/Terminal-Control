package com.bombbird.terminalcontrol.entities.trafficmanager;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.RandomGenerator;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TakeoffManager {
    private Airport airport;

    private HashMap<String, String[]> nextAircraft;
    private HashMap<String, Aircraft> prevAircraft;
    private HashMap<String, Float> timers;

    private RadarScreen radarScreen;

    public TakeoffManager(Airport airport) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        nextAircraft = new HashMap<String, String[]>();
        prevAircraft = new HashMap<String, Aircraft>();
        timers = new HashMap<String, Float>();
        for (Runway runway: airport.getRunways().values()) {
            timers.put(runway.getName(), 180f);
            prevAircraft.put(runway.getName(), null);
        }
    }

    public TakeoffManager(Airport airport, JSONObject save) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        nextAircraft = new HashMap<String, String[]>();
        prevAircraft = new HashMap<String, Aircraft>();
        timers = new HashMap<String, Float>();
        for (Runway runway: airport.getRunways().values()) {
            JSONArray info = save.getJSONObject("nextAircraft").getJSONArray(runway.getName());
            if (info.length() == 2) {
                nextAircraft.put(runway.getName(), new String[]{info.getString(0), info.getString(1)});
            } else {
                nextAircraft.put(runway.getName(), null);
            }

            timers.put(runway.getName(), (float) save.getJSONObject("timers").getDouble(runway.getName()));
        }
    }

    /** Called after aircraft load during game load since aircrafts have not been loaded during the initial airport loading */
    public void updatePrevAcft(JSONObject save) {
        for (Runway runway : airport.getRunways().values()) {
            prevAircraft.put(runway.getName(), radarScreen.aircrafts.get(save.getJSONObject("prevAircraft").isNull(runway.getName()) ? null : save.getJSONObject("prevAircraft").getString(runway.getName())));
        }
    }

    public void update() {
        //Request takeoffs if takeoffs are less than 5 more than landings
        //Update the timers & next aircrafts to take off
        for (String rwy: timers.keySet()) {
            timers.put(rwy, timers.get(rwy) + Gdx.graphics.getDeltaTime());
            if (nextAircraft.get(rwy) == null) {
                String[] aircraftInfo = RandomGenerator.randomPlane(airport);
                nextAircraft.put(rwy, aircraftInfo);
            }
        }
        if (airport.getAirborne() - airport.getLandings() < 3) {
            if ("RCTP".equals(airport.getIcao())) {
                updateRCTP();
            } else if ("RCSS".equals(airport.getIcao())) {
                updateRCSS();
            } else if ("WSSS".equals(airport.getIcao())) {
                updateWSSS();
            } else if ("RJTT".equals(airport.getIcao())) {
                updateRJTT();
            } else if ("RJAA".equals(airport.getIcao())) {
                updateRJAA();
            } else if ("RJBB".equals(airport.getIcao())) {
                updateRJBB();
            } else if ("RJOO".equals(airport.getIcao())) {
                updateRJOO();
            } else if ("RJBE".equals(airport.getIcao())) {
                updateRJBE();
            } else if ("VHHH".equals(airport.getIcao())) {
                updateVHHH();
            } else if ("VMMC".equals(airport.getIcao())) {
                updateVMMC();
            }
        }
    }

    /** Checks takeoff status for Taiwan Taoyuan */
    private void updateRCTP() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && distance > dist) {
                if ("05L".equals(runway1.getName()) && checkPreceding("05R") && checkOppLanding(airport.getRunways().get("05R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("05R".equals(runway1.getName()) && checkPreceding("05L") && checkOppLanding(airport.getRunways().get("05L"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("23L".equals(runway1.getName()) && checkPreceding("23R") && checkOppLanding(airport.getRunways().get("23R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("23R".equals(runway1.getName()) && checkPreceding("23L") && checkOppLanding(airport.getRunways().get("23L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Taipei Songshan */
    private void updateRCSS() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding("10") && checkPreceding("28") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1;
                dist = distance;
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Singapore Changi */
    private void updateWSSS() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && distance > dist) {
                if ("02L".equals(runway1.getName()) && checkPreceding("02C") && checkOppLanding(airport.getRunways().get("02C"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("02C".equals(runway1.getName()) && checkPreceding("02L") && checkOppLanding(airport.getRunways().get("02L"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("20C".equals(runway1.getName()) && checkPreceding("20R") && checkOppLanding(airport.getRunways().get("20R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("20R".equals(runway1.getName()) && checkPreceding("20C") && checkOppLanding(airport.getRunways().get("20C"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Tokyo Haneda */
    private void updateRJTT() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && distance > dist) {
                if ("34R".equals(runway1.getName()) && checkPreceding("05") && checkOppLanding(airport.getRunways().get("04")) && checkOppLanding(airport.getRunways().get("05"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("05".equals(runway1.getName()) && checkPreceding("34R") && checkOppLanding(airport.getRunways().get("04"))) {
                    //Additional check if aircraft landing on 34R is no longer in conflict with 05
                    boolean tkof = false;
                    Runway r34r = airport.getRunways().get("34R");
                    if (r34r.getAircraftsOnAppr().size == 0) {
                        tkof = true;
                    } else {
                        int index = 0;
                        while (index < r34r.getAircraftsOnAppr().size) {
                            Aircraft aircraft = r34r.getAircraftsOnAppr().get(index);
                            if (aircraft.isOnGround()) {
                                tkof = true;
                            } else {
                                tkof = !(MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), r34r.getX(), r34r.getY())) < 6);
                                break;
                            }
                            index++;
                        }
                    }
                    if (tkof) {
                        runway = runway1;
                        dist = distance;
                    }
                } else if ("16L".equals(runway1.getName()) && checkLandingRJTT23() && checkOppLanding(airport.getRunways().get("16R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("16R".equals(runway1.getName()) && checkLandingRJTT23() && checkLanding(airport.getRunways().get("22")) && checkOppLanding(airport.getRunways().get("16L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Tokyo Narita */
    private void updateRJAA() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && distance > dist) {
                if ("16L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("16R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("16R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("16L"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("34L".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("34R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("34R".equals(runway1.getName()) && checkOppLanding(airport.getRunways().get("34L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Osaka Kansai */
    private void updateRJBB() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && distance > dist) {
                if ("06L".equals(runway1.getName()) && checkPreceding("06R") && checkOppLanding(airport.getRunways().get("06R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("06R".equals(runway1.getName()) && checkPreceding("06L") && checkOppLanding(airport.getRunways().get("06L"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("24L".equals(runway1.getName()) && checkPreceding("24R") && checkOppLanding(airport.getRunways().get("24R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("24R".equals(runway1.getName()) && checkPreceding("24L") && checkOppLanding(airport.getRunways().get("24L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Osaka Itami */
    private void updateRJOO() {
        Runway runway = airport.getRunways().get("32L");
        if (runway.isTakeoff() && checkPreceding("32L") && checkLanding(runway)) {
            updateRunway(runway);
        }
    }

    /** Checks takeoff status for Kobe */
    private void updateRJBE() {
        Runway runway = airport.getRunways().get("09");
        if (runway.isTakeoff() && checkPreceding("09") && checkLanding(runway)) {
            updateRunway(runway);
        }
    }

    /** Checks takeoff status for Hong Kong */
    private void updateVHHH() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding(runway1.getName()) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.getOppRwy().getName()) && distance > dist) {
                if ("07L".equals(runway1.getName()) && checkPreceding("07R") && checkOppLanding(airport.getRunways().get("07R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("07R".equals(runway1.getName()) && checkPreceding("07L") && checkOppLanding(airport.getRunways().get("07L"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("25L".equals(runway1.getName()) && checkPreceding("25R") && checkOppLanding(airport.getRunways().get("25R"))) {
                    runway = runway1;
                    dist = distance;
                } else if ("25R".equals(runway1.getName()) && checkPreceding("25L") && checkOppLanding(airport.getRunways().get("25L"))) {
                    runway = runway1;
                    dist = distance;
                }
            }
        }
        updateRunway(runway);
    }

    /** Checks takeoff status for Macau */
    private void updateVMMC() {
        Runway runway = null;
        float dist = -1;
        for (Runway runway1: airport.getTakeoffRunways().values()) {
            float distance = runway1.getAircraftsOnAppr().size > 0 ? MathTools.pixelToNm(MathTools.distanceBetween(runway1.getAircraftsOnAppr().first().getX(), runway1.getAircraftsOnAppr().first().getY(), runway1.getX(), runway1.getY())) : 25;
            if (checkPreceding("16") && checkPreceding("34") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1;
                dist = distance;
            }
        }
        updateRunway(runway);
    }

    /** Checks whether airport has available runways for takeoff, updates hashMap and timer if available */
    private void updateRunway(Runway runway) {
        if (runway != null) {
            String callsign = nextAircraft.get(runway.getName())[0];
            radarScreen.newDeparture(callsign, nextAircraft.get(runway.getName())[1], airport, runway);
            prevAircraft.put(runway.getName(), radarScreen.aircrafts.get(callsign));
            nextAircraft.put(runway.getName(), null);
            timers.put(runway.getName(), 0f);
        }
    }

    /** Check the previous departure aircraft */
    private boolean checkPreceding(String runway) {
        float additionalTime = 100 - 20 * (airport.getLandings() - airport.getAirborne()); //Additional time between departures when arrivals are not much higher than departures
        additionalTime = MathUtils.clamp(additionalTime, 0, 150);
        if (prevAircraft.get(runway) == null) {
            //If no aircraft has taken off before
            return true;
        } else if (nextAircraft.get(runway)[1].equals("M")) {
            if (prevAircraft.get(runway).getWakeCat() == 'H') {
                //Previous is heavy, minimum 120 sec
                return timers.get(runway) >= 120 + additionalTime;
            } else {
                //Previous is super, minimum 180 sec
                return timers.get(runway) >= 180 + additionalTime;
            }
        } else if (nextAircraft.get(runway)[1].equals("H") && prevAircraft.get(runway).getWakeCat() == 'J') {
            //Previous is super, minimum 120 sec
            return timers.get(runway) >= 120 + additionalTime;
        }
        return timers.get(runway) >= 90 + additionalTime;
    }

    /** Check for any landing aircrafts */
    private boolean checkLanding(Runway runway) {
        if (runway.getAircraftsOnAppr().size == 0) {
            //No aircraft on approach
            return true;
        } else {
            Aircraft aircraft = runway.getAircraftsOnAppr().first();
            return MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), runway.getX(), runway.getY())) >= 5 && !aircraft.isOnGround() && runway.getOppRwy().getAircraftsOnAppr().size == 0;
        }
    }

    /** Checks specific case for RJTT's runway 23, same as above function but will also return true if aircraft has landed */
    private boolean checkLandingRJTT23() {
        if (!"RJTT".equals(airport.getIcao())) return false;
        Runway runway = airport.getRunways().get("23");
        if (runway.getAircraftsOnAppr().size == 0) {
            //No aircraft on approach
            return true;
        } else {
            Aircraft aircraft = runway.getAircraftsOnAppr().first();
            Aircraft aircraft1 = null;
            if (runway.getAircraftsOnAppr().size > 1) aircraft1 = runway.getAircraftsOnAppr().get(1);
            if (runway.getOppRwy().getAircraftsOnAppr().size == 0) {
                //No planes landing opposite
                if (MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), runway.getX(), runway.getY())) >= 5 && !aircraft.isOnGround()) {
                    //If latest aircraft is more than 5 miles away and not landed yet
                    return true;
                } else {
                    //If first aircraft has touched down, 2nd aircraft is non-existent OR is more than 5 miles away
                    return aircraft.isOnGround() && (aircraft1 == null || !aircraft1.isOnGround() && MathTools.pixelToNm(MathTools.distanceBetween(aircraft1.getX(), aircraft1.getY(), runway.getX(), runway.getY())) >= 5);
                }
            }
            return false;
        }
    }

    /** Check for any aircrafts landing on opposite runway */
    private boolean checkOppLanding(Runway runway) {
        return runway.getOppRwy().getAircraftsOnAppr().size == 0;
    }

    public HashMap<String, String[]> getNextAircraft() {
        return nextAircraft;
    }

    public HashMap<String, Aircraft> getPrevAircraft() {
        return prevAircraft;
    }

    public HashMap<String, Float> getTimers() {
        return timers;
    }
}
