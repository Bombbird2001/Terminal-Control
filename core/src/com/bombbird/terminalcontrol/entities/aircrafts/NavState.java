package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class NavState {
    public static float timeDelay = 2f;

    private Aircraft aircraft;

    private Array<String> latModes;
    private Array<String> altModes;
    private Array<String> spdModes;

    //Modes used for display
    private Queue<String> dispLatMode;
    private Queue<String> dispAltMode;
    private Queue<String> dispSpdMode;

    private Timer timer;

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

    public NavState(int type, Aircraft aircraft) {
        this.aircraft = aircraft;
        altModes = new Array<String>(5);
        spdModes = new Array<String>(3);

        if (type == 1) {
            //Arrival
            latModes = new Array<String>(6);
            latModes.add(aircraft.getSidStar().getName() + " arrival", "After waypoint, fly heading", "Hold at", "Fly heading");
            latModes.add("Turn left heading", "Turn right heading");

            altModes.add("Descend via STAR");

            spdModes.add("STAR speed restrictions");
        } else if (type == 2) {
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

        timer = new Timer();

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
        clearedSpd.addLast(aircraft.getClearedIas());

        goAround = new Queue<Boolean>();
        goAround.addLast(false);
    }

    /** Schedules a new task of setting aircraft's instructions after time delay; should be called after sending lat, alt and spd */
    public void updateState() {
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                if (!goAround.get(1) && aircraft.isGoAround()) {
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
                    //Do not send inputs if aircraft went around during delay
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
        }, timeDelay);
    }

    /** Called before updating aircraft mode to ensure inputs are valid in case aircraft state changes during the pilot delay*/
    private void validateInputs() {
        String currentDispLatMode = dispLatMode.first();
        String clearedDispLatMode = dispLatMode.get(1);
        String currentDirect = clearedDirect.first() == null ? null : clearedDirect.first().getName();
        String newDirect = clearedDirect.get(1) == null ? null : clearedDirect.get(1).getName();
        if (currentDispLatMode.contains("heading") && !currentDispLatMode.equals("After waypoint, fly heading") && (clearedDispLatMode.equals("After waypoint, fky heading") || clearedDispLatMode.equals("Hold at"))) {
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
            clearedDirect.addFirst(RadarScreen.waypoints.get(currentDirect));
            clearedDirect.addFirst(RadarScreen.waypoints.get(currentDirect));
        } else if (aircraft.getDirect() == null && currentDispLatMode.equals("Fly heading") && (clearedDispLatMode.contains(aircraft.getSidStar().getName()) || clearedDispLatMode.equals("After waypoint, fly heading") || clearedDispLatMode.equals("Hold at"))) {
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

    /** Gets the current cleared aircraft altitude and sets all subsequently cleared altitudes to that value, sets alt mode to climb/descend (no expedite) */
    private void replaceAllClearedAlt() {
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
            clearedDirect.addLast(RadarScreen.waypoints.get(clearedWpt));
            if (latMode.contains("arrival")) {
                if (!latModes.contains("After waypoint, fly heading", false)) {
                    latModes.add("After waypoint, fly heading");
                }
                if (!latModes.contains("Hold at", false)) {
                    latModes.add("Hold at");
                }
            }
        } else if (latMode.equals("After waypoint, fly heading")) {
            clearedAftWpt.addLast(RadarScreen.waypoints.get(afterWpt));
            clearedAftWptHdg.addLast(afterWptHdg);
        } else if (latMode.equals("Hold at")) {
            clearedHold.addLast(RadarScreen.waypoints.get(holdWpt));
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

    private void fillUp(Queue queue) {
        while (queue.size < length) {
            queue.addLast(queue.last());
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
}
