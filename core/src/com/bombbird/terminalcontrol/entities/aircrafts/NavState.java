package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import org.json.JSONArray;
import org.json.JSONObject;

public class NavState {
    public static float timeDelay = 2f;

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
        altModes = new Array<String>(5);
        spdModes = new Array<String>(3);

        if (aircraft instanceof Arrival) {
            //Arrival
            latModes = new Array<String>(6);
            latModes.add(aircraft.getSidStar().getName() + " arrival", "After waypoint, fly heading", "Hold at", "Fly heading");
            latModes.add("Turn left heading", "Turn right heading");

            altModes.add("Descend via STAR");

            spdModes.add("STAR speed restrictions");
        } else if (aircraft instanceof Departure) {
            //Departure
            latModes = new Array<String>(4);
            latModes.add(aircraft.getSidStar().getName() + " departure", "Fly heading", "Turn left heading", "Turn right heading");

            altModes.add("Climb via SID");

            spdModes.add("SID speed restrictions");
        } else {
            //Nani
            Gdx.app.log("Navstate type error", "Unknown navstate type specified!");
            latModes = new Array<String>(1);
        }
        altModes.add("Climb/descend to", "Expedite climb/descent to");

        spdModes.add("No speed restrictions");

        dispLatMode = new Queue<String>();
        dispLatMode.addLast(latModes.get(0));
        dispAltMode = new Queue<String>();
        dispAltMode.addLast(altModes.get(0));
        dispSpdMode = new Queue<String>();
        dispSpdMode.addLast(spdModes.get(0));

        timeQueue = new Array<Float>();

        clearedHdg = new Queue<Integer>();
        clearedHdg.addLast(aircraft.getClearedHeading());
        clearedDirect = new Queue<Waypoint>();
        clearedDirect.addLast(aircraft.getDirect());
        clearedAftWpt = new Queue<Waypoint>();
        clearedAftWpt.addLast(aircraft.getAfterWaypoint());
        clearedAftWptHdg = new Queue<Integer>();
        clearedAftWptHdg.addLast(aircraft.getAfterWptHdg());
        clearedHold = new Queue<Waypoint>();
        clearedHold.addFirst(null);
        clearedIls = new Queue<ILS>();
        clearedIls.addLast(null);

        clearedAlt = new Queue<Integer>();
        clearedAlt.addLast(aircraft.getClearedAltitude());
        clearedExpedite = new Queue<Boolean>();
        clearedExpedite.addLast(aircraft.isExpedite());

        clearedSpd = new Queue<Integer>();
        clearedSpd.addLast(aircraft.getClimbSpd());

