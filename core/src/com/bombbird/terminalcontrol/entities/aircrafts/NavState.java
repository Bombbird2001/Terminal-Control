package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import org.json.JSONArray;
import org.json.JSONObject;

public class NavState {
    public static float timeDelay = 2f;
    public static final int REMOVE_ALL_SIDSTAR = 0; //Removes all SID/STAR choices - SID, STAR, after waypoint fly heading, hold at
    public static final int REMOVE_AFTERHDG_HOLD = 1; //Removes after waypoint fly heading, hold at
    public static final int REMOVE_SIDSTAR_ONLY = 2; //Removes only the SID, STAR mode
    public static final int REMOVE_SIDSTAR_AFTERHDG = 3; //Removes SID, STAR, after waypoint fly heading
    public static final int REMOVE_HOLD_ONLY = 4; //Removes only hold at
    public static final int REMOVE_AFTERHDG_ONLY = 5; //Removes only after waypoint fly heading
    public static final int ADD_ALL_SIDSTAR = 6; //Adds all SID/STAR choices - SID, STAR, after waypoint fly heading, hold at

    public static final int REMOVE_SIDSTAR_RESTR = 10; //Removes SID/STAR alt/speed restrictions
    public static final int ADD_SIDSTAR_RESTR = 11; //Adds SID/STAR alt/speed restrictions

    private Aircraft aircraft;

    private Array<String> latModes;
    private Array<String> altModes;
    private Array<String> spdModes;

    private Array<Float> timeQueue;

    //Modes used for display
    private Queue<String> dispLatMode;
    private Queue<String> dispAltMode;
    private Queue<String> dispSpdMode;

    private Queue<Integer> clearedHdg;
    private Queue<Waypoint> clearedDirect;
    private Queue<Waypoint> clearedAftWpt;
    private Queue<Integer> clearedAftWptHdg;
    private Queue<Waypoint> clearedHold;
    private Queue<ILS> clearedIls;

    private Queue<Integer> clearedAlt;
    private Queue<Boolean> clearedExpedite;

    private Queue<Integer> clearedSpd;

    private Queue<Boolean> goAround;

    private int length = 1;

    private RadarScreen radarScreen;

    public NavState(Aircraft aircraft) {
        radarScreen = TerminalControl.radarScreen;

        this.aircraft = aircraft;
        altModes = new Array<>(5);
        spdModes = new Array<>(3);

        if (aircraft instanceof Arrival) {
            //Arrival
            latModes = new Array<>(6);
            latModes.add(aircraft.getSidStar().getName() + " arrival", "After waypoint, fly heading", "Hold at", "Fly heading");
            latModes.add("Turn left heading", "Turn right heading");

            altModes.add("Descend via STAR");

            spdModes.add("STAR speed restrictions");
        } else if (aircraft instanceof Departure) {
            //Departure
            latModes = new Array<>(4);
            latModes.add(aircraft.getSidStar().getName() + " departure", "Fly heading", "Turn left heading", "Turn right heading");

            altModes.add("Climb via SID");

            spdModes.add("SID speed restrictions");
        } else {
            //Nani
            Gdx.app.log("Navstate type error", "Unknown navstate type specified!");
            latModes = new Array<>(1);
        }
        altModes.add("Climb/descend to", "Expedite climb/descent to");

        spdModes.add("No speed restrictions");

        dispLatMode = new Queue<>();
        dispLatMode.addLast(latModes.get(0));
        dispAltMode = new Queue<>();
        dispAltMode.addLast(altModes.get(0));
        dispSpdMode = new Queue<>();
        dispSpdMode.addLast(spdModes.get(0));

        timeQueue = new Array<>();

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

        timeQueue = new Array<>();

        dispLatMode = new Queue<>();
        dispAltMode = new Queue<>();
        dispSpdMode = new Queue<>();

        clearedHdg = new Queue<>();
        clearedDirect = new Queue<>();
        clearedAftWpt = new Queue<>();
        clearedAftWptHdg = new Queue<>();
        clearedHold = new Queue<>();
        clearedIls = new Queue<>();

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
        }

        {
            JSONArray array = save.getJSONArray("altModes");
            for (int i = 0; i < array.length(); i++) {
                altModes.add(array.getString(i));
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
                timeQueue.add((float) array.getDouble(i));
            }
        }

        addToQueueString(save.getJSONArray("dispLatMode"), dispLatMode);
        addToQueueString(save.getJSONArray("dispAltMode"), dispAltMode);
        addToQueueString(save.getJSONArray("dispSpdMode"), dispSpdMode);
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

        addToQueueInt(save.getJSONArray("clearedAlt"), clearedAlt);
        addToQueueBool(save.getJSONArray("clearedExpedite"), clearedExpedite);
        addToQueueInt(save.getJSONArray("clearedSpd"), clearedSpd);
        addToQueueBool(save.getJSONArray("goAround"), goAround);
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
            queue.addLast(array.isNull(i) ? null : array.getInt(i));
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
        timeQueue.add(timeDelay);
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
        aircraft.setClearedAltitude(clearedAlt.first());
        aircraft.setExpedite(clearedExpedite.first());

