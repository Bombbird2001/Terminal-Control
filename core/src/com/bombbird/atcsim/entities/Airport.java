package com.bombbird.atcsim.entities;

import com.bombbird.atcsim.entities.sidstar.Sid;
import com.bombbird.atcsim.entities.sidstar.Star;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.utilities.FileLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

import java.util.HashMap;

public class Airport {
    private HashMap<String, Runway> runways;
    private HashMap<String, Runway> landingRunways;
    private HashMap<String, Runway> takeoffRunways;
    private String icao;
    private JSONObject metar;
    private HashMap<String, Star> stars;
    private HashMap<String, Sid> sids;
    private int elevation;

    public Airport(String icao, int elevation) {
        this.icao = icao;
        this.elevation = elevation;
        runways = FileLoader.loadRunways(icao);
        landingRunways = new HashMap<String, Runway>();
        takeoffRunways = new HashMap<String, Runway>();
        stars = FileLoader.loadStars(icao);
        sids = FileLoader.loadSids(icao);
        if (icao.equals("RCTP")) {
            setActive("05L", true, false);
            setActive("05R", true, true);
        } else if (icao.equals("RCSS")) {
            setActive("10", true, true);
        }
    }

    private void setActive(String rwy, boolean landing, boolean takeoff) {
        //Retrieves runway from hashtable
        Runway runway = runways.get(rwy);

        //Set actor
        if ((landing || takeoff) && !runway.isActive()) {
            //Add if active now but not before
            GameScreen.stage.addActor(runway);
        } else if (!landing && !takeoff && runway.isActive()) {
            //Remove if not active but active before
            GameScreen.stage.getActors().removeValue(runway, true);
        }

        if (!runway.isLanding() && landing) {
            //Add to landing runways if not landing before, but landing now
            landingRunways.put(rwy, runway);
        } else if (runway.isLanding() && !landing) {
            //Remove if landing before, but not landing now
            landingRunways.remove(rwy);
        }
        if (!runway.isTakeoff() && takeoff) {
            //Add to takeoff runways if not taking off before, but taking off now
            takeoffRunways.put(rwy, runway);
        } else if (runway.isTakeoff() && !takeoff) {
            //Remove if taking off before, but not taking off now
            takeoffRunways.remove(rwy);
        }

        //Set runway's internal active state
        runway.setActive(landing, takeoff);
    }

    public void renderRunways() {
        for (Runway runway: runways.values()) {
            if (runway.isActive()) {
                runway.renderShape();
            }
        }
    }

    void setMetar(JSONObject metar) {
        this.metar = metar.getJSONObject(getIcao());
        System.out.println("METAR of " + getIcao() + ": " + this.metar.toString());
        //Update active runways if METAR is updated
        int windHdg = this.metar.getInt("windDirection");
        String ws = "";
        if (this.metar.get("windshear") != JSONObject.NULL) {
            ws = this.metar.getString("windshear");
        }
        if (windHdg != 0) { //Update runways if winds are not variable
            for (Runway runway : runways.values()) {
                int rightHeading = runway.getHeading() + 90;
                int leftHeading = runway.getHeading() - 90;
                if (rightHeading > 360) {
                    rightHeading -= 360;
                    if (windHdg >= rightHeading && windHdg < leftHeading) {
                        setActive(runway.getName(), false, false);
                    } else {
                        setActive(runway.getName(), true, true);
                    }
                } else if (leftHeading < 0) {
                    leftHeading += 360;
                    if (windHdg >= rightHeading && windHdg < leftHeading) {
                        setActive(runway.getName(), false, false);
                    } else {
                        setActive(runway.getName(), true, true);
                    }
                } else {
                    if (windHdg >= leftHeading && windHdg < rightHeading) {
                        setActive(runway.getName(), true, true);
                    } else {
                        setActive(runway.getName(), false, false);
                    }
                }
                runway.setWindshear(ws.equals("ALL RWY") || ArrayUtils.contains(ws.split(" "), "R" + runway.getName()));
            }
        }
    }

    public JSONObject getMetar() {
        return metar;
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
        return new int[] {getMetar().getInt("windDirection"), getMetar().getInt("windSpeed")};
    }

    public HashMap<String, Runway> getRunways() {
        return runways;
    }

    public void setRunways(HashMap<String, Runway> runways) {
        this.runways = runways;
    }

    public void setLandingRunways(HashMap<String, Runway> landingRunways) {
        this.landingRunways = landingRunways;
    }

    public void setTakeoffRunways(HashMap<String, Runway> takeoffRunways) {
        this.takeoffRunways = takeoffRunways;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public void setStars(HashMap<String, Star> stars) {
        this.stars = stars;
    }

    public void setSids(HashMap<String, Sid> sids) {
        this.sids = sids;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }
}
