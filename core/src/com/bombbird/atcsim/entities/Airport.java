package com.bombbird.atcsim.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import static com.bombbird.atcsim.screens.GameScreen.stage;

import java.util.Enumeration;
import java.util.Hashtable;

public class Airport {
    private Hashtable<String, Runway> runways;
    private Hashtable<String, Runway> landingRunways;
    private Hashtable<String, Runway> takeoffRunways;
    private String icao;
    private String metar;
    private Hashtable<String, SidStar> stars;
    private Hashtable<String, SidStar> sids;

    public Airport(String icao) {
        this.icao = icao;
        loadRunways();
    }

    private void loadRunways() {
        runways = new Hashtable<String, Runway>();
        FileHandle handle = Gdx.files.internal("game/" + icao + "/runway" + icao + ".rwy");
        String[] indivRwys = handle.readString().split("\\r?\\n");
        for (String s: indivRwys) {
            //For each individual runway
            String rwyInfo[] = s.split(" ");
            int index = 0;
            String name = "";
            float x = 0;
            float y = 0;
            int length = 0;
            int heading = 0;
            float textX = 0;
            float textY = 0;
            for (String s1: rwyInfo) {
                switch (index) {
                    case 0: name = s1; break;
                    case 1: x = Float.parseFloat(s1); break;
                    case 2: y = Float.parseFloat(s1); break;
                    case 3: length = Integer.parseInt(s1); break;
                    case 4: heading = Integer.parseInt(s1); break;
                    case 5: textX = Float.parseFloat(s1);
                    case 6: textY = Float.parseFloat(s1); break;
                }
                index++;
            }
            Runway runway = new Runway(name, x, y, length, heading, textX, textY);
            runways.put(name, runway);
        }
        landingRunways = new Hashtable<String, Runway>();
        takeoffRunways = new Hashtable<String, Runway>();

        setActive("05L", true, false);
        setActive("05R", false, true);
    }

    private void setActive(String rwy, boolean landing, boolean takeoff) {
        Runway runway = runways.get(rwy);
        if ((landing || takeoff) && !runway.isActive()) {
            stage.addActor(runway);
        } else if (!landing && !takeoff && runway.isActive()) {
            stage.getActors().removeValue(runway, true);
        }
        runway.setActive(landing, takeoff);
        if (landing) landingRunways.put(rwy, runway);
        if (takeoff) takeoffRunways.put(rwy, runway);
    }

    public void renderRunways() {
        Enumeration<String> enumKeys = runways.keys();
        while (enumKeys.hasMoreElements()) {
            String key = enumKeys.nextElement();
            Runway runway = runways.get(key);
            if (runway.isActive()) {
                runway.renderShape();
            }
        }
    }

    public void loadSidStars() {

    }
}