        if (aircraft instanceof Arrival || (aircraft instanceof Departure && ((Departure) aircraft).isSidSet())) {
            aircraft.setClearedIas(clearedSpd.first());
        }

    }

    /** Called before updating aircraft mode to ensure inputs are valid in case aircraft state changes during the pilot delay*/
    private void validateInputs() {
        if (dispLatMode.size < 2 || clearedDirect.size < 2 || clearedAftWptHdg.size < 2 || clearedHdg.size < 2) return;
        String currentDispLatMode = dispLatMode.first();
        String clearedDispLatMode = dispLatMode.get(1);
        String currentDirect = clearedDirect.first() == null ? null : clearedDirect.first().getName();
        String newDirect = clearedDirect.get(1) == null ? null : clearedDirect.get(1).getName();
        if (currentDispLatMode.contains("heading") && !"After waypoint, fly heading".equals(currentDispLatMode) && ("After waypoint, fly heading".equals(clearedDispLatMode) || "Hold at".equals(clearedDispLatMode))) {
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
        } else if (currentDispLatMode.contains(aircraft.getSidStar().getName()) && aircraft.getRoute().findWptIndex(newDirect) < aircraft.getRoute().findWptIndex(currentDirect)) {
            //Case 2: Aircraft direct changes during delay: Replace cleared direct if it is before new direct
            clearedDirect.removeFirst();
            clearedDirect.removeFirst();
            clearedDirect.addFirst(radarScreen.waypoints.get(currentDirect));
            clearedDirect.addFirst(radarScreen.waypoints.get(currentDirect));
        } else if (aircraft.getDirect() == null && "Fly heading".equals(currentDispLatMode) && (clearedDispLatMode.contains(aircraft.getSidStar().getName()) || "After waypoint, fly heading".equals(clearedDispLatMode) || "Hold at".equals(clearedDispLatMode))) {
            //Case 3: Aircraft has reached end of SID/STAR during delay: Replace latmode with "fly heading"
            //Set all the cleared heading to current aircraft cleared heading
            replaceAllClearedHdg();
            replaceAllClearedAltMode();
        } else if (aircraft.isLocCap() && clearedHdg.get(1) != aircraft.getIls().getHeading()) {
            //Case 4: Aircraft captured LOC during delay: Replace all set headings to ILS heading
            replaceAllClearedHdg();

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

    /** Gets the current cleared aircraft heading and sets all subsequently cleared headings to that value, sets lat mode to fly heading */
    private void replaceAllClearedHdg() {
        int latSize = dispLatMode.size;
        dispLatMode.clear();
        for (int i = 0; i < latSize; i++) {
            dispLatMode.addLast("Fly heading");
        }
        int currentHdg = aircraft.getClearedHeading();
        int size = clearedHdg.size;
        clearedHdg.clear();
        for (int i = 0; i < size; i++) {
            clearedHdg.addLast(currentHdg);
        }
    }

    /** Replaces all turn left/right heading with fly heading, called after aircraft has finished a turn instructed in a specific direction */
    public void replaceAllHdgModes() {
        Queue<String> newLatMode = new Queue<>();

        int size = dispLatMode.size;
        for (int i = 0; i < size; i++) {
            String string = dispLatMode.removeFirst();
            newLatMode.addLast(string.contains("Turn") ? "Fly heading" : string);
        }

        dispLatMode = newLatMode;
    }

    /** Sets all alt mode to climb/descend (no expedite) */
    public void replaceAllClearedAltMode() {
        int altSize = dispAltMode.size;
        dispAltMode.clear();
        for (int i = 0; i < altSize; i++) {
            dispAltMode.addLast("Climb/descend to");
        }

        int expSize = clearedExpedite.size;
        clearedExpedite.clear();
        for (int i = 0; i < expSize; i++) {
            clearedExpedite.addLast(false);
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

    /** Called after aircraft enters holding mode */
    public void initHold() {
        updateAltModes(REMOVE_SIDSTAR_RESTR, false);
        updateSpdModes(REMOVE_SIDSTAR_RESTR, false);
        replaceAllClearedAltMode();
        replaceAllClearedSpdToLower();
    }

    /** Adds new lateral instructions to queue */
    public void sendLat(String latMode, String clearedWpt, String afterWpt, String holdWpt, int afterWptHdg, int clearedHdg, String clearedILS) {
        if (latMode.contains(aircraft.getSidStar().getName())) {
            clearedDirect.addLast(radarScreen.waypoints.get(clearedWpt));
            if (latMode.contains("arrival")) {
                updateLatModes(ADD_ALL_SIDSTAR, false);
            }
            if (clearedHold.last() != null) {
                clearedHold.removeLast();
                clearedHold.addLast(null);
            }
        } else if ("After waypoint, fly heading".equals(latMode)) {
            clearedAftWpt.addLast(radarScreen.waypoints.get(afterWpt));
            clearedAftWptHdg.addLast(afterWptHdg);
        } else if ("Hold at".equals(latMode)) {
            clearedHold.addLast(radarScreen.waypoints.get(holdWpt));
            updateLatModes(REMOVE_AFTERHDG_ONLY, false);
        } else {
            this.clearedHdg.addLast(clearedHdg);
            if (aircraft instanceof Arrival) {
                clearedIls.addLast(aircraft.getAirport().getApproaches().get(clearedILS.substring(3)));
                updateLatModes(REMOVE_HOLD_ONLY, false);
            }
        }
        dispLatMode.addLast(latMode);
        goAround.addLast(aircraft.isGoAround());
        length++;
        fillUpInt(this.clearedHdg);
        fillUpWpt(clearedDirect);
        fillUpWpt(clearedAftWpt);
        fillUpInt(clearedAftWptHdg);
        fillUpWpt(clearedHold);
        fillUpILS(clearedIls);
    }

    /** Adds new altitude instructions to queue, called after sendLat */
    public void sendAlt(String altMode, int clearedAlt) {
        this.clearedAlt.addLast(clearedAlt);
        dispAltMode.addLast(altMode);
        clearedExpedite.addLast(altMode.contains("Expedite"));
        fillUpBool(clearedExpedite);
    }

    /** Adds new speed instructions to queue, called after sendAlt */
    public void sendSpd(String spdMode, int clearedSpd) {
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

    /** Updates the time queue for instructions */
    public void updateTime() {
        for (int i = 0; i < timeQueue.size; i++) {
            timeQueue.set(i, timeQueue.get(i) - Gdx.graphics.getDeltaTime());
        }
        if (timeQueue.size > 0 && timeQueue.get(0) <= 0) {
            timeQueue.removeIndex(0);
            sendInstructions();
        }
    }

    /** Replaces the selections in latModes depending on input mode, and will update the current UI if updateUI is true */
    public void updateLatModes(int mode, boolean updateUI) {
        //Will not throw exception even if element not in array
        switch (mode) {
            case REMOVE_ALL_SIDSTAR:
                latModes.clear();
                latModes.add("Fly heading", "Turn left heading", "Turn right heading");
            case REMOVE_AFTERHDG_HOLD:
                latModes.removeValue("After waypoint, fly heading", false);
                latModes.removeValue("Hold at", false);
            case REMOVE_SIDSTAR_ONLY:
                latModes.removeValue(aircraft.getSidStar().getName() + " arrival", false);
                latModes.removeValue(aircraft.getSidStar().getName() + " departure", false);
            case REMOVE_SIDSTAR_AFTERHDG:
                latModes.removeValue(aircraft.getSidStar().getName() + " arrival", false);
                latModes.removeValue(aircraft.getSidStar().getName() + " departure", false);
                latModes.removeValue("After waypoint, fly heading", false);
            case REMOVE_HOLD_ONLY:
                latModes.removeValue("Hold at", false);
            case REMOVE_AFTERHDG_ONLY:
                latModes.removeValue("After waypoint, fly heading", false);
            case ADD_ALL_SIDSTAR:
                latModes.clear();
                if (aircraft instanceof Arrival) {
                    latModes.add(aircraft.getSidStar().getName() + " arrival", "After waypoint, fly heading", "Hold at", "Fly heading");
                    latModes.add("Turn left heading", "Turn right heading");
                } else if (aircraft instanceof Departure) {
                    latModes.add(aircraft.getSidStar().getName() + " departure", "Fly heading", "Turn left heading", "Turn right heading");
                }
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
                altModes.removeValue("Climb via SID", false);
                altModes.removeValue("Descend via STAR", false);
            case ADD_SIDSTAR_RESTR:
                altModes.clear();
                if (aircraft instanceof Arrival) {
                    altModes.add("Descend via STAR");
                } else if (aircraft instanceof Departure) {
                    altModes.add("Climb via SID");
                }
                altModes.add("Climb/descend to", "Expedite climb/descent to");
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
                spdModes.removeValue("SID speed restrictions", false);
                spdModes.removeValue("STAR speed restrictions", false);
            case ADD_SIDSTAR_RESTR:
                spdModes.clear();
                if (aircraft instanceof Arrival) {
                    spdModes.add("STAR speed restrictions");
                } else if (aircraft instanceof Departure) {
                    spdModes.add("SID speed restrictions");
                }
                spdModes.add("No speed restrictions");
            default:
                Gdx.app.log("NavState", "Invalid spdModes update mode: " + mode);
        }
        if (updateUI && aircraft.isSelected() && aircraft.isArrivalDeparture()) aircraft.ui.updateState();
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

    public Queue<String> getDispLatMode() {
        return dispLatMode;
    }

    public Queue<String> getDispAltMode() {
        return dispAltMode;
    }

    public Queue<String> getDispSpdMode() {
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

    public Array<Float> getTimeQueue() {
        return timeQueue;
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
}
