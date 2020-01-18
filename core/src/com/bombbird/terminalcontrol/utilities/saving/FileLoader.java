package com.bombbird.terminalcontrol.utilities.saving;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle;
import com.bombbird.terminalcontrol.entities.procedures.HoldingPoints;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.obstacles.PolygonObstacle;
import com.bombbird.terminalcontrol.entities.obstacles.CircleObstacle;
import com.bombbird.terminalcontrol.utilities.ErrorHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FileLoader {
    public static String mainDir;

    public static Array<Obstacle> loadObstacles() {
        FileHandle obstacles = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/obstacle.obs");
        Array<Obstacle> obsArray = new Array<>();
        String[] indivObs = obstacles.readString().split("\\r?\\n");
        for (String s: indivObs) {
            if ("".equals(s)) break;
            if (s.charAt(0) == '#') continue;
            //For each individual obstacle:
            PolygonObstacle obs = new PolygonObstacle(s);
            obsArray.add(obs);
            TerminalControl.radarScreen.stage.addActor(obs);
        }

        FileHandle restrictions = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/restricted.restr");
        String[] indivRests = restrictions.readString().split("\\r?\\n");
        for (String s: indivRests) {
            if ("".equals(s)) break;
            if (s.charAt(0) == '#') continue;
            //For each individual restricted area
            CircleObstacle area = new CircleObstacle(s);
            obsArray.add(area);
            TerminalControl.radarScreen.stage.addActor(area);
        }
        return obsArray;
    }

    public static HashMap<String, Waypoint> loadWaypoints() {
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/waypoint.way");
        String wayptStr = handle.readString();
        String[] indivWpt = wayptStr.split("\\r?\\n");
        HashMap <String, Waypoint> waypoints = new HashMap<>(indivWpt.length + 1, 0.999f);
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
        HashMap<String, Runway> runways = new HashMap<>();
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
        HashMap<String, Star> stars = new HashMap<>();
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/star" + airport.getIcao() + ".star");
        JSONObject jo = new JSONObject(handle.readString());
        for (String name: jo.keySet()) {
            //For each individual STAR
            Star star = new Star(airport, jo.getJSONObject(name));
            star.setName(name);
            stars.put(name, star);
        }
        return stars;
    }

    public static HashMap<String, Sid> loadSids(Airport airport) {
        //Load SIDs
        HashMap<String, Sid> sids = new HashMap<>();
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/sid" + airport.getIcao() + ".sid");
        JSONObject jo = new JSONObject(handle.readString());
        for (String name: jo.keySet()) {
            //For each individual SID
            Sid sid = new Sid(airport, jo.getJSONObject(name));
            sid.setName(name);
            sids.put(name, sid);
        }
        return sids;
    }

    public static HashMap<String, int[]> loadAircraftData() {
        HashMap<String, int[]> aircrafts = new HashMap<>();
        FileHandle handle = Gdx.files.internal("game/aircrafts/aircrafts.air");
        String[] indivAircrafts = handle.readString().split("\\r?\\n");
        for (String s: indivAircrafts) {
            if (s.charAt(0) == '#') {
                continue;
            }
            //For each aircraft
            int index = 0;
            String icao = "";
            int[] perfData = new int[6];
            for (String s1: s.split(",")) {
                if (index == 0) {
                    icao = s1; //Icao code of aircraft
                } else if (index >= 2 && index <= 5) {
                    perfData[index - 1] = Integer.parseInt(s1);
                } else if (index == 1 || index == 6) {
                    //Wake turb cat, recat
                    char cat = s1.charAt(0);
                    perfData[index - 1] = cat;
                    if (index == 1 && !(cat == 'M' || cat == 'H' || cat == 'J')) Gdx.app.log("Wake cat error", "Unknown category for " + icao);
                    if (index == 6 && !(cat >= 'A' && cat <= 'F')) Gdx.app.log("Recat error", "Unknown category for " + icao);
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
        HashMap<String, ILS> approaches = new HashMap<>();
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

    public static HashMap<String, HoldingPoints> loadHoldingPoints(Airport airport) {
        HashMap<String, HoldingPoints> holdingPoints = new HashMap<>();
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/hold" + airport.getIcao() + ".hold");
        JSONObject jo = new JSONObject(handle.readString());
        for (String point: jo.keySet()) {
            holdingPoints.put(point, new HoldingPoints(point, jo.getJSONObject(point)));
        }

        return holdingPoints;
    }

    public static HashMap<String, MissedApproach> loadMissedInfo(Airport airport) {
        HashMap<String, MissedApproach> missedApproaches = new HashMap<>();
        FileHandle handle = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/missedApch" + airport.getIcao() + ".miss");
        JSONObject jo = new JSONObject(handle.readString());
        for (String missed: jo.keySet()) {
            missedApproaches.put(missed, new MissedApproach(missed, airport, jo.getJSONObject(missed)));
        }
        return missedApproaches;
    }

    public static JSONArray loadSaves() {
        JSONArray saves = new JSONArray();

        FileHandle handle = getExtDir("saves/saves.saves");

        if (handle != null && handle.exists()) {
            Array<String> saveIds = new Array<>(handle.readString().split(","));
            if ("".equals(saveIds.get(0))) {
                //"" returned if saves.saves is empty
                return saves;
            }
            for (String id: saveIds) {
                FileHandle handle1 = handle.sibling(id + ".json");
                if (!handle1.exists()) {
                    GameSaver.deleteSave(Integer.parseInt(id));
                    continue;
                }
                String saveString = handle1.readString();
                if (saveString.length() == 0) {
                    GameSaver.deleteSave(Integer.parseInt(id));
                    continue;
                }
                try {
                    saveString = Base64Coder.decodeString(saveString);
                    JSONObject save = new JSONObject(saveString);
                    saves.put(save);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ErrorHandler.sendSaveErrorNoThrow(e, saveString);
                    TerminalControl.toastManager.jsonParseFail();
                    Gdx.app.log("Corrupted save", "JSON parse failure");
                    if (GameSaver.deleteSave(Integer.parseInt(id))) Gdx.app.log("Save deleted", "Corrupted save deleted");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    ErrorHandler.sendSaveErrorNoThrow(e, saveString);
                    TerminalControl.toastManager.jsonParseFail();
                    Gdx.app.log("Corrupted save", "Base64 decode failure");
                    if (GameSaver.deleteSave(Integer.parseInt(id))) Gdx.app.log("Save deleted", "Corrupted save deleted");
                }
            }
        }

        return saves;
    }

    public static boolean checkIfSaveExists() {
        FileHandle handle = getExtDir("saves/saves.saves");
        if (handle != null && handle.exists()) {
            Array<String> saveIds = new Array<>(handle.readString().split(","));
            //"" returned if saves.saves is empty
            return !"".equals(saveIds.get(0));
        }
        return false;
    }

    public static HashMap<Integer, String> loadAirlines(String icao) {
        HashMap<Integer, String> airlines = new HashMap<>();
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
        HashMap<String, String> aircrafts = new HashMap<>();
        String[] info = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/airlines" + icao + ".airl").readString().split("\\r?\\n");
        for (String indivAirline: info) {
            aircrafts.put(indivAirline.split(",")[0], indivAirline.split(",")[2]);
        }

        return aircrafts;
    }

    public static HashMap<String, String> loadIcaoCallsigns() {
        HashMap<String, String> callsigns = new HashMap<>();
        String[] info = Gdx.files.internal("game/aircrafts/callsigns.call").readString().split("\\r?\\n");
        for (String indivAirline: info) {
            callsigns.put(indivAirline.split(",")[0], indivAirline.split(",")[1]);
        }
        return callsigns;
    }

    public static HashMap<String, Boolean> loadNoise(String icao, boolean sid) {
        HashMap<String, Boolean> noise = new HashMap<>();
        String fileName = sid ? "/noiseSid" : "/noiseStar";
        JSONObject jo = new JSONObject(Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + fileName + icao + ".noi").readString());
        JSONArray nightArray = jo.getJSONArray("night");
        for (int i = 0; i < nightArray.length(); i++) {
            noise.put(nightArray.getString(i), true);
        }
        JSONArray dayArray = jo.getJSONArray("day");
        for (int i = 0; i < dayArray.length(); i++) {
            noise.put(dayArray.getString(i), false);
        }

        return noise;
    }

    public static Array<Array<Integer>> loadShoreline() {
        Array<Array<Integer>> shoreline = new Array<>();
        String[] info = Gdx.files.internal("game/" + TerminalControl.radarScreen.mainName + "/" + TerminalControl.radarScreen.airac + "/shoreline.shore").readString().split("\\r?\\n");
        if ("".equals(info[0])) return shoreline;

        Array<Integer> landmass = new Array<>();
        for (String line: info) {
            if (line.charAt(0) == '\"') {
                if (landmass.size > 0) {
                    shoreline.add(landmass);
                    landmass = new Array<>();
                }
            } else {
                String[] data = line.split(" ");
                landmass.add(Integer.parseInt(data[0])); //x coordinate
                landmass.add(Integer.parseInt(data[1])); //y coordinate
            }
        }

        if (landmass.size > 0) shoreline.add(landmass); //Add the last landmass

        return shoreline;
    }

    public static JSONObject loadSettings() {
        JSONObject settings = null;
        try {
            FileHandle handle = getExtDir("settings.json");
            if (handle != null && handle.exists()) {
                settings = new JSONObject(handle.readString());
            }
        } catch (JSONException e) {
            Gdx.app.log("Corrupted settings", "JSON parse failure");
        }

        return settings;
    }

    public static FileHandle getExtDir(String path) {
        FileHandle handle = null;
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            //If desktop, save to external roaming appData
            if (!TerminalControl.full) {
                handle = Gdx.files.external("AppData/Roaming/TerminalControl/" + path);
            } else {
                handle = Gdx.files.external("AppData/Roaming/TerminalControlFull/" + path);
            }
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            //If Android, check first if local storage available
            if (Gdx.files.isLocalStorageAvailable()) {
                handle = Gdx.files.local(path);
            } else {
                Gdx.app.log("Storage error", "Local storage unavailable for Android!");
                TerminalControl.toastManager.readStorageFail();
            }
        } else {
            Gdx.app.log("Storage error", "Unknown platform!");
        }
        return handle;
    }
}
