package com.bombbird.terminalcontrol.entities.airports;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.procedures.*;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.trafficmanager.RunwayManager;
import com.bombbird.terminalcontrol.entities.trafficmanager.TakeoffManager;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Airport {
    private HashMap<String, Runway> runways;
    private HashMap<String, Runway> landingRunways;
    private HashMap<String, Runway> takeoffRunways;
    private HashMap<String, HoldingPoints> holdingPoints;
    private HashMap<String, MissedApproach> missedApproaches;
    private HashMap<String, ILS> approaches;
    private String icao;
    private JSONObject metar;
    private HashMap<String, Star> stars;
    private HashMap<String, Sid> sids;
    private int elevation;
    private String ws;
    private TakeoffManager takeoffManager;
    private RunwayManager runwayManager;
    private int landings;
    private int airborne;
    private boolean congested;

    private int aircraftRatio;
    private HashMap<Integer, String> airlines;
    private HashMap<String, String> aircrafts;

    private Array<ApproachZone> approachZones;

    public Airport(String icao, int elevation, int aircraftRatio) {
        this.icao = icao;
        this.elevation = elevation;
        this.aircraftRatio = aircraftRatio;
        runways = FileLoader.loadRunways(icao);
        landingRunways = new HashMap<String, Runway>();
        takeoffRunways = new HashMap<String, Runway>();
        landings = 0;
        airborne = 0;
        congested = false;
        airlines = FileLoader.loadAirlines(icao);
        aircrafts = FileLoader.loadAirlineAircrafts(icao);
    }

    public Airport(JSONObject save) {
        icao = save.getString("icao");
        elevation = save.getInt("elevation");
        runways = FileLoader.loadRunways(icao);
        landingRunways = new HashMap<String, Runway>();
        takeoffRunways = new HashMap<String, Runway>();
        landings = save.getInt("landings");
        airborne = save.getInt("airborne");
        congested = save.getBoolean("congestion");
        aircraftRatio = save.getInt("aircraftRatio");
        airlines = FileLoader.loadAirlines(icao);
        aircrafts = FileLoader.loadAirlineAircrafts(icao);

        JSONArray landing = save.getJSONArray("landingRunways");
        for (int i = 0; i < landing.length(); i++) {
            Runway runway = runways.get(landing.getString(i));
            runway.setActive(true, false);
            landingRunways.put(runway.getName(), runway);
        }
        JSONArray takeoff = save.getJSONArray("takeoffRunways");
        for (int i = 0; i < takeoff.length(); i++) {
            Runway runway = runways.get(takeoff.getString(i));
            runway.setActive(runway.isLanding(), true);
            takeoffRunways.put(runway.getName(), runway);
        }
    }

    /** Loads the runway queue from save file separately after loading main airport data (since aircrafts have not been loaded during the main airport loading stage) */
    public void updateRunwayQueue(JSONObject save) {
        for (Runway runway: runways.values()) {
            Array<Aircraft> aircraftsOnAppr = new Array<Aircraft>();
            JSONArray queue = save.getJSONObject("runwayQueues").getJSONArray(runway.getName());
            for (int i = 0; i < queue.length(); i++) {
                aircraftsOnAppr.add(TerminalControl.radarScreen.aircrafts.get(queue.getString(i)));
            }
            runway.setAircraftsOnAppr(aircraftsOnAppr);
        }
    }

    /** Loads the necessary resources that cannot be loaded in constructor */
    public void loadOthers() {
        holdingPoints = FileLoader.loadHoldingPoints(this);
        missedApproaches = FileLoader.loadMissedInfo(this);
        approaches = FileLoader.loadILS(this);
        for (Runway runway: runways.values()) {
            runway.setIls(approaches.get(runway.getName()));
        }
        setOppRwys();
        stars = FileLoader.loadStars(this);
        sids = FileLoader.loadSids(this);
        RandomSID.loadSidNoise(icao);
        RandomSTAR.loadStarNoise(icao);

        //TerminalControl.tts.test(stars, sids);

        for (MissedApproach missedApproach: missedApproaches.values()) {
            missedApproach.loadIls();
        }

        takeoffManager = new TakeoffManager(this);
        runwayManager = new RunwayManager(this);

        approachZones = ZoneLoader.loadZones(icao);
        for (int i = 0; i < approachZones.size; i++) {
            approachZones.get(i).updateStatus(landingRunways);
        }
    }

    /** loadOthers from JSON save */
    public void loadOthers(JSONObject save) {
        loadOthers();

        for (Runway runway: runways.values()) {
            Array<Aircraft> queueArray = new Array<Aircraft>();
            JSONArray queue = save.getJSONObject("runwayQueues").getJSONArray(runway.getName());
            for (int i = 0; i < queue.length(); i++) {
                queueArray.add(TerminalControl.radarScreen.aircrafts.get(queue.getString(i)));
            }
        }

        takeoffManager = new TakeoffManager(this, save.getJSONObject("takeoffManager"));
    }

    /** Sets the opposite runway for each runway */
    private void setOppRwys() {
        for (Runway runway: runways.values()) {
            if (runway.getOppRwy() == null) {
                int oppNumber = Integer.parseInt(runway.getName().substring(0, 2)) + 18;
                if (oppNumber > 36) {
                    oppNumber -= 36;
                }
                String oppExtra;
                String extra = runway.getName().length() == 3 ? String.valueOf(runway.getName().charAt(2)) : "";
                if ("L".equals(extra)) {
                    oppExtra = "R";
                } else if ("R".equals(extra)) {
                    oppExtra = "L";
                } else {
                    oppExtra = extra;
                }
                String oppRwyStr = oppNumber + oppExtra;
                if (oppNumber < 10) {
                    oppRwyStr = "0" + oppRwyStr;
                }
                runways.get(oppRwyStr).setOppRwy(runway);
                runway.setOppRwy(runways.get(oppRwyStr));
            }
        }
    }

    /** Sets the runway's active state, and removes or adds it into hashMap of takeoff & landing runways */
    public void setActive(String rwy, boolean landing, boolean takeoff) {
        //Retrieves runway from hashtable
        Runway runway = runways.get(rwy);
        boolean ldgChange = false;
        boolean tkofChange = false;
        boolean ldgOff = false;
        boolean tkofOff = false;

        if (!runway.isLanding() && landing) {
            //Add to landing runways if not landing before, but landing now
            landingRunways.put(rwy, runway);
            ldgChange = true;
        } else if (runway.isLanding() && !landing) {
            //Remove if landing before, but not landing now
            landingRunways.remove(rwy);
            ldgOff = true;
        }
        if (!runway.isTakeoff() && takeoff) {
            //Add to takeoff runways if not taking off before, but taking off now
            takeoffRunways.put(rwy, runway);
            tkofChange = true;
        } else if (runway.isTakeoff() && !takeoff) {
            //Remove if taking off before, but not taking off now
            takeoffRunways.remove(rwy);
            tkofOff = true;
        }

        //Set runway's internal active state
        runway.setActive(landing, takeoff);

        //Message to inform user of runway change
        if (ldgChange || tkofChange) {
            String msg = "Runway " + rwy + " at " + icao + " is now active for ";
            if (ldgChange && tkofChange) {
                msg += "takeoffs and landings.";
            } else if (ldgChange) {
                msg += "landings.";
            } else {
                msg += "takeoffs.";
            }
            TerminalControl.radarScreen.getCommBox().normalMsg(msg);
            TerminalControl.radarScreen.soundManager.playRunwayChange();
        }

        if (ldgOff || tkofOff) {
            String msg = "Runway " + rwy + " at " + icao + " is no longer active for ";
            if (ldgOff && tkofOff) {
                msg += "takeoffs and landings.";
            } else if (ldgOff) {
                msg += "landings.";
            } else {
                msg += "takeoffs.";
            }
            TerminalControl.radarScreen.getCommBox().normalMsg(msg);
            TerminalControl.radarScreen.soundManager.playRunwayChange();
        }

        //Updates approach zone status
        for (int i = 0; i < approachZones.size; i++) {
            approachZones.get(i).updateStatus(landingRunways);
        }
    }

    public void renderRunways() {
        if (!TerminalControl.radarScreen.tutorial) {
            takeoffManager.update();
        }

        if (landings - airborne > 12) {
            if (!congested) TerminalControl.radarScreen.getCommBox().warningMsg(icao + " is experiencing congestion! To allow aircrafts on the ground to take off, reduce the number of arrivals into the airport by reducing speed or putting them in holding patterns.");
            congested = true;
        } else {
            congested = false;
        }

        for (Runway runway: runways.values()) {
            runway.setLabelColor(congested ? Color.ORANGE : Color.WHITE);
            runway.renderShape();
        }
    }

    public void renderApchZones() {
        for (int i = 0; i < approachZones.size; i++) {
            approachZones.get(i).renderShape();
        }
    }

    public void setMetar(JSONObject metar) {
        this.metar = metar.getJSONObject(getIcao());
        System.out.println("METAR of " + getIcao() + ": " + this.metar.toString());
        //Update active runways if METAR is updated
        int windHdg = this.metar.isNull("windDirection") ? 0 : this.metar.getInt("windDirection");
        ws = "";
        if (!this.metar.isNull("windshear")) {
            ws = this.metar.getString("windshear");
        } else {
            ws = "None";
        }
        runwayManager.updateRunways(windHdg, this.metar.getInt("windSpeed"));
        for (Runway runway: runways.values()) {
            runway.setWindshear(runway.isLanding() && ("ALL RWY".equals(ws) || ArrayUtils.contains(ws.split(" "), "R" + runway.getName())));
        }
    }

    public HashMap<String, Star> getStars() {
        return stars;
    }

    public HashMap<String, Sid> getSids() {
        return sids;
    }

    public HashMap<String, Runway> getLandingRunways() {
        return landingRunways;
    }

    public HashMap<String, Runway> getTakeoffRunways() {
        return takeoffRunways;
    }

    public int[] getWinds() {
        return metar.isNull("windDirection") ? new int[] {0, 0} : new int[] {metar.getInt("windDirection"), metar.getInt("windSpeed")};
    }

    public HashMap<String, Runway> getRunways() {
        return runways;
    }
    
    public String getIcao() {
        return icao;
    }

    public int getElevation() {
        return elevation;
    }

    public int getGusts() {
        if (metar.get("windGust") == JSONObject.NULL) {
            return -1;
        } else {
            return metar.getInt("windGust");
        }
    }

    public int getVisibility() {
        return metar.getInt("visibility");
    }

    public String getWindshear() {
        return ws;
    }

    public HashMap<String, ILS> getApproaches() {
        return approaches;
    }

    public HashMap<String, HoldingPoints> getHoldingPoints() {
        return holdingPoints;
    }

    public HashMap<String, MissedApproach> getMissedApproaches() {
        return missedApproaches;
    }

    public int getLandings() {
        return landings;
    }

    public void setLandings(int landings) {
        this.landings = landings;
    }

    public int getAirborne() {
        return airborne;
    }

    public void setAirborne(int airborne) {
        this.airborne = airborne;
    }

    public TakeoffManager getTakeoffManager() {
        return takeoffManager;
    }

    public int getAircraftRatio() {
        return aircraftRatio;
    }

    public HashMap<Integer, String> getAirlines() {
        return airlines;
    }

    public HashMap<String, String> getAircrafts() {
        return aircrafts;
    }

    public boolean isCongested() {
        return congested;
    }

    public JSONObject getMetar() {
        return metar;
    }

    public Array<ApproachZone> getApproachZones() {
        return approachZones;
    }
}
