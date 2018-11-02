package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.entities.aircrafts.approaches.ILS;
import com.bombbird.terminalcontrol.entities.Waypoint;
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

    private Queue<String> latModeQueue;
    private Queue<Integer> clearedHdg;
    private Queue<Waypoint> clearedDirect;
    private int lastSidStarIndex = 0;
    private Queue<Waypoint> clearedAftWpt;
    private Queue<Integer> clearedAftWptHdg;
    private Queue<ILS> clearedIls;

    private Queue<String> altModeQueue;
    private Queue<Integer> clearedAlt;
    private Queue<Boolean> clearedExpedite;

    private Queue<String> spdModeQueue;
    private Queue<Integer> clearedSpd;

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

        latModeQueue = new Queue<String>();
        latModeQueue.addLast(aircraft.getLatMode());
        clearedHdg = new Queue<Integer>();
        clearedHdg.addLast(aircraft.getClearedHeading());
        clearedDirect = new Queue<Waypoint>();
        clearedDirect.addLast(aircraft.getDirect());
        clearedAftWpt = new Queue<Waypoint>();
        clearedAftWpt.addLast(aircraft.getAfterWaypoint());
        clearedAftWptHdg = new Queue<Integer>();
        clearedAftWptHdg.addLast(aircraft.getAfterWptHdg());
        clearedIls = new Queue<ILS>();
        clearedIls.addLast(aircraft.getIls());

        altModeQueue = new Queue<String>();
        altModeQueue.addLast(aircraft.getAltMode());
        clearedAlt = new Queue<Integer>();
        clearedAlt.addLast(aircraft.getClearedAltitude());
        clearedExpedite = new Queue<Boolean>();
        clearedExpedite.addLast(aircraft.isExpedite());

        spdModeQueue = new Queue<String>();
        spdModeQueue.addLast(aircraft.getSpdMode());
        clearedSpd = new Queue<Integer>();
        clearedSpd.addLast(aircraft.getClearedIas());
    }

    /** Schedules a new task of setting aircraft's instructions after time delay; should be called after sending lat, alt and spd */
    public void updateState() {
        timer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                latModeQueue.removeFirst();
                aircraft.setLatMode(latModeQueue.first());
                clearedHdg.removeFirst();
                aircraft.setClearedHeading(clearedHdg.first());
                clearedDirect.removeFirst();
                aircraft.setDirect(clearedDirect.first());
                aircraft.setSidStarIndex(aircraft.getSidStar().findWptIndex(aircraft.getDirect().getName()));
                clearedAftWpt.removeFirst();
                aircraft.setAfterWaypoint(clearedAftWpt.first());
                clearedAftWptHdg.removeFirst();
                aircraft.setAfterWptHdg(clearedAftWptHdg.first());
                clearedIls.removeFirst();
                aircraft.setIls(clearedIls.first());

                altModeQueue.removeFirst();
                aircraft.setAltMode(altModeQueue.first());
                clearedAlt.removeFirst();
                aircraft.setClearedAltitude(clearedAlt.first());
                clearedExpedite.removeFirst();
                aircraft.setExpedite(clearedExpedite.first());

                spdModeQueue.removeFirst();
                aircraft.setSpdMode(spdModeQueue.first());
                clearedSpd.removeFirst();
                aircraft.setClearedIas(clearedSpd.first());
                aircraft.setTargetIas(clearedSpd.first());

                dispLatMode.removeFirst();
                dispAltMode.removeFirst();
                dispSpdMode.removeFirst();

                length--;
            }
        }, timeDelay);
    }

    /** Adds new lateral instructions to queue */
    public void sendLat(String latMode, String clearedWpt, String afterWpt, int afterWptHdg, int clearedHdg, String clearedILS) {
        if (latMode.contains(aircraft.getSidStar().getName())) {
            if (aircraft instanceof Arrival || (aircraft instanceof Departure && ((Departure) aircraft).isSidSet())) {
                latModeQueue.addLast("sidstar");
                clearedDirect.addLast(RadarScreen.waypoints.get(clearedWpt));
                aircraft.updateSelectedWaypoints(null);
                lastSidStarIndex = aircraft.getSidStar().findWptIndex(clearedWpt);
                if (!aircraft.getNavState().getLatModes().contains("After waypoint, fly heading", false)) {
                    aircraft.getNavState().getLatModes().add("After waypoint, fly heading");
                }
                if (!aircraft.getNavState().getLatModes().contains("Hold at", false)) {
                    aircraft.getNavState().getLatModes().add("Hold at");
                }
            }
        } else if (latMode.equals("After waypoint, fly heading")) {
            latModeQueue.addLast("sidstar");
            clearedAftWpt.addLast(RadarScreen.waypoints.get(afterWpt));
            clearedAftWptHdg.addLast(afterWptHdg);
        } else if (latMode.equals("Hold at")) {
            System.out.println("Hold at");
        } else {
            latModeQueue.addLast("vector");
            this.clearedHdg.addLast(clearedHdg);
            if (aircraft instanceof Arrival) {
                clearedIls.addLast(aircraft.getAirport().getApproaches().get(clearedILS.substring(3)));
            }
        }
        dispLatMode.addLast(latMode);
        length++;
        fillUp(latModeQueue);
        fillUp(this.clearedHdg);
        fillUp(clearedDirect);
        fillUp(clearedAftWpt);
        fillUp(clearedAftWptHdg);
        fillUp(clearedIls);
    }

    /** Adds new altitude instructions to queue, called after sendLat */
    public void sendAlt(String altMode, int clearedAlt) {
        if (altMode.equals("Climb via SID") || altMode.equals("Descend via STAR")) {
            altModeQueue.addLast("sidstar");
        } else {
            altModeQueue.addLast("open");
            clearedExpedite.addLast(altMode.contains("Expedite"));
        }
        this.clearedAlt.addLast(clearedAlt);
        dispAltMode.addLast(altMode);
        fillUp(clearedExpedite);
    }

    /** Adds new speed instructions to queue, called after sendAlt */
    public void sendSpd(String spdMode, int clearedSpd) {
        if (spdMode.equals("No speed restrictions")) {
            spdModeQueue.addLast("open");
        } else {
            spdModeQueue.addLast("sidstar");
        }
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

    public int getLastSidStarIndex() {
        return lastSidStarIndex;
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
}
