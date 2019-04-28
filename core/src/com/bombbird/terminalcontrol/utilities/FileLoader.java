package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.procedures.HoldProcedure;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class FileLoader {
    public static final String mainDir = "full".equals(Gdx.files.internal("game/type.type").readString()) ? "AppData/Roaming/TerminalControlFull" : "AppData/Roaming/TerminalControl";

    public static Array<Obstacle> loadObstacles() {
        FileHandle obstacles = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/obstacle.obs");
        Array<Obstacle> obsArray = new Array<Obstacle>();
        String[] indivObs = obstacles.readString().split("\\r?\\n");
        for (String s: indivObs) {
            if ("".equals(s)) break;
            if (s.charAt(0) == '#') continue;
            //For each individual obstacle:
            Obstacle obs = new Obstacle(s);
            obsArray.add(obs);
            TerminalControl.radarScreen.stage.addActor(obs);
        }
        return obsArray;
    }

    public static Array<RestrictedArea> loadRestricted() {
        FileHandle restrictions = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/restricted.restr");
        Array<RestrictedArea> restArray = new Array<RestrictedArea>();
        String[] indivRests = restrictions.readString().split("\\r?\\n");
        for (String s: indivRests) {
            if ("".equals(s)) break;
            if (s.charAt(0) == '#') continue;
            //For each individual restricted area
            RestrictedArea area = new RestrictedArea(s);
            restArray.add(area);
            TerminalControl.radarScreen.stage.addActor(area);
        }
        return restArray;
    }

    public static HashMap<String, Waypoint> loadWaypoints() {
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/waypoint.way");
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
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/restricted.rest");
                }
                index++;
            }
            Waypoint waypoint = new Waypoint(name, x, y);
            waypoints.put(name, waypoint);
            TerminalControl.radarScreen.stage.addActor(waypoint);
        }
        return waypoints;
    }

    public static HashMap<String, Runway> loadRunways(String icao) {
        HashMap<String, Runway> runways = new HashMap<String, Runway>();
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/runway" + icao + ".rwy");
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
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/star" + airport.getIcao() + ".star");
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
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/sid" + airport.getIcao() + ".sid");
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
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/ils" + airport.getIcao() + ".ils");
        String[] indivApches = handle.readString().split("\\r?\\n");
        for (String s: indivApches) {
            //For each approach
            if (s.contains("ILS")) {
                ILS ils = new ILS(airport, s);
                approaches.put(ils.getRwy().getName(), ils);
            } else if (s.contains("LDA")) {
                LDA lda = new LDA(airport, s);
                approaches.put(lda.getRwy().getName(), lda);
            } else {
                Gdx.app.log("ILS error", "Invalid approach type specified for " + s);
            }
        }
        return approaches;
    }

    public static HashMap<String, HoldProcedure> loadHoldInfo(Airport airport) {
        HashMap<String, HoldProcedure> holdProcedures = new HashMap<String, HoldProcedure>();
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/hold" + airport.getIcao() + ".hold");
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
            holdProcedures.put(name, new HoldProcedure(airport, toParse));
        }
        return holdProcedures;
    }

    public static HashMap<String, MissedApproach> loadMissedInfo(Airport airport) {
        HashMap<String, MissedApproach> missedApproaches = new HashMap<String, MissedApproach>();
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/missedApch" + airport.getIcao() + ".miss");
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

    public static JSONArray loadSaves() {
        JSONArray saves = new JSONArray();

        FileHandle handle = null;
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            //If desktop, save to external roaming appData
            String type = Gdx.files.internal("game/type.type").readString();
            if ("lite".equals(type)) {
                handle = Gdx.files.external("AppData/Roaming/TerminalControl/saves/saves.saves");
            } else if ("full".equals(type)) {
                handle = Gdx.files.external("AppData/Roaming/TerminalControlFull/saves/saves.saves");
            } else {
                Gdx.app.log("Invalid game type", "Invalid game type " + type + ".");
                handle = Gdx.files.external("AppData/Roaming/TerminalControl/saves/saves.saves");
            }
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            //If Android, check first if local storage available
            if (Gdx.files.isLocalStorageAvailable()) {
                handle = Gdx.files.local("saves/saves.saves");
            } else {
                Gdx.app.log("Storage error", "Local storage unavailable for Android!");
            }
        }

        if (handle != null && handle.exists()) {
            String[] saveIds = handle.readString().split(",");
            if ("".equals(saveIds[0])) {
                //"" returned if saves.saves is empty
                return saves;
            }
            for (String id: saveIds) {
                String saveString = handle.sibling(id + ".json").readString();
                if (saveString.charAt(0) != '{') saveString = Base64Coder.decodeString(saveString);
                JSONObject save = new JSONObject(saveString);
                saves.put(save);
            }
        }

        return saves;
    }

    public static HashMap<Integer, String> loadAirlines(String icao) {
        HashMap<Integer, String> airlines = new HashMap<Integer, String>();
        String[] info = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/airlines" + icao + ".airl").readString().split("\\r?\\n");
        int counter = 0;
        for (String indivAirline: info) {
            String callsign = indivAirline.split(",")[0];
            int times = Integer.parseInt(indivAirline.split(",")[1]);
            for (int i = 0; i < times; i++) {
                airlines.put(counter, callsign);
                counter++;
            }
        }

        return airlines;
    }

    public static HashMap<String, String> loadAirlineAircrafts(String icao) {
        HashMap<String, String> aircrafts = new HashMap<String, String>();
        String[] info = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/airlines" + icao + ".airl").readString().split("\\r?\\n");
        for (String indivAirline: info) {
            aircrafts.put(indivAirline.split(",")[0], indivAirline.split(",")[2]);
        }

        return aircrafts;
    }

    public static HashMap<String, String> loadIcaoCallsigns() {
        HashMap<String, String> callsigns = new HashMap<String, String>();
        String[] info = Gdx.files.internal("game/aircrafts/callsigns.call").readString().split("\\r?\\n");
        for (String indivAirline: info) {
            callsigns.put(indivAirline.split(",")[0], indivAirline.split(",")[1]);
        }
        return callsigns;
    }

    public static HashMap<String, int[][]> loadSidNoise(String icao, boolean sid) {
        HashMap<String, int[][]> noise = new HashMap<String, int[][]>();
        String fileName = sid ? "/noiseSid" : "/noiseStar";
        String read = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + fileName + icao + ".noi").readString();
        if ("".equals(read)) return noise;
        String[] info = read.split("\\r?\\n");
        for (String s: info) {
            String[] data = s.split(":");
            String[] times = data[1].split(">");
            int[][] timeInfo = new int[times.length][2];
            for (int i = 0; i < times.length; i++) {
                String[] minMaxTime = times[i].split("-");
                int[] time = new int[2];
                time[0] = Integer.parseInt(minMaxTime[0]);
                time[1] = Integer.parseInt(minMaxTime[1]);
                timeInfo[i] = time;
            }
            noise.put(data[0], timeInfo);
        }
        return noise;
    }

    public static Image loadShoreline(String icao) {
        return new Image(new Texture(Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/shorelineDone.png")));
    }
}
