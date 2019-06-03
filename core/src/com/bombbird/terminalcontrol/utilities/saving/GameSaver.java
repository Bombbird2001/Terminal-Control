package com.bombbird.terminalcontrol.utilities.saving;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameSaver {
    /** Saves current game state */
    public static void saveGame() {
        RadarScreen radarScreen = TerminalControl.radarScreen;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aircrafts", saveAircraft());
        jsonObject.put("airports", saveAirports());
        jsonObject.put("saveId", radarScreen.saveId);
        jsonObject.put("MAIN_NAME", radarScreen.mainName);
        jsonObject.put("AIRAC", radarScreen.airac);
        jsonObject.put("score", radarScreen.getScore());
        jsonObject.put("highScore", radarScreen.getHighScore());
        jsonObject.put("planesToControl", (double) radarScreen.getPlanesToControl());
        jsonObject.put("arrivals", radarScreen.getArrivals());
        jsonObject.put("spawnTimer", (double) radarScreen.getSpawnTimer());
        jsonObject.put("radarTime", (double) radarScreen.getRadarTime());
        jsonObject.put("trailTime", (double) radarScreen.getTrailTime());
        jsonObject.put("trajectoryLine", radarScreen.trajectoryLine);
        jsonObject.put("liveWeather", radarScreen.liveWeather);
        jsonObject.put("sounds", radarScreen.soundSel);
        jsonObject.put("arrivalManager", getArrivalManager());
        jsonObject.put("commBox", getCommBox());
        jsonObject.put("metar", radarScreen.getMetar().getMetarObject());
        jsonObject.put("lastNumber", radarScreen.separationChecker.getLastNumber());
        jsonObject.put("sepTime", (double) radarScreen.separationChecker.getTime());

        JSONArray jsonArray = new JSONArray();
        for (String aircraft: radarScreen.getAllAircraft().keySet()) {
            jsonArray.put(aircraft);
        }
        jsonObject.put("allAircraft", jsonArray);

        int aircraftsLanded = 0;
        int aircraftsAirborne = 0;
        for (Airport airport: radarScreen.airports.values()) {
            aircraftsLanded += airport.getLandings();
            aircraftsAirborne += airport.getAirborne();
        }

        jsonObject.put("landings", aircraftsLanded);
        jsonObject.put("airborne", aircraftsAirborne);

        FileHandle handle = null;
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            //If desktop, save to external roaming appData
            handle = Gdx.files.external(FileLoader.mainDir + "/saves/" + radarScreen.saveId + ".json");
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            //If Android, check first if local storage available
            if (Gdx.files.isLocalStorageAvailable()) {
                handle = Gdx.files.local("saves/" + radarScreen.saveId + ".json");
            } else {
                Gdx.app.log("Storage error", "Local storage unavailable for Android!");
            }
        }

        if (handle != null) {
            handle.writeString(Base64Coder.encodeString(jsonObject.toString()), false);
        }

        saveID(radarScreen.saveId);
    }

    /** Saves all aircraft information */
    private static JSONArray saveAircraft() {
        JSONArray aircrafts = new JSONArray();
        for (Aircraft aircraft: TerminalControl.radarScreen.aircrafts.values()) {
            JSONObject aircraftInfo = new JSONObject();
            String type;

            aircraftInfo.put("actionRequired", aircraft.isActionRequired()); //Whether aircraft label is flashing
            aircraftInfo.put("dataTagMin", aircraft.getDataTag().isMinimized()); //Whether aircraft label is minimized
            aircraftInfo.put("emergency", aircraft.isEmergency()); //Whether aircraft is having a 7700

            aircraftInfo.put("airport", aircraft.getAirport().getIcao()); //Airport
            aircraftInfo.put("runway", aircraft.getRunway() == null ? JSONObject.NULL : aircraft.getRunway().getName()); //Runway
            aircraftInfo.put("onGround", aircraft.isOnGround()); //Whether it is on the ground
            aircraftInfo.put("tkOfLdg", aircraft.isTkOfLdg()); //Whether it is taking off/landing

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
            aircraftInfo.put("terrainConflict", aircraft.isTerrainConflict()); //Aircraft in conflict with terrain

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
                    for (double info2: info) {
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
            for (Image image: aircraft.getDataTag().getTrailDots()) {
                JSONArray point = new JSONArray();
                point.put((double)(image.getX() + image.getWidth() / 2));
                point.put((double)(image.getY() + image.getHeight() / 2));
                trail.put(point);
            }
            aircraftInfo.put("trailDots", trail);

            aircraftInfo.put("prevAlt", (double) aircraft.getPrevAlt()); //Previous altitude
            aircraftInfo.put("altitude", (double) aircraft.getAltitude()); //Altitude
            aircraftInfo.put("clearedAltitude", aircraft.getClearedAltitude()); //Cleared altitude
            aircraftInfo.put("targetAltitude", aircraft.getTargetAltitude()); //Target altitude
            aircraftInfo.put("verticalSpeed", (double) aircraft.getVerticalSpeed()); //Vertical speed
            aircraftInfo.put("expedite", aircraft.isExpedite()); //Expedite
            aircraftInfo.put("expediteTime", (double) aircraft.getExpediteTime()); //Expedite time
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
            aircraftInfo.put("deltaPosition", deltaPosition);

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

            aircraftInfo.put("voice", aircraft.getVoice()); //Text to speech voice

            JSONArray labelPos = new JSONArray(); //Aircraft label position
            labelPos.put((double) aircraft.getDataTag().getLabelPosition()[0]);
            labelPos.put((double) aircraft.getDataTag().getLabelPosition()[1]);
            aircraftInfo.put("labelPos", labelPos);

            if (aircraft instanceof Arrival) {
                type = "Arrival";
                aircraftInfo.put("star", aircraft.getSidStar().getName());

                if (((Arrival) aircraft).getNonPrecAlts() != null) {
                    //Non prec alts for arrivals
                    JSONArray nonPrecAlts = new JSONArray();
                    for (float[] info : ((Arrival) aircraft).getNonPrecAlts()) {
                        JSONArray data = new JSONArray();
                        data.put((double) info[0]);
                        data.put((double) info[1]);
                        nonPrecAlts.put(data);
                    }
                    aircraftInfo.put("nonPrecAlts", nonPrecAlts);
                } else {
                    aircraftInfo.put("nonPrecAlts", JSONObject.NULL);
                }

                aircraftInfo.put("lowerSpdSet", ((Arrival) aircraft).isLowerSpdSet());
                aircraftInfo.put("ilsSpdSet", ((Arrival) aircraft).isIlsSpdSet());
                aircraftInfo.put("finalSpdSet", ((Arrival) aircraft).isFinalSpdSet());
                aircraftInfo.put("willGoAround", ((Arrival) aircraft).isWillGoAround());
                aircraftInfo.put("goAroundAlt", ((Arrival) aircraft).getGoAroundAlt());
                aircraftInfo.put("goAroundSet", ((Arrival) aircraft).isGoAroundSet());
                aircraftInfo.put("contactAlt", ((Arrival) aircraft).getContactAlt());
                aircraftInfo.put("fuel", (double) ((Arrival) aircraft).getFuel());
                aircraftInfo.put("requestPriority", ((Arrival) aircraft).isRequestPriority());
                aircraftInfo.put("declareEmergency", ((Arrival) aircraft).isDeclareEmergency());
                aircraftInfo.put("divert", ((Arrival) aircraft).isDivert());
            } else if (aircraft instanceof Departure) {
                type = "Departure";
                aircraftInfo.put("sid", aircraft.getSidStar().getName());
                aircraftInfo.put("outboundHdg", ((Departure) aircraft).getOutboundHdg());
                aircraftInfo.put("contactAlt", ((Departure) aircraft).getContactAlt());
                aircraftInfo.put("handOverAlt", ((Departure) aircraft).getHandoveralt());
                aircraftInfo.put("v2set", ((Departure) aircraft).isV2set());
                aircraftInfo.put("accel", ((Departure) aircraft).isAccel());
                aircraftInfo.put("sidSet", ((Departure) aircraft).isSidSet());
                aircraftInfo.put("contacted", ((Departure) aircraft).isContacted());
                aircraftInfo.put("cruiseAlt", ((Departure) aircraft).getCruiseAlt());
                aircraftInfo.put("higherSpdSet", ((Departure) aircraft).isHigherSpdSet());
                aircraftInfo.put("cruiseSpdSet", ((Departure) aircraft).isCruiseSpdSet());
            } else {
                type = "Type error";
                Gdx.app.log("Save error", "Invalid aircraft instance type");
            }
            aircraftInfo.put("TYPE", type);

            //Save aircraft route
            JSONObject route = new JSONObject();
            JSONArray wpts = new JSONArray();
            JSONArray restrictions = new JSONArray();
            JSONArray flyOver = new JSONArray();
            for (int i = 0; i < aircraft.getRoute().getWaypoints().size; i++) {
                wpts.put(aircraft.getRoute().getWaypoints().get(i).getName());
                int[] data = aircraft.getRoute().getRestrictions().get(i);
                String stuff = data[0] + " " + data[1] + " " + data[2];
                restrictions.put(stuff);
                flyOver.put(aircraft.getRoute().getFlyOver().get(i));
            }
            route.put("waypoints", wpts);
            route.put("restrictions", restrictions);
            route.put("flyOver", flyOver);
            aircraftInfo.put("route", route);

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
            timeQueue.put((double) time);
        }
        navState.put("timeQueue", timeQueue);

        //Add display lat mode queue
        navState.put("dispLatMode", getStringArray(aircraft.getNavState().getDispLatMode()));

        //Add display alt mode queue
        navState.put("dispAltMode", getStringArray(aircraft.getNavState().getDispAltMode()));

        //Add display spd mode queue
        navState.put("dispSpdMode", getStringArray(aircraft.getNavState().getDispSpdMode()));

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
            airportInfo.put("elevation", airport.getElevation()); //Elevation of airport
            airportInfo.put("aircraftRatio", airport.getAircraftRatio()); //Ratio of flights to the airport
            airportInfo.put("takeoffManager", getTakeoffManager(airport)); //Takeoff manager

            //Queue for runway landings
            JSONObject runwayQueues = new JSONObject();
            for (Runway runway: airport.getRunways().values()) {
                JSONArray queue = new JSONArray();
                for (Aircraft aircraft: runway.getAircraftsOnAppr()) {
                    queue.put(aircraft.getCallsign());
                }
                runwayQueues.put(runway.getName(), queue);
            }
            airportInfo.put("runwayQueues", runwayQueues);

            airportInfo.put("landings", airport.getLandings()); //Landings
            airportInfo.put("airborne", airport.getAirborne()); //Airborne
            airportInfo.put("congestion", airport.isCongested()); //Congestion

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
            if (airport.getTakeoffManager().getNextAircraft().get(rwy) == null) airport.getTakeoffManager().update();
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
            timers.put(rwy, (double) airport.getTakeoffManager().getTimers().get(rwy));
        }
        takeoffManager.put("timers", timers);

        return takeoffManager;
    }

    /** Returns the arrival manager as JSONObject */
    private static JSONObject getArrivalManager() {
        JSONObject arrivalManager = new JSONObject();

        for (String waypoint: TerminalControl.radarScreen.getArrivalManager().getEntryPoint().keySet()) {
            arrivalManager.put(waypoint, TerminalControl.radarScreen.getArrivalManager().getEntryPoint().get(waypoint) == null ? JSONObject.NULL : TerminalControl.radarScreen.getArrivalManager().getEntryPoint().get(waypoint).getCallsign());
        }

        return arrivalManager;
    }

    /** Saves the input ID into saves.saves file */
    private static void saveID(int id) {
        FileHandle handle;
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            handle = Gdx.files.local("saves/saves.saves");
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            handle = Gdx.files.external(FileLoader.mainDir + "/saves/saves.saves");
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
            ids.removeValue("", false); //Remove a random "" that is loaded into the array
        } else {
            ids = new Array<String>();
            ids.add(Integer.toString(id));
        }

        handle.writeString(ids.toString(","), false);
    }

    private static JSONArray getCommBox() {
        JSONArray jsonArray = new JSONArray();
        for (Label label: TerminalControl.radarScreen.getCommBox().getLabels()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", label.getText());
            jsonObject.put("color", label.getStyle().fontColor.toString());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    /** Deletes a save given input game ID */
    public static void deleteSave(int id) {
        FileHandle handle;
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            handle = Gdx.files.local("saves/saves.saves");
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            handle = Gdx.files.external(FileLoader.mainDir + "/saves/saves.saves");
        } else {
            handle = Gdx.files.local("saves/saves.saves");
            Gdx.app.log("File load error", "Unknown platform " + Gdx.app.getType().name() + " used!");
        }

        String[] saveIDs = handle.readString().split(",");
        Array<Integer> ids = new Array<Integer>();
        for (String stringID: saveIDs) {
            ids.add(Integer.parseInt(stringID));
        }

        if (!ids.removeValue(id, false)) {
            Gdx.app.log("Delete error", "Save ID " + id + " not found in saves.saves");
        }
        handle.writeString(ids.toString(","), false);

        handle = handle.sibling(id + ".json");
        if (!handle.delete()) {
            Gdx.app.log("Delete error", id + ".json not found");
        }
    }
}
