package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.procedures.HoldProcedure;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;

import java.util.HashMap;

public class FileLoader {
    public static Array<Obstacle> loadObstacles() {
        FileHandle obstacles = Gdx.files.internal("game/" + RadarScreen.mainName + "/obstacle.obs");
        Array<Obstacle> obsArray = new Array<Obstacle>();
        String[] indivObs = obstacles.readString().split("\\r?\\n");
        for (String s: indivObs) {
            //For each individual obstacle:
            Obstacle obs = new Obstacle(s);
            obsArray.add(obs);
            GameScreen.stage.addActor(obs);
        }
        return obsArray;
    }

    public static Array<RestrictedArea> loadRestricted() {
        FileHandle restrictions = Gdx.files.internal("game/" + RadarScreen.mainName + "/restricted.rest");
        Array<RestrictedArea> restArray = new Array<RestrictedArea>();
        String[] indivRests = restrictions.readString().split("\\r?\\n");
        for (String s: indivRests) {
            //For each individual restricted area
            RestrictedArea area = new RestrictedArea(s);
            restArray.add(area);
            GameScreen.stage.addActor(area);
        }
        return restArray;
    }

    public static HashMap<String, Waypoint> loadWaypoints() {
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/waypoint.way");
        String wayptStr = handle.readString();
        String[] indivWpt = wayptStr.split("\\r?\\n");
        HashMap <String, Waypoint> waypoints = new HashMap<String, Waypoint>(indivWpt.length + 1, 0.999f);
        for (String s: indivWpt) {
            //For each waypoint
            int index = 0;
            String name = "";
            int x = 0;
            int y = 0;
            for (String s1: s.split(" ")) {
                switch (index) {
                    case 0: name = s1; break;
                    case 1: x = Integer.parseInt(s1); break;
                    case 2: y = Integer.parseInt(s1); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + RadarScreen.mainName + "/restricted.rest");
                }
                index++;
            }
            Waypoint waypoint = new Waypoint(name, x, y);
            waypoints.put(name, waypoint);
            GameScreen.stage.addActor(waypoint);
        }
        return waypoints;
    }

    public static HashMap<String, Runway> loadRunways(String icao) {
        HashMap<String, Runway> runways = new HashMap<String, Runway>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/runway" + icao + ".rwy");
        String[] indivRwys = handle.readString().split("\\r?\\n");
        for (String s: indivRwys) {
            //For each individual runway
            Runway runway = new Runway(s);
            runways.put(runway.getName(), runway);
        }
        return runways;
    }

    public static HashMap<String, Star> loadStars(Airport airport) {
        //Load STARs
        HashMap<String, Star> stars = new HashMap<String, Star>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/star" + airport.getIcao() + ".star");
        String[] indivStars = handle.readString().split("\\r?\\n");
        for (String s: indivStars) {
            //For each individual STAR
            Star star = new Star(airport, s);
            stars.put(star.getName(), star);
        }
        return stars;
    }

    public static HashMap<String, Sid> loadSids(Airport airport) {
        //Load SIDs
        HashMap<String, Sid> sids = new HashMap<String, Sid>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/sid" + airport.getIcao() + ".sid");
        String[] indivStars = handle.readString().split("\\r?\\n");
        for (String s: indivStars) {
            //For each individual SID
            Sid sid = new Sid(airport, s);
            sids.put(sid.getName(), sid);
        }
        return sids;
    }

    public static HashMap<String, int[]> loadAircraftData() {
        HashMap<String, int[]> aircrafts = new HashMap<String, int[]>();
        FileHandle handle = Gdx.files.internal("game/aircrafts/aircrafts.air");
        String[] indivAircrafts = handle.readString().split("\\r?\\n");
        for (String s: indivAircrafts) {
            if (s.charAt(0) == '#') {
                continue;
            }
            //For each aircraft
            int index = 0;
            String icao = "";
            int[] perfData = new int[5];
            for (String s1: s.split(",")) {
                if (index == 0) {
                    icao = s1; //Icao code of aircraft
                } else if (index >= 1 && index <= 5) {
                    perfData[index - 1] = Integer.parseInt(s1);
                } else {
                    Gdx.app.log("Load error", "Unexpected additional parameter in game/aircrafts.air");
                }
                index++;
            }
            aircrafts.put(icao, perfData);
        }
        return aircrafts;
    }

    public static HashMap<String, ILS> loadILS(Airport airport) {
        HashMap<String, ILS> approaches = new HashMap<String, ILS>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/ils" + airport.getIcao() + ".ils");
        String[] indivApches = handle.readString().split("\\r?\\n");
        for (String s: indivApches) {
            //For each approach
            if (s.split(",").length == 8) {
                ILS ils = new ILS(airport, s);
                approaches.put(ils.getRwy().getName(), ils);
            } else {
                LDA lda = new LDA(airport, s);
                approaches.put(lda.getRwy().getName(), lda);
            }
        }
        return approaches;
    }

    public static HashMap<String, HoldProcedure> loadHoldInfo(Airport airport) {
        HashMap<String, HoldProcedure> holdProcedures = new HashMap<String, HoldProcedure>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/hold" + airport.getIcao() + ".hold");
        String[] indivHold = handle.readString().split("\\r?\\n");
        for (String hold: indivHold) {
            boolean nameSet = false;
            String name = "";
            String toParse = "";
            for (String info: hold.split(":")) {
                if (!nameSet) {
                    name = info;
                    nameSet = true;
                } else {
                    toParse = info;
                }
            }
            holdProcedures.put(name, new HoldProcedure(name, airport, toParse));
        }
        return holdProcedures;
    }

    public static HashMap<String, MissedApproach> loadMissedInfo(Airport airport) {
        HashMap<String, MissedApproach> missedApproaches = new HashMap<String, MissedApproach>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/missedApch" + airport.getIcao() + ".miss");
        String[] indivMissed = handle.readString().split("\\r?\\n");
        for (String missed: indivMissed) {
            boolean nameSet = false;
            String name = "";
            String toParse = "";
            for (String info: missed.split(":")) {
                if (!nameSet) {
                    name = info;
                    nameSet = true;
                } else {
                    toParse = info;
                }
            }
            missedApproaches.put(name, new MissedApproach(name, airport, toParse));
        }
        return missedApproaches;
    }
}
