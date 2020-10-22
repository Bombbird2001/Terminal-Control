package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;

import java.util.HashMap;

public class RandomSID {
    private static final HashMap<String, HashMap<String, Boolean>> noise = new HashMap<>();

    /** Loads SID noise info for the airport */
    public static void loadSidNoise(String icao) {
        noise.put(icao, FileLoader.loadNoise(icao, true));
    }

    /** Gets a random SID for the airport and runway */
    public static Sid randomSID(Airport airport, String rwy) {
        Array<Sid> possibleSids = new Array<>();
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
        if (!noise.get(airport.getIcao()).containsKey(sid)) return true; //Sid can be used both during day, night
        return DayNightManager.checkNoiseAllowed(noise.get(airport.getIcao()).get(sid));
    }
}