        goAround = new Queue<Boolean>();
        goAround.addLast(false);
    }

    public NavState(Aircraft aircraft, JSONObject save) {
        radarScreen = TerminalControl.radarScreen;
        this.aircraft = aircraft;

        latModes = new Array<String>();
        altModes = new Array<String>();
        spdModes = new Array<String>();

        timeQueue = new Array<Float>();

        dispLatMode = new Queue<String>();
        dispAltMode = new Queue<String>();
        dispSpdMode = new Queue<String>();

        clearedHdg = new Queue<Integer>();
        clearedDirect = new Queue<Waypoint>();
        clearedAftWpt = new Queue<Waypoint>();
        clearedAftWptHdg = new Queue<Integer>();
        clearedHold = new Queue<Waypoint>();
        clearedIls = new Queue<ILS>();

        clearedAlt = new Queue<Integer>();
        clearedExpedite = new Queue<Boolean>();

        clearedSpd = new Queue<Integer>();

        goAround = new Queue<Boolean>();

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

            dispLatMode.removeFirst();
            dispAltMode.removeFirst();
            dispSpdMode.removeFirst();

            clearedHdg.removeFirst();
            if (aircraft instanceof Arrival || (aircraft instanceof Departure && ((Departure) aircraft).isSidSet())) {
                aircraft.setClearedHeading(clearedHdg.first());
            }
            clearedDirect.removeFirst();
            aircraft.setDirect(clearedDirect.first());
            aircraft.setSidStarIndex(aircraft.getSidStar().findWptIndex(aircraft.getDirect() == null ? null : aircraft.getDirect().getName()));
            clearedAftWpt.removeFirst();
            aircraft.setAfterWaypoint(clearedAftWpt.first());
            clearedAftWptHdg.removeFirst();
            aircraft.setAfterWptHdg(clearedAftWptHdg.first());
            clearedHold.removeFirst();
            aircraft.setHoldWpt(clearedHold.first());
            clearedIls.removeFirst();
            aircraft.setIls(clearedIls.first());

            clearedAlt.removeFirst();
            aircraft.setClearedAltitude(clearedAlt.first());
            clearedExpedite.removeFirst();
            aircraft.setExpedite(clearedExpedite.first());

            clearedSpd.removeFirst();
            if (aircraft instanceof Arrival || (aircraft instanceof Departure && ((Departure) aircraft).isSidSet())) {
                aircraft.setClearedIas(clearedSpd.first());
            }

            goAround.removeFirst();
        }
        length--;
    }

    /** Called before updating aircraft mode to ensure inputs are valid in case aircraft state changes during the pilot delay*/
    private void validateInputs() {
        String currentDispLatMode = dispLatMode.first();
        String clearedDispLatMode = dispLatMode.get(1);
        String currentDirect = clearedDirect.first() == null ? null : clearedDirect.first().getName();
        String newDirect = clearedDirect.get(1) == null ? null : clearedDirect.get(1).getName();
        if (currentDispLatMode.contains("heading") && !"After waypoint, fly heading".equals(currentDispLatMode) && ("After waypoint, fky heading".equals(clearedDispLatMode) || "Hold at".equals(clearedDispLatMode))) {
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
        } else if (currentDispLatMode.contains(aircraft.getSidStar().getName()) && aircraft.getSidStar().findWptIndex(newDirect) < aircraft.getSidStar().findWptIndex(currentDirect)) {
            //Case 2: Aircraft direct changes during delay: Replace cleared direct if it is before new direct
            clearedDirect.removeFirst();
            clearedDirect.removeFirst();
            clearedDirect.addFirst(radarScreen.waypoints.get(currentDirect));
            clearedDirect.addFirst(radarScreen.waypoints.get(currentDirect));
        } else if (aircraft.getDirect() == null && "Fly heading".equals(currentDispLatMode) && (clearedDispLatMode.contains(aircraft.getSidStar().getName()) || "After waypoint, fly heading".equals(clearedDispLatMode) || "Hold at".equals(clearedDispLatMode))) {
            //Case 3: Aircraft has reached end of SID/STAR during delay: Replace latmode with "fly heading"
            dispLatMode.removeFirst();
            dispLatMode.removeFirst();
            dispLatMode.addFirst("Fly heading");
            dispLatMode.addFirst("Fly heading");

            //And set all the cleared heading to current aircraft cleared heading
            replaceAllClearedHdg();
        } else if (aircraft.isLocCap() && clearedHdg.get(1) != aircraft.getIls().getHeading()) {
            //Case 4: Aircraft captured LOC during delay: Replace all set headings to ILS heading
            replaceAllClearedHdg();

            if (aircraft.getIls() instanceof LDA) {
                //Case 4b: Aircraft on LDA has captured LOC and is performing non precision approach: Replace all set altitude to missed approach altitude
                replaceAllClearedAlt();
            }
        }

        if (aircraft.isGsCap() || (aircraft.getIls() instanceof LDA && aircraft.isLocCap())) {
            //Case 5: Aircraft captured GS during delay: Replace all set altitude to missed approach altitude
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
        Queue<String> newLatMode = new Queue<String>();

        int size = dispLatMode.size;
        for (int i = 0; i < size; i++) {
            String string = dispLatMode.removeFirst();
            newLatMode.addLast(string.contains("Turn") ? "Fly heading" : string);
        }

        dispLatMode = newLatMode;
    }

    /** Gets the current cleared aircraft altitude and sets all subsequently cleared altitudes to that value, sets alt mode to climb/descend (no expedite) */
    public void replaceAllClearedAlt() {
        int altSize = dispAltMode.size;
        dispAltMode.clear();
        for (int i = 0; i < altSize; i++) {
            dispAltMode.addLast("Climb/descend to");
        }
        int currentAlt = aircraft.getClearedAltitude();
        int size = clearedAlt.size;
        clearedAlt.clear();
        for (int i = 0; i < size; i++) {
            clearedAlt.addLast(currentAlt);
        }
    }

    /** Gets current cleared aircraft speed and sets all subsequently cleared speed to that value if larger */
    public void replaceAllClearedSpdToLower() {
        Queue<Integer> newQueue = new Queue<Integer>();
        while (!clearedSpd.isEmpty()) {
            int first = clearedSpd.removeFirst();
            newQueue.addLast(first > aircraft.getClearedIas() ? aircraft.getClearedIas() : first);
        }
        clearedSpd = newQueue;
    }

    public void voidAllIls() {
        int size = clearedIls.size;
        clearedIls.clear();
        for (int i = 0; i < size; i++) {
            clearedIls.addLast(null);
        }
    }

    /** Adds new lateral instructions to queue */
    public void sendLat(String latMode, String clearedWpt, String afterWpt, String holdWpt, int afterWptHdg, int clearedHdg, String clearedILS) {
        if (latMode.contains(aircraft.getSidStar().getName())) {
            clearedDirect.addLast(radarScreen.waypoints.get(clearedWpt));
            if (latMode.contains("arrival")) {
                if (!latModes.contains("After waypoint, fly heading", false)) {
                    latModes.add("After waypoint, fly heading");
                }
                if (!latModes.contains("Hold at", false)) {
                    latModes.add("Hold at");
                }
            }
            if (clearedHold.last() != null) {
                clearedHold.removeLast();
                clearedHold.addLast(null);
            }
        } else if (latMode.equals("After waypoint, fly heading")) {
            clearedAftWpt.addLast(radarScreen.waypoints.get(afterWpt));
            clearedAftWptHdg.addLast(afterWptHdg);
        } else if (latMode.equals("Hold at")) {
            clearedHold.addLast(radarScreen.waypoints.get(holdWpt));
            latModes.removeValue("After waypoint, fly heading", false);
        } else {
            this.clearedHdg.addLast(clearedHdg);
            if (aircraft instanceof Arrival) {
                clearedIls.addLast(aircraft.getAirport().getApproaches().get(clearedILS.substring(3)));
            }
        }
        dispLatMode.addLast(latMode);
        goAround.addLast(aircraft.isGoAround());
        length++;
        fillUp(this.clearedHdg);
        fillUp(clearedDirect);
        fillUp(clearedAftWpt);
        fillUp(clearedAftWptHdg);
        fillUp(clearedHold);
        fillUp(clearedIls);
    }

    /** Adds new altitude instructions to queue, called after sendLat */
    public void sendAlt(String altMode, int clearedAlt) {
        this.clearedAlt.addLast(clearedAlt);
        dispAltMode.addLast(altMode);
        clearedExpedite.addLast(altMode.contains("Expedite"));
        fillUp(clearedExpedite);
    }

    /** Adds new speed instructions to queue, called after sendAlt */
    public void sendSpd(String spdMode, int clearedSpd) {
        this.clearedSpd.addLast(clearedSpd);
        dispSpdMode.addLast(spdMode);
    }

    /** Fills up input queue to ideal length, with its last element */
    private void fillUp(Queue queue) {
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
}
