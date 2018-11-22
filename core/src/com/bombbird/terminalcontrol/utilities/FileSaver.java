package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileSaver {
    /** Saves current game state */
    public static void saveGame() {
        RadarScreen radarScreen = TerminalControl.radarScreen;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aircrafts", saveAircraft());
        jsonObject.put("airports", saveAirports());
        jsonObject.put("SAVE_ID", radarScreen.saveId);
        jsonObject.put("MAIN_NAME", radarScreen.mainName);
        jsonObject.put("AIRAC", radarScreen.airac);
        jsonObject.put("score", radarScreen.getScore());
        jsonObject.put("highScore", radarScreen.getHighScore());
        jsonObject.put("planesToControl", (double) radarScreen.getPlanesToControl());
        jsonObject.put("arrivals", radarScreen.getArrivals());
        jsonObject.put("radarTime", (double) radarScreen.getRadarTime());
        jsonObject.put("trailTime", (double) radarScreen.getTrailTime());
        jsonObject.put("arrivalManager", getArrivalManager());

        FileHandle handle = null;
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            //If desktop, save to external roaming appData
            handle = Gdx.files.external("AppData/Roaming/TerminalControl/saves/" + radarScreen.saveId + ".json");
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            //If Android, check first if local storage available
            if (Gdx.files.isLocalStorageAvailable()) {
                handle = Gdx.files.local("saves/Test.json");
            } else {
                Gdx.app.log("Storage error", "Local storage unavailable for Android!");
            }
        }

        if (handle != null) {
            handle.writeString(jsonObject.toString(), false);
        }

        saveID(radarScreen.saveId);
    }

    /** Saves all aircraft information */
    private static JSONArray saveAircraft() {
        JSONArray aircrafts = new JSONArray();
        for (Aircraft aircraft: TerminalControl.radarScreen.aircrafts.values()) {
            JSONObject aircraftInfo = new JSONObject();
            String type;
            if (aircraft instanceof Arrival) {
                type = "Arrival";
            } else if (aircraft instanceof Departure) {
                type = "Departure";
            } else {
                type = "Type error";
                Gdx.app.log("Save error", "Invalid aircraft instance type");
            }
            aircraftInfo.put("TYPE", type);
            aircraftInfo.put("airport", aircraft.getAirport().getIcao()); //Airport
            aircraftInfo.put("runway", aircraft.getRunway() == null ? JSONObject.NULL : aircraft.getRunway().getName()); //Runway
            aircraftInfo.put("onGround", aircraft.isOnGround()); //Whether it is on the ground
            aircraftInfo.put("tkOfLdg", aircraft.isTkofLdg()); //Whether it is taking off/landing

            aircraftInfo.put("callsign", aircraft.getCallsign()); //Callsign
            aircraftInfo.put("icaoType", aircraft.getIcaoType()); //ICAO aircraft type
            aircraftInfo.put("wakeCat", String.valueOf(aircraft.getWakeCat())); //Wake turbulence category
            aircraftInfo.put("v2", aircraft.getV2()); //V2 speed
            aircraftInfo.put("typClimb", aircraft.getTypClimb()); //Typical climb rate
            aircraftInfo.put("maxClimb", aircraft.getMaxClimb()); //Max climb rate
            aircraftInfo.put("typDes", aircraft.getTypDes()); //Typical descent rate
            aircraftInfo.put("maxDes", aircraft.getMaxDes()); //Max descent rate
            aircraftInfo.put("apchSpd", aircraft.getApchSpd()); //Approach speed
            aircraftInfo.put("controlState", aircraft.getControlState()); //Control state
            aircraftInfo.put("navState", getNavState(aircraft)); //Nav state
            aircraftInfo.put("goAround", aircraft.isGoAround()); //Go around
            aircraftInfo.put("goAroundWindow", aircraft.isGoAroundWindow()); //Go around window is active
            aircraftInfo.put("goAroundTime", (double) aircraft.getGoAroundTime()); //Go around window timing
            aircraftInfo.put("conflict", aircraft.isConflict()); //Aircraft is in conflict
            aircraftInfo.put("warning", aircraft.isWarning()); //Aircraft is in warning state

            aircraftInfo.put("x", (double) aircraft.getX()); //x coordinate
            aircraftInfo.put("y", (double) aircraft.getY()); //y coordinate
            aircraftInfo.put("heading", aircraft.getHeading()); //Heading
            aircraftInfo.put("targetHeading", aircraft.getTargetHeading()); //Target heading
            aircraftInfo.put("clearedHeading", aircraft.getClearedHeading()); //Cleared heading
            aircraftInfo.put("angularVelocity", aircraft.getAngularVelocity()); //Angular velocity
            aircraftInfo.put("track", aircraft.getTrack()); //Track
            aircraftInfo.put("sidStarIndex", aircraft.getSidStarIndex()); //Sid star index
            aircraftInfo.put("direct", aircraft.getDirect() == null ? JSONObject.NULL : aircraft.getDirect().getName()); //Direct waypoint
            aircraftInfo.put("afterWaypoint", aircraft.getAfterWaypoint() == null ? JSONObject.NULL : aircraft.getAfterWaypoint().getName()); //After direct waypoint
            aircraftInfo.put("afterWptHdg", aircraft.getAfterWptHdg()); //After waypoint heading
            aircraftInfo.put("ils", aircraft.getIls() == null ? JSONObject.NULL : aircraft.getIls().getName()); //ILS
            aircraftInfo.put("locCap", aircraft.isLocCap()); //Localizer captured
            aircraftInfo.put("holdWpt", aircraft.getHoldWpt() == null ? JSONObject.NULL : aircraft.getHoldWpt().getName()); //Holding point
            aircraftInfo.put("holding", aircraft.isHolding()); //Aircraft holding
            aircraftInfo.put("init", aircraft.isInit()); //Additional holding info
            aircraftInfo.put("type1leg", aircraft.isType1leg()); //Additional holding info

            //More additional holding info
            if (aircraft.getHoldTargetPt() != null) {
                JSONArray info0 = new JSONArray();
                JSONArray info1 = new JSONArray();
                int index = 0;
                for (float[] info: aircraft.getHoldTargetPt()) {
                    JSONArray point = new JSONArray();
                    for (float info2: info) {
                        point.put(info2);
                    }
                    info0.put(point);
                    info1.put(aircraft.getHoldTargetPtSelected()[index]);
                    index++;
                }
                aircraftInfo.put("holdTargetPt", info0);
                aircraftInfo.put("holdTargetPtSelected", info1);
            } else {
                aircraftInfo.put("holdTargetPt", JSONObject.NULL);
                aircraftInfo.put("holdTargetPtSelected", JSONObject.NULL);
            }

            //Trail dots
            JSONArray trail = new JSONArray();
            for (Image image: aircraft.getTrailDots()) {
                JSONArray point = new JSONArray();
                point.put((double) (image.getX() + image.getWidth() / 2));
                point.put((double) (image.getY() + image.getHeight() / 2));
                trail.put(point);
            }
            aircraftInfo.put("trailDots", trail);

            aircraftInfo.put("prevAlt", (double) aircraft.getPrevAlt()); //Previous altitude
            aircraftInfo.put("altitude", (double) aircraft.getAltitude()); //Altitude
            aircraftInfo.put("clearedAltitude", aircraft.getClearedAltitude()); //Cleared altitude
            aircraftInfo.put("targetAltitude", aircraft.getTargetAltitude()); //Target altitude
            aircraftInfo.put("verticalSpeed", (double) aircraft.getVerticalSpeed()); //Vertical speed
            aircraftInfo.put("expedite", aircraft.isExpedite()); //Expedite
            aircraftInfo.put("lowestAlt", aircraft.getLowestAlt()); //Lowest altitude
            aircraftInfo.put("highestAlt", aircraft.getHighestAlt()); //Highest altitude
            aircraftInfo.put("gsCap", aircraft.isGsCap()); //Glide slope captured

            aircraftInfo.put("ias", (double) aircraft.getIas()); //Airspeed
            aircraftInfo.put("tas", (double) aircraft.getTas()); //True airspeed
            aircraftInfo.put("gs", (double) aircraft.getGs()); //Ground speed

            //Delta position
            JSONArray deltaPosition = new JSONArray();
            deltaPosition.put((double) aircraft.getDeltaPosition().x);
            deltaPosition.put((double) aircraft.getDeltaPosition().y);
            aircraftInfo.put("deltaPosition",deltaPosition);

            aircraftInfo.put("clearedIas", aircraft.getClearedIas()); //Cleared speed
            aircraftInfo.put("deltaIas", (double) aircraft.getDeltaIas()); //Rate of change of speed
            aircraftInfo.put("climbSpd", aircraft.getClimbSpd()); //Climb speed

            aircraftInfo.put("radarX", (double) aircraft.getRadarX()); //Radar x position
            aircraftInfo.put("radarY", (double) aircraft.getRadarY()); //Radar y position
            aircraftInfo.put("radarHdg", aircraft.getRadarHdg()); //Radar heading
            aircraftInfo.put("radarTrack", aircraft.getRadarTrack()); //Radar track
            aircraftInfo.put("radarGs", (double) aircraft.getRadarGs()); //Radar ground speed
            aircraftInfo.put("radarAlt", (double) aircraft.getRadarAlt()); //Radar altitude
            aircraftInfo.put("radarVs", (double) aircraft.getRadarVs()); //Radar vertical speed

            aircrafts.put(aircraftInfo);
        }
        return aircrafts;
    }

    /** Saves all navState information for input aircraft */
    private static JSONObject getNavState(Aircraft aircraft) {
        JSONObject navState = new JSONObject();

        //Add all allowed lat modes
        JSONArray latModes = new JSONArray();
        for (String string: aircraft.getNavState().getLatModes()) {
            latModes.put(string);
        }
        navState.put("latModes", latModes);

        //Add all allowed alt modes
        JSONArray altModes = new JSONArray();
        for (String string: aircraft.getNavState().getAltModes()) {
            altModes.put(string);
        }
        navState.put("altModes", altModes);

        //Add all allowed spd modes
        JSONArray spdModes = new JSONArray();
        for (String string: aircraft.getNavState().getSpdModes()) {
            spdModes.put(string);
        }
        navState.put("spdModes", spdModes);

        //Add transmit timings
        JSONArray timeQueue = new JSONArray();
        for (float time: aircraft.getNavState().getTimeQueue()) {
            timeQueue.put(time);
        }
        navState.put("timeQueue", timeQueue);

        //Add display lat mode queue
        navState.put("dispLatMode", getStringArray(aircraft.getNavState().getDispLatMode()));

        //Add display alt mode queue
        navState.put("dispAltMode", getStringArray(aircraft.getNavState().getDispAltMode()));

        //Add display spd mode queue

        navState.put("dispLatMode", getStringArray(aircraft.getNavState().getDispSpdMode()));

        //Add cleared heading
        navState.put("clearedHdg", getIntArray(aircraft.getNavState().getClearedHdg()));

        //Add cleared direct
        navState.put("clearedDirect", getWptArray(aircraft.getNavState().getClearedDirect()));

        //Add cleared after waypoint
        navState.put("clearedAftWpt", getWptArray(aircraft.getNavState().getClearedAftWpt()));

        //Add cleared after waypoint heading
        navState.put("clearedAftWptHdg", getIntArray(aircraft.getNavState().getClearedAftWptHdg()));

        //Add cleared hold waypoint
        navState.put("clearedHold", getWptArray(aircraft.getNavState().getClearedHold()));

        //Add cleared ILS
        JSONArray clearedIls = new JSONArray();
        for (ILS ils: aircraft.getNavState().getClearedIls()) {
            clearedIls.put(ils == null ? JSONObject.NULL : ils.getName());
        }
        navState.put("clearedIls", clearedIls);

        //Add cleared altitude
        navState.put("clearedAlt", getIntArray(aircraft.getNavState().getClearedAlt()));

        //Add cleared expedite
        navState.put("clearedExpedite", getBoolArray(aircraft.getNavState().getClearedExpedite()));

        //Add cleared speed
        navState.put("clearedSpd", getIntArray(aircraft.getNavState().getClearedSpd()));

        //Add go around state
        navState.put("goAround", getBoolArray(aircraft.getNavState().getGoAround()));

        //Save length
        navState.put("length", aircraft.getNavState().getLength());

        return navState;
    }

    /** Returns a JSONArray given an input queue of strings */
    private static JSONArray getStringArray(Queue<String> queue) {
        JSONArray array = new JSONArray();
        for (String string: queue) {
            array.put(string);
        }
        return array;
    }

    /** Returns a JSONArray given an input queue of integers */
    private static JSONArray getIntArray(Queue<Integer> queue) {
        JSONArray array = new JSONArray();
        for (int integer: queue) {
            array.put(integer);
        }
        return array;
    }

    /** Returns a JSONArray given an input queue of waypoints */
    private static JSONArray getWptArray(Queue<Waypoint> queue) {
        JSONArray array = new JSONArray();
        for (Waypoint waypoint: queue) {
            array.put(waypoint == null ? JSONObject.NULL : waypoint.getName());
        }
        return array;
    }

    /** Returns a JSONArray given an input queue of booleans */
    private static JSONArray getBoolArray(Queue<Boolean> queue) {
        JSONArray array = new JSONArray();
        for (boolean bool: queue) {
            array.put(bool);
        }
        return array;
    }

    /** Saves current information for all airports */
    private static JSONArray saveAirports() {
        JSONArray airports = new JSONArray();
        for (Airport airport: TerminalControl.radarScreen.airports.values()) {
            JSONObject airportInfo = new JSONObject();

            //Landing runways
            JSONArray landingRunways = new JSONArray();
            for (Runway runway: airport.getLandingRunways().values()) {
                landingRunways.put(runway.getName());
            }
            airportInfo.put("landingRunways", landingRunways);

            //Takeoff runways
            JSONArray takeoffRunways = new JSONArray();
            for (Runway runway: airport.getTakeoffRunways().values()) {
                takeoffRunways.put(runway.getName());
            }
            airportInfo.put("takeoffRunways", takeoffRunways);

            airportInfo.put("icao", airport.getIcao()); //ICAO code of airport
            airportInfo.put("takeoffManager", getTakeoffManager(airport)); //Takeoff manager
            airportInfo.put("landings", airport.getLandings()); //Landings
            airportInfo.put("airborne", airport.getAirborne()); //Airborne

            airports.put(airportInfo);
        }
        return airports;
    }

    /** Returns the airport's takeoff manager as JSONObject */
    private static JSONObject getTakeoffManager(Airport airport) {
        JSONObject takeoffManager = new JSONObject();

        //Save nextAircraft for each runway
        JSONObject nextAircraft = new JSONObject();
        for (String rwy: airport.getTakeoffManager().getNextAircraft().keySet()) {
            JSONArray aircraftInfo = new JSONArray();
            for (String s: airport.getTakeoffManager().getNextAircraft().get(rwy)) {
                aircraftInfo.put(s);
            }
            nextAircraft.put(rwy, aircraftInfo);
        }
        takeoffManager.put("nextAircraft", nextAircraft);

        //Save prevAircraft for each runway
        JSONObject prevAircraft = new JSONObject();
        for (String rwy: airport.getTakeoffManager().getPrevAircraft().keySet()) {
            prevAircraft.put(rwy, airport.getTakeoffManager().getPrevAircraft().get(rwy) == null ? JSONObject.NULL : airport.getTakeoffManager().getPrevAircraft().get(rwy).getCallsign());
        }
        takeoffManager.put("prevAircraft", prevAircraft);

        //Save timers for each runway
        JSONObject timers = new JSONObject();
        for (String rwy: airport.getTakeoffManager().getTimers().keySet()) {
            timers.put(rwy, airport.getTakeoffManager().getTimers().get(rwy));
        }
        takeoffManager.put("timers", timers);

        return takeoffManager;
    }

    /** Returns the arrival manager as JSONObject */
    private static JSONObject getArrivalManager() {
        JSONObject arrivalManager = new JSONObject();

        for (Waypoint waypoint: TerminalControl.radarScreen.getArrivalManager().getEntryPoint().keySet()) {
            arrivalManager.put(waypoint.getName(), TerminalControl.radarScreen.getArrivalManager().getEntryPoint().get(waypoint) == null ? JSONObject.NULL : TerminalControl.radarScreen.getArrivalManager().getEntryPoint().get(waypoint).getCallsign());
        }

        return arrivalManager;
    }

    /** Saves the input ID into saves.saves file */
    private static void saveID(int id) {
        FileHandle handle;
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            handle = Gdx.files.local("saves/saves.saves");
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            handle = Gdx.files.external("AppData/Roaming/TerminalControl/saves/saves.saves");
        } else {
            handle = Gdx.files.local("saves/saves.saves");
            Gdx.app.log("File load error", "Unknown platform " + Gdx.app.getType().name() + " used!");
        }

        Array<String> ids = null;
        if (handle.exists()) {
            ids = new Array<String>(handle.readString().split(","));
        }
        if (ids != null) {
            if (!ids.contains(Integer.toString(id), false)) {
                //If does not contain, add to array
                ids.add(Integer.toString(id));
            } else {
                return; //If contains ID, no action required
            }
        } else {
            ids = new Array<String>();
            ids.add(Integer.toString(id));
        }

        handle.writeString(ids.toString(","), false);
    }
}
