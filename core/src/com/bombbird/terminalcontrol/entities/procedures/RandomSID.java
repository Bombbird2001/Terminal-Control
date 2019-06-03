package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class RandomSID {
    private static final HashMap<String, HashMap<String, int[][]>> noise = new HashMap<String, HashMap<String, int[][]>>();

    /** Loads SID noise info for the airport */
    public static void loadSidNoise(String icao) {
        noise.put(icao, FileLoader.loadNoise(icao, true));
    }

    /** Gets a random SID for the airport and runway */
    public static Sid randomSID(Airport airport, String rwy) {
        Array<Sid> possibleSids = new Array<Sid>();
        for (Sid sid: airport.getSids().values()) {
            if (sid.getRunways().contains(rwy, false) && checkNoise(airport, sid.getName())) possibleSids.add(sid);
        }

        if (possibleSids.size == 0) {
            Gdx.app.log("Random SID", "No SIDs found to match criteria for " + airport.getIcao() + " " + rwy);
            throw new IllegalArgumentException("No SIDs found to match criteria for " + airport.getIcao() + " " + rwy);
        } else {
            return possibleSids.get(MathUtils.random(0, possibleSids.size - 1));
        }
    }

    /** Check whether a SID is allowed to be used for the airport at the current time */
    private static boolean checkNoise(Airport airport, String sid) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int additional = calendar.get(Calendar.AM_PM) == Calendar.PM ? 12 : 0;
        int time = (calendar.get(Calendar.HOUR) + additional) * 100 + calendar.get(Calendar.MINUTE);
        if (!noise.containsKey(airport.getIcao()) || !noise.get(airport.getIcao()).containsKey(sid)) return true;
        for (int[] timeSlot: noise.get(airport.getIcao()).get(sid)) {
            if (time >= timeSlot[0] && time < timeSlot[1]) return true;
        }
        return false;
    }
}
