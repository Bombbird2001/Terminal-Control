package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.sidstar.Route;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.utilities.Revision;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class NavState {
    //Update available mode codes
    public static final int REMOVE_ALL_SIDSTAR = 0; //Removes all SID/STAR choices - SID, STAR, after waypoint fly heading, hold at
    public static final int REMOVE_AFTERHDG_HOLD = 1; //Removes after waypoint fly heading, hold at
    public static final int REMOVE_SIDSTAR_ONLY = 2; //Removes only the SID, STAR mode
    public static final int REMOVE_SIDSTAR_AFTERHDG = 3; //Removes SID, STAR, after waypoint fly heading
    public static final int REMOVE_HOLD_ONLY = 4; //Removes only hold at
    public static final int REMOVE_AFTERHDG_ONLY = 5; //Removes only after waypoint fly heading
    public static final int ADD_ALL_SIDSTAR = 6; //Adds all SID/STAR choices - SID, STAR, after waypoint fly heading, hold at

    public static final int REMOVE_SIDSTAR_RESTR = 10; //Removes SID/STAR alt/speed restrictions
    public static final int ADD_SIDSTAR_RESTR_UNRESTR = 11; //Adds all modes
    public static final int REMOVE_UNRESTR = 12; //Removes unrestricted mode
    public static final int ADD_UNRESTR_ONLY = 13; //Adds unrestricted mode only
    public static final int ADD_SIDSTAR_RESTR_ONLY = 14; //Adds SID/STAR restrictions only

    //NavState codes
    public static final int UNKNOWN_STATE = -1;
    //Lateral modes
    public static final int SID_STAR = 20;
    public static final int AFTER_WAYPOINT_FLY_HEADING = 21;
    public static final int FLY_HEADING = 22;
    public static final int NO_DIRECTION = 27;
    public static final int HOLD_AT = 25;
    public static final int CHANGE_STAR = 26;

    //Turn directions - separate from lateral mode
    public static final int TURN_LEFT = 23;
    public static final int TURN_RIGHT = 24;

    //Altitude/Speed modes
    public static final int SID_STAR_RESTR = 30;
    public static final int NO_RESTR = 31;

    //Expedite - separate from altitude mode
    public static final int EXPEDITE = 32;

    private Aircraft aircraft;

    private final Array<String> latModes;
    private final Array<String> altModes;
    private final Array<String> spdModes;

    private final Array<Float> timeQueueArray;

    //Modes used for display
    private final Queue<Integer> dispLatMode;
    private final Queue<Integer> dispAltMode;
    private final Queue<Integer> dispSpdMode;

    private final Queue<Integer> clearedHdg;
    private final Queue<Waypoint> clearedDirect;
    private final Queue<Waypoint> clearedAftWpt;
    private final Queue<Integer> clearedAftWptHdg;
    private final Queue<Waypoint> clearedHold;
    private final Queue<ILS> clearedIls;
    private final Queue<String> clearedNewStar;
    private final Queue<Integer> clearedTurnDir;

    private final Queue<Integer> clearedAlt;
    private final Queue<Boolean> clearedExpedite;

    private Queue<Integer> clearedSpd;

    private final Queue<Boolean> goAround;

    private int length = 1;

    private final RadarScreen radarScreen;

    public NavState(Aircraft aircraft) {
        radarScreen = TerminalControl.radarScreen;

        this.aircraft = aircraft;
        altModes = new Array<>(5);
        spdModes = new Array<>(3);

        if (aircraft instanceof Arrival) {
            //Arrival
            latModes = new Array<>(6);
            latModes.add(aircraft.getSidStar().getName() + " arrival", Ui.AFTER_WPT_FLY_HDG, Ui.FLY_HEADING);
            latModes.add(Ui.HOLD_AT);
            if (!radarScreen.tutorial) latModes.add(Ui.CHANGE_STAR);

            altModes.add(Ui.DESCEND_VIA_STAR);

            spdModes.add(Ui.STAR_SPD_RESTRICTIONS);
        } else if (aircraft instanceof Departure) {
            //Departure
            latModes = new Array<>(4);
            latModes.add(aircraft.getSidStar().getName() + " departure", Ui.FLY_HEADING);

            altModes.add(Ui.CLIMB_VIA_SID);

            spdModes.add(Ui.SID_SPD_RESTRICTIONS);
        } else {
            //Nani
            Gdx.app.log("Navstate type error", "Unknown navstate type specified!");
            latModes = new Array<>(1);
        }
        altModes.add(Ui.CLIMB_DESCEND_TO);

        spdModes.add(Ui.NO_SPD_RESTRICTIONS);

        dispLatMode = new Queue<>();
        dispLatMode.addLast(SID_STAR);
        dispAltMode = new Queue<>();
        dispAltMode.addLast(SID_STAR_RESTR);
        dispSpdMode = new Queue<>();
        dispSpdMode.addLast(SID_STAR_RESTR);

        timeQueueArray = new Array<>();

        clearedHdg = new Queue<>();
        clearedHdg.addLast(aircraft.getClearedHeading());
        clearedDirect = new Queue<>();
        clearedDirect.addLast(aircraft.getDirect());
        clearedAftWpt = new Queue<>();
        clearedAftWpt.addLast(aircraft.getAfterWaypoint());
        clearedAftWptHdg = new Queue<>();
        clearedAftWptHdg.addLast(aircraft.getAfterWptHdg());
        clearedHold = new Queue<>();
        clearedHold.addFirst(null);
        clearedIls = new Queue<>();
        clearedIls.addLast(null);
        clearedNewStar = new Queue<>();
        clearedNewStar.addLast(null);
        clearedTurnDir = new Queue<>();
        clearedTurnDir.addLast(NO_DIRECTION);

        clearedAlt = new Queue<>();
        clearedAlt.addLast(aircraft.getClearedAltitude());
        clearedExpedite = new Queue<>();
        clearedExpedite.addLast(aircraft.isExpedite());

        clearedSpd = new Queue<>();
        if (aircraft instanceof Departure) {
            clearedSpd.addLast(aircraft.getV2());
        } else {
            clearedSpd.addLast(aircraft.getClimbSpd());
        }

        goAround = new Queue<>();
        goAround.addLast(false);
    }

    public NavState(Aircraft aircraft, JSONObject save) {
        radarScreen = TerminalControl.radarScreen;
        this.aircraft = aircraft;

        latModes = new Array<>();
        altModes = new Array<>();
        spdModes = new Array<>();

        timeQueueArray = new Array<>();

        dispLatMode = new Queue<>();
        dispAltMode = new Queue<>();
        dispSpdMode = new Queue<>();

        clearedHdg = new Queue<>();
        clearedDirect = new Queue<>();
        clearedAftWpt = new Queue<>();
        clearedAftWptHdg = new Queue<>();
        clearedHold = new Queue<>();
        clearedIls = new Queue<>();
        clearedNewStar = new Queue<>();
        clearedTurnDir = new Queue<>();

        clearedAlt = new Queue<>();
        clearedExpedite = new Queue<>();

        clearedSpd = new Queue<>();

        goAround = new Queue<>();

        length = save.getInt("length");

        {
            JSONArray array = save.getJSONArray("latModes");
            for (int i = 0; i < array.length(); i++) {
                latModes.add(array.getString(i));
            }
            if (aircraft instanceof Arrival && !latModes.contains(Ui.CHANGE_STAR, false)) {
                latModes.add(Ui.CHANGE_STAR);
            }
        }

        {
            JSONArray array = save.getJSONArray("altModes");
            for (int i = 0; i < array.length(); i++) {
                String mode = array.getString(i);
                if (Ui.EXPEDITE_TO.equals(mode)) {
                    altModes.add(Ui.CLIMB_DESCEND_TO);
                } else altModes.add(array.getString(i));
            }
        }

        {
            JSONArray array = save.getJSONArray("spdModes");
            for (int i = 0; i < array.length(); i++) {
                spdModes.add(array.getString(i));
            }
        }

        {
            JSONArray array = save.getJSONArray("timeQueue");
            for (int i = 0; i < array.length(); i++) {
                timeQueueArray.add((float) array.getDouble(i));
            }
        }



        if (radarScreen.getRevision() < Revision.NAVSTATE_REVISION) {
            //Old navstate format, parse to fit
            addToQueueIntParseFromString(save.getJSONArray("dispLatMode"), dispLatMode);
            addToQueueIntParseFromString(save.getJSONArray("dispAltMode"), dispAltMode);
            addToQueueIntParseFromString(save.getJSONArray("dispSpdMode"), dispSpdMode);
        } else {
            //New format
            addToQueueInt(save.getJSONArray("dispLatMode"), dispLatMode);
            addToQueueInt(save.getJSONArray("dispAltMode"), dispAltMode);
            addToQueueInt(save.getJSONArray("dispSpdMode"), dispSpdMode);
        }
        replaceAllTurnHdgModes(); //Replace any old turn left/right modes to fly heading mode
        addToQueueInt(save.getJSONArray("clearedHdg"), clearedHdg);
        addToQueueWpt(save.getJSONArray("clearedDirect"), clearedDirect);
        addToQueueWpt(save.getJSONArray("clearedAftWpt"), clearedAftWpt);
        addToQueueInt(save.getJSONArray("clearedAftWptHdg"), clearedAftWptHdg);
        addToQueueWpt(save.getJSONArray("clearedHold"), clearedHold);

        {
            JSONArray array = save.getJSONArray("clearedIls");
            for (int i = 0; i < array.length(); i++) {
                clearedIls.addLast(array.isNull(i) ? null : aircraft.getAirport().getApproaches().get(array.getString(i).substring(3)));
            }
        }

        if (save.isNull("newStar")) {
            clearedNewStar.addLast(null);
            fillUpString(clearedNewStar);
        } else {
            addToQueueString(save.getJSONArray("clearedNewStar"), clearedNewStar);
        }

        if (save.isNull("clearedTurnDir")) {
            clearedTurnDir.addLast(NO_DIRECTION);
            fillUpInt(clearedTurnDir);
        } else {
            addToQueueInt(save.getJSONArray("clearedTurnDir"), clearedTurnDir);
        }

        addToQueueInt(save.getJSONArray("clearedAlt"), clearedAlt);
        addToQueueBool(save.getJSONArray("clearedExpedite"), clearedExpedite);
        addToQueueInt(save.getJSONArray("clearedSpd"), clearedSpd);
        addToQueueBool(save.getJSONArray("goAround"), goAround);
    }

    /** Adds all elements in string array to string queue */
    private void addToQueueIntParseFromString(JSONArray array, Queue<Integer> queue) {
        for (int i = 0; i < array.length(); i++) {
            queue.addLast(array.isNull(i) ? -1 : getCodeFromString(array.getString(i)));
        }
    }

    /** Adds all elements in string array to string queue */
    private void addToQueueString(JSONArray array, Queue<String> queue) {
        for (int i = 0; i < array.length(); i++) {
            queue.addLast(array.isNull(i) ? null : array.getString(i));
        }
    }

    /** Adds all elements in int array to int queue */
    private void addToQueueInt(JSONArray array, Queue<Integer> queue) {
        for (int i = 0; i < array.length(); i++) {
            queue.addLast(array.isNull(i) ? -1 : array.getInt(i));
            if (queue.last() != -1 && queue.last() == EXPEDITE) {
                queue.removeLast();
                queue.addLast(NO_RESTR);
            }
        }
    }

    /** Adds all elements in bool array to bool queue */
    private void addToQueueBool(JSONArray array, Queue<Boolean> queue) {
        for (int i = 0; i < array.length(); i++) {
            queue.addLast(array.isNull(i) ? null : array.getBoolean(i));
        }
    }

    /** Adds all elements in wpt array to wpt queue */
    private void addToQueueWpt(JSONArray array, Queue<Waypoint> queue) {
        for (int i = 0; i < array.length(); i++) {
            queue.addLast(array.isNull(i) ? null : radarScreen.waypoints.get(array.getString(i)));
        }
    }

    /** Adds the time delay to keep track of when to send instructions */
    public void updateState() {
        float min = 2;
        float max = 4;
        if (timeQueueArray.size > 1) {
            min = Math.max(min, timeQueueArray.peek() + 0.1f); //Ensure instructions are always executed in order - last instruction will always be carried out last
            if (min > max) max = min + 0.1f; //If min time required exceeds max time, then make max time 0.1s longer than min time
        }
        timeQueueArray.add(MathUtils.random(min, max));
    }

    /** When called updates the aircraft's intentions (i.e. after reaction time has passed) */
    private void sendInstructions() {
        if (!goAround.get(1) && aircraft.isGoAround()) {
            //Do not send inputs if aircraft went around during delay
            dispLatMode.removeIndex(1);
            dispAltMode.removeIndex(1);
            dispSpdMode.removeIndex(1);

            clearedHdg.removeIndex(1);
            clearedDirect.removeIndex(1);
            clearedAftWpt.removeIndex(1);
            clearedAftWptHdg.removeIndex(1);
            clearedHold.removeIndex(1);
            clearedIls.removeIndex(1);
            clearedNewStar.removeIndex(1);
            clearedTurnDir.removeIndex(1);

            clearedAlt.removeIndex(1);
            clearedExpedite.removeIndex(1);

            clearedSpd.removeIndex(1);

            goAround.removeIndex(1);
        } else {
            validateInputs();

            if (dispLatMode.size > 1) dispLatMode.removeFirst();
            if (dispAltMode.size > 1) dispAltMode.removeFirst();
            if (dispSpdMode.size > 1) dispSpdMode.removeFirst();

            if (clearedHdg.size > 1) clearedHdg.removeFirst();
            if (clearedDirect.size > 1) clearedDirect.removeFirst();
            if (clearedAftWpt.size > 1) clearedAftWpt.removeFirst();
            if (clearedAftWptHdg.size > 1) clearedAftWptHdg.removeFirst();
            if (clearedHold.size > 1) clearedHold.removeFirst();
            if (clearedIls.size > 1) clearedIls.removeFirst();
            if (clearedNewStar.size > 1) clearedNewStar.removeFirst();
            if (clearedTurnDir.size > 1) clearedTurnDir.removeFirst();

            if (clearedAlt.size > 1) clearedAlt.removeFirst();
            if (clearedExpedite.size > 1) clearedExpedite.removeFirst();

            if (clearedSpd.size > 1) clearedSpd.removeFirst();

            if (goAround.size > 1) goAround.removeFirst();

            updateAircraftInfo();
        }
        if (length > 1) length--;
    }

    /** Sets the direct aircraft navigation states */
    public void updateAircraftInfo() {
        if (aircraft instanceof Arrival || (aircraft instanceof Departure && ((Departure) aircraft).isSidSet())) {
            aircraft.setClearedHeading(clearedHdg.first());
        }

        aircraft.setDirect(clearedDirect.first());
        aircraft.setSidStarIndex(aircraft.getRoute().findWptIndex(aircraft.getDirect() == null ? null : aircraft.getDirect().getName()));
        aircraft.setAfterWaypoint(clearedAftWpt.first());
        aircraft.setAfterWptHdg(clearedAftWptHdg.first());
        aircraft.setHoldWpt(clearedHold.first());
        aircraft.setIls(clearedIls.first());
        if (clearedIls.first() != null && dispLatMode.first() == NavState.SID_STAR) {
            int lowestAlt = aircraft.getRoute().getWptMinAlt(aircraft.getRoute().getWaypoints().size - 1);
            if (lowestAlt == -1) lowestAlt = radarScreen.minAlt;
            aircraft.setClearedAltitude(lowestAlt);
            replaceAllClearedAlt();
        } else {
            aircraft.setClearedAltitude(clearedAlt.first());
        }
        aircraft.setExpedite(clearedExpedite.first());

        if (aircraft instanceof Arrival || (aircraft instanceof Departure && ((Departure) aircraft).isSidSet())) {
            aircraft.setClearedIas(clearedSpd.first());
        }

        if (aircraft instanceof Arrival && clearedNewStar.first() != null) {
            if (!aircraft.isLocCap()) {
                Star newStar = aircraft.getAirport().getStars().get(clearedNewStar.first().split(" ")[0]);
                ((Arrival) aircraft).setStar(newStar);
                if (!dispLatMode.isEmpty() && !containsCode(dispLatMode.first(), FLY_HEADING)) {
                    dispLatMode.removeFirst();
                    dispLatMode.addFirst(FLY_HEADING);
                }
                aircraft.setRoute(new Route(aircraft, newStar));
                aircraft.setDirect(null);
                aircraft.setAfterWaypoint(null);
                aircraft.setAfterWptHdg(aircraft.getClearedHeading());
                aircraft.setSidStarIndex(0);
                clearedNewStar.removeFirst();
                clearedNewStar.addFirst(null);
                updateLatModes(ADD_ALL_SIDSTAR, false);
                updateLatModes(REMOVE_AFTERHDG_HOLD, true);
                radarScreen.getCommBox().alertMsg("The STAR for " + aircraft.getCallsign() + " has been changed to " + newStar.getName() + ". You may clear the aircraft to a waypoint on the new STAR.");
            } else {
                clearedNewStar.removeFirst();
                clearedNewStar.addFirst(null);
                radarScreen.getCommBox().alertMsg("The STAR for " + aircraft.getCallsign() + " cannot be changed now.");
            }
        }
    }

    /** Called before updating aircraft mode to ensure inputs are valid in case aircraft state changes during the pilot delay*/
    private void validateInputs() {
        if (dispLatMode.size < 2 || clearedDirect.size < 2 || clearedAftWptHdg.size < 2 || clearedHdg.size < 2) return;
        int currentDispLatMode = dispLatMode.first();
        int clearedDispLatMode = dispLatMode.get(1);
        Waypoint currentDirect = clearedDirect.first();
        Waypoint newDirect = clearedDirect.get(1);
        if (containsCode(currentDispLatMode, FLY_HEADING) && containsCode(clearedDispLatMode, HOLD_AT, AFTER_WAYPOINT_FLY_HEADING)) {
            //Case 1: Aircraft changed from after waypoint fly heading, to heading mode during delay: Remove hold at, after waypoint fly heading
            dispLatMode.removeFirst();
            dispLatMode.removeFirst();
            dispLatMode.addFirst(currentDispLatMode);
            dispLatMode.addFirst(currentDispLatMode);

            //Updates cleared heading to aftWptHdg to ensure aircraft flies the new requested after waypoint heading
            int initHdg = clearedHdg.removeFirst();
            clearedHdg.removeFirst();
            clearedHdg.addFirst(clearedAftWptHdg.get(1));
            clearedHdg.addFirst(initHdg);

            replaceAllClearedAltMode();
        } else if (newDirect != null && currentDirect != null && currentDispLatMode == SID_STAR && clearedNewStar.get(1) == null && aircraft.getRoute().findWptIndex(newDirect.getName()) < aircraft.getRoute().findWptIndex(currentDirect.getName())) {
            //Case 2: Aircraft direct changes during delay: Replace cleared direct if it is before new direct (if changing STARs, do not replace)
            clearedDirect.removeFirst();
            clearedDirect.removeFirst();
            clearedDirect.addFirst(currentDirect);
            clearedDirect.addFirst(currentDirect);
        } else if (newDirect != null && !aircraft.getRoute().getRemainingWaypoints(aircraft.getSidStarIndex(), aircraft.getRoute().getWaypoints().size - 1).contains(newDirect, false) && currentDispLatMode == FLY_HEADING && containsCode(clearedDispLatMode, SID_STAR, HOLD_AT, AFTER_WAYPOINT_FLY_HEADING)) {
            //Case 3: Aircraft has reached end of SID/STAR during delay: Replace latmode with "fly heading"
            //Set all the cleared heading to current aircraft cleared heading
            replaceAllClearedHdg(aircraft.getClearedHeading());
            replaceAllClearedAltMode();
        } else if (aircraft.isLocCap() && clearedHdg.get(1) != aircraft.getIls().getHeading()) {
            //Case 4: Aircraft captured LOC during delay: Replace all set headings to ILS heading
            replaceAllClearedHdg(aircraft.getIls().getHeading());

            if (aircraft.getIls().isNpa()) {
                //Case 4b: Aircraft on LDA has captured LOC and is performing non precision approach: Replace all set altitude to missed approach altitude
                replaceAllClearedAltMode();
                replaceAllClearedAlt();
            }
        }

        if (aircraft.isGsCap() || (aircraft.getIls() != null && aircraft.getIls().isNpa() && aircraft.isLocCap())) {
            //Case 5: Aircraft captured GS during delay: Replace all set altitude to missed approach altitude
            replaceAllClearedAltMode();
            replaceAllClearedAlt();
        }
    }

    /** Replaces all after waypoint, fly heading modes with fly heading, replaces cleared heading to input heading */
    public void replaceAllAfterWptModesWithHdg(int hdg) {
        int latSize = dispLatMode.size;
        for (int i = 0; i < latSize; i++) {
            int latMode = dispLatMode.removeFirst();
            int altMode = dispAltMode.removeFirst();
            int spdMode = dispSpdMode.removeFirst();
            if (latMode == AFTER_WAYPOINT_FLY_HEADING) {
                latMode = FLY_HEADING;
                altMode = NO_RESTR;
                spdMode = NO_RESTR;
            }
            dispLatMode.addLast(latMode);
            dispAltMode.addLast(altMode);
            dispSpdMode.addLast(spdMode);
        }

        int size = clearedHdg.size;
        clearedHdg.clear();
        for (int i = 0; i < size; i++) {
            clearedHdg.addLast(hdg);
        }
    }

    /** Replaces all directs that is earlier than the current aircraft direct waypoint (or if null) */
    public void replaceAllOutdatedDirects(Waypoint latestDirect) {
        int size = clearedDirect.size;
        for (int i = 0; i < size; i++) {
            Waypoint wpt = clearedDirect.removeFirst();
            if (latestDirect == null || aircraft.getRoute().findWptIndex(wpt.getName()) < aircraft.getRoute().findWptIndex(latestDirect.getName())) wpt = latestDirect;
            clearedDirect.addLast(wpt);
        }
    }

    /** Gets the current cleared aircraft heading and sets all subsequently cleared headings to that value, sets lat mode to fly heading */
    private void replaceAllClearedHdg(int hdg) {
        int latSize = dispLatMode.size;
        dispLatMode.clear();
        for (int i = 0; i < latSize; i++) {
            dispLatMode.addLast(FLY_HEADING);
        }
        int size = clearedHdg.size;
        clearedHdg.clear();
        for (int i = 0; i < size; i++) {
            clearedHdg.addLast(hdg);
        }
    }

    /** Replaces all turn left/right heading modes (old) with fly heading, used to migrate old mode (before turn/left removed) to new mode */
    private void replaceAllTurnHdgModes() {
        int size = dispLatMode.size;
        for (int i = 0; i < size; i++) {
            int code = dispLatMode.removeFirst();
            dispLatMode.addLast(containsCode(code, TURN_RIGHT, TURN_LEFT) ? FLY_HEADING : code);
        }
    }

    /** Replaces all turn directions with no direction, called after aircraft has finished a turn instructed in a specific direction */
    public void replaceAllTurnDirections() {
        int size = clearedTurnDir.size;
        clearedTurnDir.clear();
        for (int i = 0; i < size; i++) {
            clearedTurnDir.addLast(NO_DIRECTION);
        }
    }

    /** Sets all alt mode to climb/descend (no expedite) */
    public void replaceAllClearedAltMode() {
        int altSize = dispAltMode.size;
        dispAltMode.clear();
        for (int i = 0; i < altSize; i++) {
            dispAltMode.addLast(NO_RESTR);
        }

        int expSize = clearedExpedite.size;
        clearedExpedite.clear();
        for (int i = 0; i < expSize; i++) {
            clearedExpedite.addLast(false);
        }
    }

    /** Sets all spd mode to unrestricted */
    public void replaceAllClearedSpdMode() {
        int spdSize = dispSpdMode.size;
        dispSpdMode.clear();
        for (int i = 0; i < spdSize; i++) {
            dispSpdMode.addLast(NO_RESTR);
        }
    }

    /** Gets the current cleared aircraft altitude and sets all subsequently cleared altitudes to that value */
    public void replaceAllClearedAlt() {
        int currentAlt = aircraft.getClearedAltitude();
        int size = clearedAlt.size;
        clearedAlt.clear();
        for (int i = 0; i < size; i++) {
            clearedAlt.addLast(currentAlt);
        }
    }

    /** Gets current cleared aircraft speed and sets all subsequently cleared speed to that value if larger */
    public void replaceAllClearedSpdToLower() {
        Queue<Integer> newQueue = new Queue<>();
        while (!clearedSpd.isEmpty()) {
            int first = clearedSpd.removeFirst();
            newQueue.addLast(Math.min(first, aircraft.getClearedIas()));
        }
        clearedSpd = newQueue;
    }

    /** Gets current cleared aircraft speed and sets all subsequently cleared speed to that value if smaller */
    public void replaceAllClearedSpdToHigher() {
        Queue<Integer> newQueue = new Queue<>();
        while (!clearedSpd.isEmpty()) {
            int first = clearedSpd.removeFirst();
            newQueue.addLast(Math.max(first, aircraft.getClearedIas()));
        }
        clearedSpd = newQueue;
    }

    /** Removes all ILS clearances */
    public void voidAllIls() {
        int size = clearedIls.size;
        clearedIls.clear();
        for (int i = 0; i < size; i++) {
            clearedIls.addLast(null);
        }
    }

    /** Adds new lateral instructions to queue */
    public void sendLat(int latMode, String clearedWpt, String afterWpt, String holdWpt, int afterWptHdg, int clearedHdg, String clearedApch, String newStar, int turnDir) {
        int latModeName = latMode;
        if (latMode == SID_STAR) {
            clearedDirect.addLast(radarScreen.waypoints.get(clearedWpt));
            if (latModes.first().contains("arrival")) {
                updateLatModes(ADD_ALL_SIDSTAR, false);
            }
            if (clearedHold.last() != null) {
                clearedHold.removeLast();
                clearedHold.addLast(null);
            }
        } else if (latMode == AFTER_WAYPOINT_FLY_HEADING) {
            clearedAftWpt.addLast(radarScreen.waypoints.get(afterWpt));
            clearedAftWptHdg.addLast(afterWptHdg);
        } else if (latMode == HOLD_AT) {
            clearedHold.addLast(radarScreen.waypoints.get(holdWpt));
            updateLatModes(REMOVE_AFTERHDG_ONLY, false);
        } else if (latMode == CHANGE_STAR) {
            clearedNewStar.addLast(newStar);
            if (dispLatMode.last() == FLY_HEADING) {
                latModeName = dispLatMode.last();
            } else {
                latModeName = FLY_HEADING;
                this.clearedHdg.addLast((int) aircraft.getHeading());
            }
            clearedDirect.addLast(null);
            clearedAftWpt.addLast(null);
            clearedHold.addLast(null);
            updateLatModes(REMOVE_ALL_SIDSTAR, false);
        } else {
            this.clearedHdg.addLast(clearedHdg);
            clearedTurnDir.addLast(turnDir);
        }
        if (aircraft instanceof Arrival) {
            clearedIls.addLast(aircraft.getAirport().getApproaches().get(clearedApch.substring(3)));
            if (latMode == SID_STAR) {
                if (clearedIls.last() != null) {
                    updateLatModes(REMOVE_AFTERHDG_HOLD, false);
                } else {
                    updateLatModes(ADD_ALL_SIDSTAR, false);
                }
            }
        }
        dispLatMode.addLast(latModeName);
        goAround.addLast(aircraft.isGoAround());
        length++;
        fillUpInt(this.clearedHdg);
        fillUpWpt(clearedDirect);
        fillUpWpt(clearedAftWpt);
        fillUpInt(clearedAftWptHdg);
        fillUpWpt(clearedHold);
        fillUpILS(clearedIls);
        fillUpString(clearedNewStar);
        fillUpInt(clearedTurnDir);
    }

    /** Adds new altitude instructions to queue, called after sendLat */
    public void sendAlt(int altMode, int clearedAlt, boolean expedite) {
        this.clearedAlt.addLast(clearedAlt);
        dispAltMode.addLast(altMode);
        clearedExpedite.addLast(expedite);
        fillUpBool(clearedExpedite);
    }

    /** Adds new speed instructions to queue, called after sendAlt */
    public void sendSpd(int spdMode, int clearedSpd) {
        if (aircraft instanceof Departure && !((Departure) aircraft).isAccel() && clearedSpd == aircraft.getV2()) {
            this.clearedSpd.addLast(220);
        } else {
            this.clearedSpd.addLast(clearedSpd);
        }
        dispSpdMode.addLast(spdMode);
    }

    /** Fills up input queue to ideal length, with its last element */
    private void fillUpInt(Queue<Integer> queue) {
        while (queue.size < length) {
            queue.addLast(queue.last());
        }
    }

    private void fillUpWpt(Queue<Waypoint> queue) {
        while (queue.size < length) {
            queue.addLast(queue.last());
        }
    }

    private void fillUpBool(Queue<Boolean> queue) {
        while (queue.size < length) {
            queue.addLast(queue.last());
        }
    }

    private void fillUpILS(Queue<ILS> queue) {
        while (queue.size < length) {
            queue.addLast(queue.last());
        }
    }

    private void fillUpString(Queue<String> queue) {
        while (queue.size < length) {
            queue.addLast(queue.last());
        }
    }

    /** Updates the time queue for instructions */
    public void updateTime() {
        for (int i = 0; i < timeQueueArray.size; i++) {
            timeQueueArray.set(i, timeQueueArray.get(i) - Gdx.graphics.getDeltaTime());
        }
        if (timeQueueArray.size > 0 && timeQueueArray.get(0) <= 0) {
            timeQueueArray.removeIndex(0);
            sendInstructions();
        }
    }

    /** Replaces the selections in latModes depending on input mode, and will update the current UI if updateUI is true */
    public void updateLatModes(int mode, boolean updateUI) {
        //Will not throw exception even if element not in array
        switch (mode) {
            case REMOVE_ALL_SIDSTAR:
                latModes.clear();
                latModes.add(Ui.FLY_HEADING);
                if (!radarScreen.tutorial && aircraft instanceof Arrival) latModes.add(Ui.CHANGE_STAR);
                break;
            case REMOVE_AFTERHDG_HOLD:
                latModes.removeValue(Ui.AFTER_WPT_FLY_HDG, false);
                latModes.removeValue(Ui.HOLD_AT, false);
                break;
            case REMOVE_SIDSTAR_ONLY:
                latModes.removeValue(aircraft.getSidStar().getName() + " arrival", false);
                latModes.removeValue(aircraft.getSidStar().getName() + " departure", false);
                break;
            case REMOVE_SIDSTAR_AFTERHDG:
                latModes.removeValue(aircraft.getSidStar().getName() + " arrival", false);
                latModes.removeValue(aircraft.getSidStar().getName() + " departure", false);
                latModes.removeValue(Ui.AFTER_WPT_FLY_HDG, false);
                break;
            case REMOVE_HOLD_ONLY:
                latModes.removeValue(Ui.HOLD_AT, false);
                break;
            case REMOVE_AFTERHDG_ONLY:
                latModes.removeValue(Ui.AFTER_WPT_FLY_HDG, false);
                break;
            case ADD_ALL_SIDSTAR:
                latModes.clear();
                if (aircraft instanceof Arrival) {
                    latModes.add(aircraft.getSidStar().getName() + " arrival", Ui.AFTER_WPT_FLY_HDG, Ui.FLY_HEADING);
                    latModes.add(Ui.HOLD_AT);
                    if (!radarScreen.tutorial) latModes.add(Ui.CHANGE_STAR);
                } else if (aircraft instanceof Departure) {
                    latModes.add(aircraft.getSidStar().getName() + " departure", Ui.FLY_HEADING);
                }
                break;
            default:
                Gdx.app.log("NavState", "Invalid latModes update mode: " + mode);
        }
        if (updateUI && aircraft.isSelected() && aircraft.isArrivalDeparture()) aircraft.ui.updateState();
    }

    /** Replaces the selections in altModes depending on input mode, and will update the current UI if updateUI is true */
    public void updateAltModes(int mode, boolean updateUI) {
        //Will not throw exception even if element not in array
        switch (mode) {
            case REMOVE_SIDSTAR_RESTR:
                altModes.removeValue(Ui.CLIMB_VIA_SID, false);
                altModes.removeValue(Ui.DESCEND_VIA_STAR, false);
                break;
            case ADD_SIDSTAR_RESTR_UNRESTR:
                altModes.clear();
                if (aircraft instanceof Arrival) {
                    altModes.add(Ui.DESCEND_VIA_STAR);
                } else if (aircraft instanceof Departure) {
                    altModes.add(Ui.CLIMB_VIA_SID);
                }
                altModes.add(Ui.CLIMB_DESCEND_TO);
                break;
            case REMOVE_UNRESTR:
                altModes.removeValue(Ui.CLIMB_DESCEND_TO, false);
                break;
            case ADD_UNRESTR_ONLY:
                altModes.clear();
                altModes.add(Ui.CLIMB_DESCEND_TO);
                break;
            case ADD_SIDSTAR_RESTR_ONLY:
                altModes.clear();
                if (aircraft instanceof Arrival) {
                    altModes.add(Ui.DESCEND_VIA_STAR);
                } else if (aircraft instanceof Departure) {
                    altModes.add(Ui.CLIMB_VIA_SID);
                }
                break;
            default:
                Gdx.app.log("NavState", "Invalid altModes update mode: " + mode);
        }
        if (updateUI && aircraft.isSelected() && aircraft.isArrivalDeparture()) aircraft.ui.updateState();
    }

    /** Replaces the selections in spdModes depending on input mode, and will update the current UI if updateUI is true */
    public void updateSpdModes(int mode, boolean updateUI) {
        //Will not throw exception even if element not in array
        switch (mode) {
            case REMOVE_SIDSTAR_RESTR:
                spdModes.removeValue(Ui.SID_SPD_RESTRICTIONS, false);
                spdModes.removeValue(Ui.STAR_SPD_RESTRICTIONS, false);
                break;
            case ADD_SIDSTAR_RESTR_UNRESTR:
                spdModes.clear();
                if (aircraft instanceof Arrival) {
                    spdModes.add(Ui.STAR_SPD_RESTRICTIONS);
                } else if (aircraft instanceof Departure) {
                    spdModes.add(Ui.SID_SPD_RESTRICTIONS);
                }
                spdModes.add(Ui.NO_SPD_RESTRICTIONS);
                break;
            case REMOVE_UNRESTR:
                spdModes.removeValue(Ui.NO_SPD_RESTRICTIONS, false);
                break;
            case ADD_UNRESTR_ONLY:
                spdModes.clear();
                spdModes.add(Ui.NO_SPD_RESTRICTIONS);
                break;
            case ADD_SIDSTAR_RESTR_ONLY:
                spdModes.clear();
                if (aircraft instanceof Arrival) {
                    spdModes.add(Ui.STAR_SPD_RESTRICTIONS);
                } else if (aircraft instanceof Departure) {
                    spdModes.add(Ui.SID_SPD_RESTRICTIONS);
                }
                break;
            default:
                Gdx.app.log("NavState", "Invalid spdModes update mode: " + mode);
        }
        if (updateUI && aircraft.isSelected() && aircraft.isArrivalDeparture()) aircraft.ui.updateState();
    }

    /** Gets the appropriate navState code from string */
    public static int getCodeFromString(String string) {
        if (string.contains("arrival") || string.contains("departure")) {
            return SID_STAR;
        } else if (Ui.AFTER_WPT_FLY_HDG.equals(string)) {
            return AFTER_WAYPOINT_FLY_HEADING;
        } else if (Ui.FLY_HEADING.equals(string)) {
            return FLY_HEADING;
        } else if (Ui.LEFT_HEADING.equals(string)) {
            return FLY_HEADING; //No longer used, defaults to fly heading
        } else if (Ui.RIGHT_HEADING.equals(string)) {
            return FLY_HEADING; //No longer used, defaults to fly heading
        } else if (Ui.HOLD_AT.equals(string)) {
            return HOLD_AT;
        } else if (Ui.CLIMB_VIA_SID.equals(string) || Ui.DESCEND_VIA_STAR.equals(string) || Ui.SID_SPD_RESTRICTIONS.equals(string) || Ui.STAR_SPD_RESTRICTIONS.equals(string)) {
            return SID_STAR_RESTR;
        } else if (Ui.CLIMB_DESCEND_TO.equals(string) || Ui.NO_SPD_RESTRICTIONS.equals(string)) {
            return NO_RESTR;
        } else if (Ui.EXPEDITE_TO.equals(string)) {
            return NO_RESTR; //No longer used, defaults to no restriction
        } else if (Ui.CHANGE_STAR.equals(string)) {
            return CHANGE_STAR;
        } else {
            //No such code
            Gdx.app.log("Navstate", "Unknown navState string " + string);
            return UNKNOWN_STATE;
        }
    }

    /** Checks if the supplied code matches any of the required codes */
    public boolean containsCode(int toCheck, int... codesAllowed) {
        return ArrayUtils.contains(codesAllowed, toCheck);
    }

    public Array<String> getLatModes() {
        return latModes;
    }

    public Array<String> getAltModes() {
        return altModes;
    }

    public Array<String> getSpdModes() {
        return spdModes;
    }

    public Queue<Integer> getDispLatMode() {
        return dispLatMode;
    }

    public Queue<Integer> getDispAltMode() {
        return dispAltMode;
    }

    public Queue<Integer> getDispSpdMode() {
        return dispSpdMode;
    }

    public Queue<Integer> getClearedHdg() {
        return clearedHdg;
    }

    public Queue<Waypoint> getClearedDirect() {
        return clearedDirect;
    }

    public Queue<Waypoint> getClearedAftWpt() {
        return clearedAftWpt;
    }

    public Queue<Integer> getClearedAftWptHdg() {
        return clearedAftWptHdg;
    }

    public Queue<ILS> getClearedIls() {
        return clearedIls;
    }

    public Queue<Integer> getClearedAlt() {
        return clearedAlt;
    }

    public Queue<Boolean> getClearedExpedite() {
        return clearedExpedite;
    }

    public Queue<Integer> getClearedSpd() {
        return clearedSpd;
    }

    public Queue<Waypoint> getClearedHold() {
        return clearedHold;
    }

    public Array<Float> getTimeQueueArray() {
        return timeQueueArray;
    }

    public Queue<Boolean> getGoAround() {
        return goAround;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    public Queue<String> getClearedNewStar() {
        return clearedNewStar;
    }

    public Queue<Integer> getClearedTurnDir() {
        return clearedTurnDir;
    }
}
